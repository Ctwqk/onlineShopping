package com.taiwei.reggie.common;

public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();

    public static void setCurrentID(Long id){
        threadLocal.set(id);
    }
    public static Long getCurrentID(){
        return threadLocal.get();
    }



}
