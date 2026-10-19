package com.taiwei.reggie.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application-specific settings (prefix {@code reggie}). Everything is overridable through environment variables,
 * see {@code application.yml} and {@code .env.example}.
 */
@Data
@ConfigurationProperties(prefix = "reggie")
public class ReggieProperties {

    /** Directory that uploaded dish / setmeal images are written to. */
    private String imagePath = "./data/images/";

    private final Verification verification = new Verification();
    private final Sms sms = new Sms();
    private final Email email = new Email();
    private final BootstrapAdmin bootstrapAdmin = new BootstrapAdmin();

    @Data
    public static class Verification {
        /** How the login code reaches the customer: {@code log} (default), {@code sms} or {@code email}. */
        private String channel = "log";
        /** Demo helper: echo the generated code in the /user/sendMsg response. Never enable in production. */
        private boolean exposeCode = false;
        private int ttlMinutes = 5;
        /** Wrong guesses allowed before the code is invalidated. */
        private int maxAttempts = 5;
        /** Minimum seconds between two codes for the same phone / email (0 disables the limit). */
        private int resendCooldownSeconds = 30;
    }

    @Data
    public static class Sms {
        private String regionId = "cn-hangzhou";
        private String accessKeyId;
        private String accessKeySecret;
        private String signName;
        private String templateCode;
    }

    @Data
    public static class Email {
        /** Sender address; falls back to {@code spring.mail.username}. */
        private String from;
        private String subject = "Your verification code";
    }

    /** Creates a first administrator when the employee table is empty and a password is configured. */
    @Data
    public static class BootstrapAdmin {
        private String username = "admin";
        private String name = "Administrator";
        private String password;
    }
}
