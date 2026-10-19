package com.taiwei.reggie.notify;

import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.config.ReggieProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Email delivery through Spring's {@link JavaMailSender}. Enabled with {@code reggie.verification.channel=email}
 * plus the standard {@code spring.mail.*} settings (host, port, username, password).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "reggie.verification", name = "channel", havingValue = "email")
public class EmailCodeSender implements CodeSender {

    private final JavaMailSender mailSender;
    private final ReggieProperties.Email email;
    private final String from;

    public EmailCodeSender(JavaMailSender mailSender, ReggieProperties properties,
                           @Value("${spring.mail.username:}") String mailUsername) {
        this.mailSender = mailSender;
        this.email = properties.getEmail();
        this.from = StringUtils.hasText(email.getFrom()) ? email.getFrom() : mailUsername;
        if (!StringUtils.hasText(from)) {
            throw new IllegalStateException(
                    "reggie.verification.channel=email requires reggie.email.from or spring.mail.username");
        }
    }

    @Override
    public void send(String target, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(target);
        message.setSubject(email.getSubject());
        message.setText("Your verification code is " + code + ". It expires in a few minutes; do not share it.");
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("email delivery to {} failed: {}", target, e.getMessage());
            throw new CustomException("failed to send the verification code");
        }
    }
}
