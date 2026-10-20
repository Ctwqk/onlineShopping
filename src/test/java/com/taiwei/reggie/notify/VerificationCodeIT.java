package com.taiwei.reggie.notify;

import com.taiwei.reggie.support.AbstractIntegrationTest;
import com.taiwei.reggie.support.ApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    void concurrentRequestsCannotConsumeTheSameCodeTwice() throws Exception {
        String code = codes.issue("13800138000");
        var pool = Executors.newFixedThreadPool(12);
        CountDownLatch ready = new CountDownLatch(12);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 12; i++) {
                results.add(pool.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("verification workers did not start");
                    }
                    return codes.verify("13800138000", code);
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            int accepted = 0;
            for (Future<Boolean> result : results) {
                if (result.get(10, TimeUnit.SECONDS)) {
                    accepted++;
                }
            }
            assertThat(accepted).as("a login code has only one successful consumer").isEqualTo(1);
        } finally {
            start.countDown();
            pool.shutdownNow();
        }
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

        String code = requestCode(client, "13800138000");
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
        String code = requestCode(client, "13800138000");
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
