package com.taiwei.reggie.notify;

import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Same application, but with the resend cooldown switched on (it is disabled in the shared test profile). */
@TestPropertySource(properties = "reggie.verification.resend-cooldown-seconds=60")
class VerificationCooldownIT extends AbstractIntegrationTest {

    @Autowired
    private VerificationCodeService codes;

    @Test
    void aSecondCodeForTheSameTargetIsRefusedUntilTheCooldownPasses() {
        String first = codes.issue("13800138000");

        assertThatThrownBy(() -> codes.issue("13800138000"))
                .isInstanceOf(CustomException.class).hasMessageContaining("wait");
        assertThat(codes.verify("13800138000", first)).isTrue(); // the first code is still valid
        assertThat(codes.issue("13900139000")).isNotBlank();      // other targets are unaffected
    }
}
