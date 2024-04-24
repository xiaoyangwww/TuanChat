package com.ywt.common.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.SqlUtil;
import com.ywt.common.annotation.RedissonLock;
import com.ywt.common.service.LockService;
import com.ywt.common.utils.SpElUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodClassKey;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 功能描述
 * 分布式锁切面
 * @author: scott
 * @date: 2024年04月24日 11:30
 */
@Slf4j
@Aspect
@Component
@Order(0)//确保比事务注解先执行，分布式锁在事务外
public class RedissonLockAspect {

    @Autowired
    private LockService lockService;

    // @Around 是环绕通知，它可以在目标方法执行前后都执行自定义的逻辑，还可以控制是否调用目标方法以及如何处理目标方法的返回值。
    @Around("@annotation(com.ywt.common.annotation.RedissonLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 首先通过 joinPoint.getSignature() 获取方法签名，然后通过
        // ((MethodSignature) joinPoint.getSignature()).getMethod() 获取被调用的方法对象。
        // 接着，通过 method.getAnnotation(RedissonLock.class) 获取该方法上的 RedissonLock 注解对象。
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);
        //默认方法限定名+注解排名（可能多个）
        String prefix = StrUtil.isBlank(redissonLock.prefixKey()) ? SpElUtils.getMethodKey(method) :redissonLock.prefixKey();
        // 解析注解中的 SpEL 表达式，生成实际的键值。
        String key = SpElUtils.parseSpEl(method,joinPoint.getArgs(),redissonLock.key());
        return lockService.executeWithLockThrows(prefix + ":" + key,redissonLock.waitTime(),redissonLock.unit(),joinPoint::proceed);
    }


}
