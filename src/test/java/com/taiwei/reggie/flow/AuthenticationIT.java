package com.taiwei.reggie.flow;

import com.taiwei.reggie.support.AbstractIntegrationTest;
import com.taiwei.reggie.support.ApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationIT extends AbstractIntegrationTest {

    @Test
    void adminLogsInAndNeverReceivesThePasswordHash() {
        ApiClient admin = anonymous();

        ApiClient.Response login = admin.post("/employee/login",
                Map.of("username", "admin", "password", "Adm1n-for-tests")).assertSuccess();

        assertThat(login.data().path("username").asText()).isEqualTo("admin");
        assertThat(login.data().has("password")).isFalse();
        admin.get("/employee/page?page=1&pageSize=10").assertSuccess();
    }

    @Test
    void wrongPasswordAndUnknownUserGetTheSameAnswer() {
        ApiClient client = anonymous();

        String wrongPassword = client.post("/employee/login", Map.of("username", "admin", "password", "nope"))
                .assertFailure("password or username wrong").message();
        String unknownUser = client.post("/employee/login", Map.of("username", "ghost", "password", "nope"))
                .assertFailure("password or username wrong").message();

        assertThat(wrongPassword).isEqualTo(unknownUser); // no user enumeration
    }

    @Test
    void protectedEndpointsRejectAnonymousCallers() {
        ApiClient client = anonymous();

        client.get("/dish/page?page=1&pageSize=10").assertNotLoggedIn();
        client.get("/employee/page?page=1&pageSize=10").assertNotLoggedIn();
        client.get("/shoppingCart/list").assertNotLoggedIn();
        client.get("/order/page?page=1&pageSize=10").assertNotLoggedIn();
        // public: the catalogue UIs can load without a login only after the login page, but docs/health are open
        assertThat(client.get("/actuator/health").status()).isEqualTo(200);
    }

    @Test
    void logoutReallyEndsTheAdminSession() {
        ApiClient admin = adminClient();
        admin.get("/employee/page?page=1&pageSize=10").assertSuccess();

        admin.postNoBody("/employee/logout").assertSuccess();

        admin.get("/employee/page?page=1&pageSize=10").assertNotLoggedIn();
    }

    @Test
    void disabledEmployeesCannotLogIn() {
        ApiClient admin = adminClient();
        admin.post("/employee", Map.of("username", "sam", "name", "Sam", "phone", "13800000009", "sex", "1",
                "idNumber", "110101199001011234")).assertSuccess();
        Long id = jdbc.queryForObject("SELECT id FROM employee WHERE username = 'sam'", Long.class);

        // new employees get the documented initial password and are enabled
        anonymous().post("/employee/login", Map.of("username", "sam", "password", "123456")).assertSuccess();

        jdbc.update("UPDATE employee SET status = 0 WHERE id = ?", id);
        anonymous().post("/employee/login", Map.of("username", "sam", "password", "123456"))
                .assertFailure("user banned");
    }

    @Test
    void newEmployeePasswordsAreStoredAsBcrypt() {
        adminClient().post("/employee", Map.of("username", "kim", "name", "Kim", "phone", "13800000008",
                "sex", "0", "idNumber", "110101199001011235")).assertSuccess();

        String stored = jdbc.queryForObject("SELECT password FROM employee WHERE username = 'kim'", String.class);

        assertThat(stored).startsWith("$2").hasSize(60);
    }

    @Test
    void legacyMd5AccountsCanStillLogInAndAreUpgradedToBcrypt() {
        String md5 = DigestUtils.md5DigestAsHex("old-secret".getBytes());
        jdbc.update("INSERT INTO employee (id, name, username, password, phone, sex, id_number, status, create_time,"
                + " update_time) VALUES (9001, 'Legacy', 'legacy', ?, '1', '1', '1', 1, NOW(), NOW())", md5);

        anonymous().post("/employee/login", Map.of("username", "legacy", "password", "old-secret")).assertSuccess();

        String stored = jdbc.queryForObject("SELECT password FROM employee WHERE username = 'legacy'", String.class);
        assertThat(stored).startsWith("$2").hasSize(60);
        // and the account keeps working with the new hash
        anonymous().post("/employee/login", Map.of("username", "legacy", "password", "old-secret")).assertSuccess();
        anonymous().post("/employee/login", Map.of("username", "legacy", "password", "wrong"))
                .assertFailure("password or username wrong");
    }

    @Test
    void profileEditsCannotOverwriteThePassword() {
        ApiClient admin = adminClient();
        admin.post("/employee", Map.of("username", "lee", "name", "Lee", "phone", "13800000007", "sex", "1",
                "idNumber", "110101199001011236")).assertSuccess();
        Long id = jdbc.queryForObject("SELECT id FROM employee WHERE username = 'lee'", Long.class);
        String before = jdbc.queryForObject("SELECT password FROM employee WHERE id = ?", String.class, id);

        admin.put("/employee", Map.of("id", id.toString(), "name", "Lee R.", "password", "plaintext!"))
                .assertSuccess();

        assertThat(jdbc.queryForObject("SELECT password FROM employee WHERE id = ?", String.class, id))
                .isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT name FROM employee WHERE id = ?", String.class, id))
                .isEqualTo("Lee R.");
    }

    @Test
    void customersAndAdminsAreKeptApart() {
        ApiClient customer = customerClient("13800138000");

        customer.get("/shoppingCart/list").assertSuccess();
        customer.get("/dish/list").assertSuccess(); // shared, read-only catalogue
        customer.get("/employee/page?page=1&pageSize=10").assertNotLoggedIn();
        customer.get("/dish/page?page=1&pageSize=10").assertNotLoggedIn();
        customer.get("/order/page?page=1&pageSize=10").assertNotLoggedIn();
        customer.post("/category", Map.of("name", "x", "type", 1, "sort", 1)).assertNotLoggedIn();

        ApiClient admin = adminClient();
        admin.get("/shoppingCart/list").assertNotLoggedIn(); // an admin session is not a customer session
        admin.get("/addressBook/list").assertNotLoggedIn();
    }

    @Test
    void adminAndCustomerSharingOneBrowserSessionKeepSeparateIdentities() {
        ApiClient browser = adminClient();
        String code = browser.post("/user/sendMsg", Map.of("phone", "13900139000")).json()
                .path("map").path("devCode").asText();
        browser.post("/user/login", Map.of("phone", "13900139000", "code", code)).assertSuccess();

        // the cart belongs to the customer, not to the employee id that is also in the session
        browser.post("/addressBook", Map.of("consignee", "Linda", "phone", "13900139000", "detail", "x"))
                .assertSuccess();
        Long owner = jdbc.queryForObject("SELECT user_id FROM address_book", Long.class);
        Long customerId = jdbc.queryForObject("SELECT id FROM `user` WHERE phone = '13900139000'", Long.class);
        assertThat(owner).isEqualTo(customerId);
    }
}
