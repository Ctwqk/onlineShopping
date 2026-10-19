package com.taiwei.reggie.notify;

import com.taiwei.reggie.support.AbstractIntegrationTest;
import com.taiwei.reggie.support.ApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationCodeIT extends AbstractIntegrationTest {

    @Autowired
    private VerificationCodeService codes;

    @Test
    void codeIsFourDigitsAndSingleUse() {
        String code = codes.issue("13800138000");

        assertThat(code).matches("[1-9][0-9]{3}");
        assertThat(codes.verify("13800138000", code)).isTrue();
        assertThat(codes.verify("13800138000", code)).isFalse(); // consumed
    }

    @Test
    void wrongGuessesInvalidateTheCodeAfterTheConfiguredNumberOfAttempts() {
        String code = codes.issue("13900139000");
        String wrong = code.equals("1111") ? "2222" : "1111";

        assertThat(codes.verify("13900139000", wrong)).isFalse();
        assertThat(codes.verify("13900139000", wrong)).isFalse();
        assertThat(codes.verify("13900139000", wrong)).isFalse(); // third strike (max-attempts = 3)

        assertThat(codes.verify("13900139000", code)).isFalse(); // even the right code is gone now
    }

    @Test
    void codesAreScopedToTheTargetAndUnknownTargetsNeverVerify() {
        String code = codes.issue("13800138000");

        assertThat(codes.verify("13900139000", code)).isFalse();
        assertThat(codes.verify("never-asked", "1234")).isFalse();
        assertThat(codes.verify("13800138000", null)).isFalse();
    }

    @Test
    void loginFlowRejectsMissingWrongAndReusedCodes() {
        ApiClient client = anonymous();
        client.post("/user/sendMsg", Map.of("phone", "")).assertFailure("required");
        client.post("/user/login", Map.of("phone", "13800138000")).assertFailure("required");
        client.post("/user/login", Map.of("phone", "13800138000", "code", "0000"))
                .assertFailure("wrong or expired");

        String code = client.post("/user/sendMsg", Map.of("phone", "13800138000")).json()
                .path("map").path("devCode").asText();
        client.post("/user/login", Map.of("phone", "13800138000", "code", code)).assertSuccess();
        // the same code cannot be replayed from another browser
        anonymous().post("/user/login", Map.of("phone", "13800138000", "code", code))
                .assertFailure("wrong or expired");
    }

    @Test
    void firstLoginCreatesTheCustomerAndLaterLoginsReuseIt() {
        customerClient("13800138000");
        customerClient("13800138000");

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM `user`", Integer.class)).isEqualTo(1);
    }

    @Test
    void disabledCustomersAreTurnedAway() {
        customerClient("13800138000");
        jdbc.update("UPDATE `user` SET status = 0");

        ApiClient client = anonymous();
        String code = client.post("/user/sendMsg", Map.of("phone", "13800138000")).json()
                .path("map").path("devCode").asText();
        client.post("/user/login", Map.of("phone", "13800138000", "code", code)).assertFailure("disabled");
    }

    @Test
    void customerLogoutEndsTheCustomerSession() {
        ApiClient customer = customerClient("13800138000");
        customer.get("/shoppingCart/list").assertSuccess();

        customer.postNoBody("/user/loginout").assertSuccess();

        customer.get("/shoppingCart/list").assertNotLoggedIn();
    }
}
