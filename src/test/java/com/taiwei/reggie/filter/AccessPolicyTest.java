package com.taiwei.reggie.filter;

import com.taiwei.reggie.filter.AccessPolicy.Access;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class AccessPolicyTest {

    @ParameterizedTest
    @CsvSource({
            "GET,/,PUBLIC",
            "GET,/backend/page/login/login.html,PUBLIC",
            "GET,/front/styles/index.css,PUBLIC",
            "POST,/employee/login,PUBLIC",
            "POST,/user/sendMsg,PUBLIC",
            "POST,/user/login,PUBLIC",
            "GET,/doc.html,PUBLIC",
            "GET,/v3/api-docs,PUBLIC",
            "GET,/actuator/health,PUBLIC",
            "GET,/actuator/health/liveness,PUBLIC"
    })
    void staticFilesLoginAndDocsArePublic(String method, String path, Access expected) {
        assertThat(AccessPolicy.classify(method, path)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "GET,/shoppingCart/list",
            "POST,/shoppingCart/add",
            "POST,/shoppingCart/sub",
            "DELETE,/shoppingCart/clean",
            "GET,/addressBook/list",
            "PUT,/addressBook/default",
            "POST,/order/submit",
            "GET,/order/userPage",
            "POST,/order/again"
    })
    void customerFeaturesNeedACustomerLogin(String method, String path) {
        assertThat(AccessPolicy.classify(method, path)).isEqualTo(Access.CUSTOMER);
    }

    @ParameterizedTest
    @CsvSource({
            "GET,/category/list",
            "GET,/dish/list",
            "GET,/setmeal/list",
            "GET,/setmeal/dish/123",
            "GET,/common/download"
    })
    void readOnlyCatalogueIsSharedByBothUis(String method, String path) {
        assertThat(AccessPolicy.classify(method, path)).isEqualTo(Access.ANY);
    }

    @ParameterizedTest
    @CsvSource({
            "GET,/employee/page",
            "POST,/employee",
            "GET,/dish/page",
            "POST,/dish",
            "DELETE,/dish",
            "POST,/dish/status/0",
            "PUT,/setmeal",
            "POST,/category",
            "GET,/order/page",
            "PUT,/order",
            "POST,/common/upload"
    })
    void everythingElseIsAdminOnly(String method, String path) {
        assertThat(AccessPolicy.classify(method, path)).isEqualTo(Access.ADMIN);
    }

    @Test
    void writesToTheSharedCataloguePathsAreStillAdminOnly() {
        // GET /dish/list is shared, but POST /dish/list (or any other verb) is not a public contract
        assertThat(AccessPolicy.classify("POST", "/dish/list")).isEqualTo(Access.ADMIN);
        assertThat(AccessPolicy.classify("DELETE", "/category/list")).isEqualTo(Access.ADMIN);
    }

    @Test
    void unknownPathsDefaultToAdmin() {
        assertThat(AccessPolicy.classify("GET", "/something/new")).isEqualTo(Access.ADMIN);
    }
}
