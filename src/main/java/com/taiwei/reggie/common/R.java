package com.taiwei.reggie.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Envelope for every JSON response: {@code code} 1 = success, 0 = failure (with {@code msg}).
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code; // 1 = success, 0 (or anything else) = failure

    private String msg; // error message

    private T data; // payload

    private Map<String, Object> map = new HashMap<>(); // dynamic extras

    public static <T> R<T> success(T object) {
        R<T> r = new R<>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
