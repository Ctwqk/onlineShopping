package com.taiwei.reggie.controller;

import com.taiwei.reggie.config.ReggieProperties;
import com.taiwei.reggie.entity.User;
import com.taiwei.reggie.notify.VerificationCodeService;
import com.taiwei.reggie.service.UserService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @ParameterizedTest
    @CsvSource({"default,true,false", "prod,true,false", "test,true,false", "dev,true,true", "dev,false,false"})
    void codeEchoRequiresTheDevProfileAndExplicitOptIn(String profile, boolean expose, boolean expected) {
        // Only delivery/storage are mocked: this tests the real response's disclosure policy.
        VerificationCodeService codes = mock(VerificationCodeService.class);
        when(codes.issue("13800138000")).thenReturn("1234");
        ReggieProperties settings = new ReggieProperties();
        settings.getVerification().setExposeCode(expose);

        new ApplicationContextRunner()
                .withInitializer(context -> {
                    if (!profile.equals("default")) {
                        context.getEnvironment().setActiveProfiles(profile);
                    }
                })
                .withBean(UserService.class, () -> mock(UserService.class))
                .withBean(VerificationCodeService.class, () -> codes)
                .withBean(ReggieProperties.class, () -> settings)
                .withBean(UserController.class)
                .run(context -> {
                    User target = new User();
                    target.setPhone("13800138000");
                    var response = context.getBean(UserController.class).sendMsg(target);
                    assertThat(response.getCode()).isEqualTo(1);
                    if (expected) {
                        assertThat(response.getMap()).containsEntry("devCode", "1234");
                    } else {
                        assertThat(response.getMap()).doesNotContainKey("devCode");
                    }
                });
    }
}
