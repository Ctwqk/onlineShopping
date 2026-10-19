package com.taiwei.reggie.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taiwei.reggie.common.BaseContext;
import com.taiwei.reggie.common.R;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Session-based authentication for both UIs, driven by {@link AccessPolicy}. Unauthenticated callers get the
 * {@code {"code":0,"msg":"NOTLOGIN"}} envelope, which the front-end axios interceptors turn into a redirect to the
 * matching login page. The id of the caller is exposed through {@link BaseContext} for the duration of the request.
 */
@Component
public class LoginCheckFilter extends OncePerRequestFilter {

    public static final String EMPLOYEE_SESSION_KEY = "employee";
    public static final String USER_SESSION_KEY = "user";

    private final ObjectMapper objectMapper;

    public LoginCheckFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        AccessPolicy.Access access = AccessPolicy.classify(request.getMethod(), request.getRequestURI());
        if (access == AccessPolicy.Access.PUBLIC) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        Long principal = resolvePrincipal(session, access);
        if (principal == null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(R.error("NOTLOGIN")));
            return;
        }
        BaseContext.setCurrentID(principal);
        try {
            chain.doFilter(request, response);
        } finally {
            BaseContext.clear(); // servlet threads are pooled
        }
    }

    private static Long resolvePrincipal(HttpSession session, AccessPolicy.Access access) {
        if (session == null) {
            return null;
        }
        Long employee = (Long) session.getAttribute(EMPLOYEE_SESSION_KEY);
        Long user = (Long) session.getAttribute(USER_SESSION_KEY);
        return switch (access) {
            case ADMIN -> employee;
            case CUSTOMER -> user;
            case ANY -> employee != null ? employee : user;
            case PUBLIC -> null;
        };
    }
}
