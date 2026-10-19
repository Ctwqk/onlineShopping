package com.taiwei.reggie.common;

/**
 * Holds the id of the logged-in principal (employee or customer, depending on the endpoint) for the current
 * request thread. Set by {@code LoginCheckFilter}, read by controllers/services and by the audit-field filler.
 */
public class BaseContext {
    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    public static void setCurrentID(Long id) {
        THREAD_LOCAL.set(id);
    }

    public static Long getCurrentID() {
        return THREAD_LOCAL.get();
    }

    /** Must be called when the request finishes: servlet threads are pooled. */
    public static void clear() {
        THREAD_LOCAL.remove();
    }
}
