package com.ywt.common.thread;

import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月20日 22:50
 */
@Slf4j
public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Exception in thread {} ", t.getName(), e);
    }
}
