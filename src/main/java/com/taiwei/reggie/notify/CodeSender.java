package com.taiwei.reggie.notify;

/**
 * Delivers a one-time login code to a customer. Exactly one implementation is active, chosen by
 * {@code reggie.verification.channel} (log | sms | email).
 */
public interface CodeSender {

    /**
     * @param target phone number or email address the customer typed in
     * @param code   the one-time code
     */
    void send(String target, String code);
}
