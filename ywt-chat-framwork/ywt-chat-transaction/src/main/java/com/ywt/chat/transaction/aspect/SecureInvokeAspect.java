package com.ywt.chat.transaction.aspect;

import cn.hutool.core.date.DateUtil;
import com.ywt.chat.transaction.annotation.SecureInvoke;
import com.ywt.chat.transaction.domain.dto.SecureInvokeDTO;
import com.ywt.chat.transaction.domain.entity.SecureInvokeRecord;
import com.ywt.chat.transaction.service.SecureInvokeHolder;
import com.ywt.chat.transaction.service.SecureInvokeService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ywt.chat.common.utils.JsonUtils;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月04日 16:52
 */
@Slf4j
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 1)//确保最先执行
@Component
public class SecureInvokeAspect {

    @Autowired
    private SecureInvokeService secureInvokeService;

    @Around("@annotation(secureInvoke)")
    public Object around(ProceedingJoinPoint joinPoint, SecureInvoke secureInvoke) throws Throwable {
        boolean async = secureInvoke.async();
        // 检查当前是否存在活动的事务
        boolean inTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        //非事务状态，直接执行，不做任何保证。作用：本地方法表的目的就是要将分布式事务变为本地事务执行，所以使用注解必须有事务
        if (SecureInvokeHolder.isInvoking() || !inTransaction) {
            return joinPoint.proceed();
        }
        // 返回类型为 Signature 接口，可以通过该接口获取方法名、声明类型等信息。
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 有序获取方法参数的类型
        List<String> parameters = Stream.of(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList());
        SecureInvokeDTO secureInvokeDTO = SecureInvokeDTO.builder()
                .args(JsonUtils.toStr(joinPoint.getArgs())) // 保存方法调用时具体参数的值json
                .className(method.getDeclaringClass().getName()) // 类名
                .methodName(method.getName()) // 方法名
                .parameterTypes(JsonUtils.toStr(parameters)) // 参数类型 json
                .build();
        SecureInvokeRecord invokeRecord = SecureInvokeRecord.builder()
                .secureInvokeDTO(secureInvokeDTO)
                .maxRetryTimes(secureInvoke.maxRetryTimes()) // 设置最大重试次数
                .nextRetryTime(DateUtil.offsetMinute(new Date(), (int) SecureInvokeService.RETRY_INTERVAL_MINUTES)) //偏移时间
                .build();
        secureInvokeService.invoke(invokeRecord, async);
        return null;

    }

}
