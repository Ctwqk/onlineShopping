package com.taiwei.reggie.notify;

import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.config.ReggieProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;

/**
 * Issues and verifies one-time login codes. Codes live in Redis with a TTL, are single use, can be guessed only a
 * limited number of times, and cannot be re-requested too quickly (protects the SMS / mail budget).
 */
@Service
public class VerificationCodeService {

    private static final String CODE_KEY = "login:code:";
    private static final String ATTEMPTS_KEY = "login:attempts:";
    private static final String COOLDOWN_KEY = "login:cooldown:";

    // Checking, consuming and counting guesses must be one Redis operation: GET followed by DEL allows
    // simultaneous login requests to reuse the same code.
    private static final RedisScript<Long> VERIFY = RedisScript.of("""
            local expected = redis.call('GET', KEYS[1])
            if not expected then return 0 end
            if expected == ARGV[1] then
                redis.call('DEL', KEYS[1], KEYS[2])
                return 1
            end
            local attempts = redis.call('INCR', KEYS[2])
            if attempts >= tonumber(ARGV[2]) then
                redis.call('DEL', KEYS[1], KEYS[2])
            elseif attempts == 1 then
                redis.call('EXPIRE', KEYS[2], ARGV[3])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redis;
    private final CodeSender sender;
    private final ReggieProperties.Verification settings;
    private final SecureRandom random = new SecureRandom();

    public VerificationCodeService(StringRedisTemplate redis, CodeSender sender, ReggieProperties properties) {
        this.redis = redis;
        this.sender = sender;
        this.settings = properties.getVerification();
    }

    /** Generates a 4-digit code, stores it and hands it to the configured {@link CodeSender}. */
    public String issue(String target) {
        if (settings.getResendCooldownSeconds() > 0) {
            Boolean fresh = redis.opsForValue().setIfAbsent(COOLDOWN_KEY + target, "1",
                    Duration.ofSeconds(settings.getResendCooldownSeconds()));
            if (!Boolean.TRUE.equals(fresh)) {
                throw new CustomException("please wait a moment before requesting another code");
            }
        }
        String code = String.valueOf(1000 + random.nextInt(9000));
        redis.opsForValue().set(CODE_KEY + target, code, Duration.ofMinutes(settings.getTtlMinutes()));
        redis.delete(ATTEMPTS_KEY + target);
        try {
            sender.send(target, code);
        } catch (RuntimeException e) {
            redis.delete(CODE_KEY + target);
            redis.delete(COOLDOWN_KEY + target);
            throw e;
        }
        return code;
    }

    /** @return true (and consumes the code) when {@code code} matches the one issued for {@code target}. */
    public boolean verify(String target, String code) {
        if (code == null) {
            return false;
        }
        Long accepted = redis.execute(VERIFY, List.of(CODE_KEY + target, ATTEMPTS_KEY + target),
                code.trim(), String.valueOf(settings.getMaxAttempts()),
                String.valueOf(Duration.ofMinutes(settings.getTtlMinutes()).toSeconds()));
        return Long.valueOf(1).equals(accepted);
    }
}
