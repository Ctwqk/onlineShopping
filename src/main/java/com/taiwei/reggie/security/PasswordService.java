package com.taiwei.reggie.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * Password hashing for employees. New hashes are BCrypt. The original project stored unsalted MD5 (32 hex chars);
 * those hashes still verify so existing accounts keep working, and {@link #isLegacy(String)} lets the caller
 * transparently upgrade them to BCrypt right after a successful login.
 */
@Component
public class PasswordService {

    private static final Pattern LEGACY_MD5 = Pattern.compile("^[0-9a-fA-F]{32}$");

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        if (isLegacy(storedHash)) {
            String md5 = DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(md5.getBytes(StandardCharsets.UTF_8),
                    storedHash.toLowerCase().getBytes(StandardCharsets.UTF_8));
        }
        return encoder.matches(rawPassword, storedHash);
    }

    /** @return true for a pre-BCrypt MD5 hash that should be re-hashed. */
    public boolean isLegacy(String storedHash) {
        return storedHash != null && LEGACY_MD5.matcher(storedHash).matches();
    }
}
