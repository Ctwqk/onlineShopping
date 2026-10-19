package com.taiwei.reggie.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default channel: writes the code to the application log instead of contacting an SMS / mail provider, so the
 * project runs without any third-party account. Not for production use.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "reggie.verification", name = "channel", havingValue = "log", matchIfMissing = true)
public class LoggingCodeSender implements CodeSender {

    @Override
    public void send(String target, String code) {
        log.info("verification code for {} is {} (log channel - nothing was sent)", target, code);
    }
}
