package com.taiwei.reggie.filter;

import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * Decides which kind of login an HTTP request needs. The admin console and the customer front share one
 * application (and possibly one browser session), so the check is by <em>who</em> is calling, not merely whether
 * anyone is logged in:
 * <ul>
 *   <li>{@link Access#PUBLIC} - static UI files, login endpoints, API docs, health;</li>
 *   <li>{@link Access#CUSTOMER} - cart, address book, own orders;</li>
 *   <li>{@link Access#ANY} - read-only catalogue endpoints used by both UIs;</li>
 *   <li>{@link Access#ADMIN} - everything else (employee, category/dish/setmeal management, order management,
 *       uploads).</li>
 * </ul>
 */
public final class AccessPolicy {

    public enum Access { PUBLIC, CUSTOMER, ANY, ADMIN }

    private record Rule(String method, String pattern, Access access) {
    }

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private static final List<Rule> RULES = List.of(
            new Rule(null, "/", Access.PUBLIC),
            new Rule(null, "/index.html", Access.PUBLIC),
            new Rule(null, "/favicon.ico", Access.PUBLIC),
            new Rule(null, "/error", Access.PUBLIC),
            new Rule(null, "/backend/**", Access.PUBLIC),
            new Rule(null, "/front/**", Access.PUBLIC),
            new Rule(null, "/employee/login", Access.PUBLIC),
            new Rule(null, "/employee/logout", Access.PUBLIC),
            new Rule(null, "/user/sendMsg", Access.PUBLIC),
            new Rule(null, "/user/login", Access.PUBLIC),
            new Rule(null, "/user/loginout", Access.PUBLIC),
            new Rule(null, "/doc.html", Access.PUBLIC),
            new Rule(null, "/webjars/**", Access.PUBLIC),
            new Rule(null, "/v3/api-docs/**", Access.PUBLIC),
            new Rule(null, "/swagger-ui.html", Access.PUBLIC),
            new Rule(null, "/swagger-ui/**", Access.PUBLIC),
            new Rule(null, "/swagger-resources/**", Access.PUBLIC),
            new Rule(null, "/actuator/health/**", Access.PUBLIC),
            new Rule(null, "/actuator/health", Access.PUBLIC),

            new Rule(null, "/shoppingCart/**", Access.CUSTOMER),
            new Rule(null, "/addressBook/**", Access.CUSTOMER),
            new Rule(null, "/order/submit", Access.CUSTOMER),
            new Rule(null, "/order/userPage", Access.CUSTOMER),
            new Rule(null, "/order/again", Access.CUSTOMER),

            new Rule("GET", "/category/list", Access.ANY),
            new Rule("GET", "/dish/list", Access.ANY),
            new Rule("GET", "/setmeal/list", Access.ANY),
            new Rule("GET", "/setmeal/dish/*", Access.ANY),
            new Rule("GET", "/common/download", Access.ANY));

    private AccessPolicy() {
    }

    public static Access classify(String method, String path) {
        for (Rule rule : RULES) {
            if ((rule.method() == null || rule.method().equalsIgnoreCase(method))
                    && MATCHER.match(rule.pattern(), path)) {
                return rule.access();
            }
        }
        return Access.ADMIN;
    }
}
