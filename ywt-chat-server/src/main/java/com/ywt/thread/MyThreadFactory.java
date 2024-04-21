package com.ywt.thread;

import lombok.AllArgsConstructor;

import java.util.concurrent.ThreadFactory;

/**
 * 功能描述
 *  装饰器模式
 *  写一个自己的线程工厂，把spring的线程工厂传进来。调用它的线程创建后，再扩展设置我们的异常捕获
 * @author: scott
 * @date: 2024年04月20日 22:47
 */
@AllArgsConstructor
public class MyThreadFactory implements ThreadFactory {

    private ThreadFactory original;
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = original.newThread(r);
        thread.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());//异常捕获
        return thread;
    }
}
