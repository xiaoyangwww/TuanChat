package com.ywt.common.utils;

import com.ywt.common.domain.dto.RequestInfo;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月21日 23:09
 */
public class RequestHolder {

    private static final ThreadLocal<RequestInfo> threadLocal = new ThreadLocal<>();

    public static void set(RequestInfo requestInfo) {
        threadLocal.set(requestInfo);
    }

    public static RequestInfo get(){
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
