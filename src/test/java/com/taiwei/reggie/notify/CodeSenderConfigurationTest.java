package com.taiwei.reggie.notify;

import com.taiwei.reggie.config.ReggieProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The optional channels must fail fast at startup when they are enabled without credentials. */
class CodeSenderConfigurationTest {

    @Test
    void smsChannelRefusesToStartWithoutCredentials() {
        ReggieProperties properties = new ReggieProperties();

        assertThatThrownBy(() -> new AliyunSmsCodeSender(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("REGGIE_SMS");
    }

    @Test
    void smsChannelStartsWhenFullyConfigured() {
        ReggieProperties properties = new ReggieProperties();
        properties.getSms().setAccessKeyId("id");
        properties.getSms().setAccessKeySecret("secret");
        properties.getSms().setSignName("sign");
        properties.getSms().setTemplateCode("SMS_1");

        assertThat(new AliyunSmsCodeSender(properties)).isNotNull();
    }

    @Test
    void emailChannelNeedsASenderAddress() {
        assertThatThrownBy(() -> new EmailCodeSender(new JavaMailSenderImpl(), new ReggieProperties(), ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reggie.email.from");
    }

    @Test
    void emailChannelFallsBackToTheMailUsernameAsSender() {
        assertThat(new EmailCodeSender(new JavaMailSenderImpl(), new ReggieProperties(), "shop@example.com"))
                .isNotNull();
    }
}
