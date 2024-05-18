package com.ywt.chat.transaction.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.ywt.chat.transaction.dao.SecureInvokeRecordDao;
import com.ywt.chat.transaction.domain.dto.SecureInvokeDTO;
import com.ywt.chat.transaction.domain.entity.SecureInvokeRecord;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ywt.chat.common.utils.JsonUtils;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * <p>
 * 本地消息表 服务类
 * </p>
 *
 * @author ywt
 * @since 2024-05-04
 */
@Slf4j
@AllArgsConstructor
public class SecureInvokeService {
    // 偏移时间
    public static final double RETRY_INTERVAL_MINUTES = 2D;

    private SecureInvokeRecordDao secureInvokeRecordDao;

    private final Executor executor;


    //@Scheduled(cron = "*/5 * * * * ?") // 每5秒执行一次
    public void retry() {
        List<SecureInvokeRecord> secureInvokeRecords = secureInvokeRecordDao.getWaitRetryRecords();
        for (SecureInvokeRecord secureInvokeRecord : secureInvokeRecords) {
            doAsyncInvoke(secureInvokeRecord);
        }
    }


    public void save(SecureInvokeRecord record) {
        secureInvokeRecordDao.save(record);
    }
    public void removeRecord(Long recordId) {
        secureInvokeRecordDao.removeById(recordId);
    }

    public void invoke(SecureInvokeRecord invokeRecord, boolean async) {
        boolean inTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        //非事务状态，直接执行，不做任何保证。
        if (!inTransaction) {
            return;
        }
        //保存执行数据
        save(invokeRecord);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @SneakyThrows
            @Override
            public void afterCommit() {
                //事务后执行
                if (async) {
                    doAsyncInvoke(invokeRecord);
                } else {
                    doInvoke(invokeRecord);
                }
            }
        });
    }

    public void doAsyncInvoke(SecureInvokeRecord invokeRecord) {
        executor.execute(() -> {
            System.out.println(Thread.currentThread().getName());
            doInvoke(invokeRecord);
        });
    }

    public void doInvoke(SecureInvokeRecord invokeRecord) {
        SecureInvokeDTO secureInvokeDTO = invokeRecord.getSecureInvokeDTO();
        try {
            SecureInvokeHolder.setInvoking();
            Class<?> beanClass = Class.forName(secureInvokeDTO.getClassName());
            Object bean = SpringUtil.getBean(beanClass);
            List<String> parameterStrings = JsonUtils.toList(secureInvokeDTO.getParameterTypes(), String.class);
            List<Class<?>> parameterClasses = getParameters(parameterStrings);
            Method method = ReflectUtil.getMethod(beanClass, secureInvokeDTO.getMethodName(), parameterClasses.toArray(new Class[]{}));
            Object[] args = getArgs(secureInvokeDTO, parameterClasses);
            //执行方法
            method.invoke(bean,args);
            //执行成功更新状态,删除记录
            removeRecord(invokeRecord.getId());
        } catch (Throwable e) {
            log.error("SecureInvokeService invoke fail", e);
            //执行失败，等待下次执行
            retryRecord(invokeRecord, e.getMessage());
        }finally {
            SecureInvokeHolder.invoked();
        }
    }

    private void retryRecord(SecureInvokeRecord invokeRecord, String errorMsg) {
        Integer retryTimes = invokeRecord.getRetryTimes() + 1;
        SecureInvokeRecord update = new SecureInvokeRecord();
        update.setId(invokeRecord.getId());
        update.setFailReason(errorMsg);
        update.setNextRetryTime(getNextRetryTime(retryTimes));
        if (retryTimes > invokeRecord.getMaxRetryTimes()) {
            // 超过最大重试次数，失败
            update.setStatus(SecureInvokeRecord.STATUS_FAIL);
        }else {
            update.setRetryTimes(retryTimes);
        }
        secureInvokeRecordDao.updateById(update);
    }

    private Date getNextRetryTime(Integer retryTimes) {//或者可以采用退避算法
        double waitMinutes = Math.pow(RETRY_INTERVAL_MINUTES, retryTimes);//重试时间指数上升 2m 4m 8m 16m
        return DateUtil.offsetMinute(new Date(), (int) waitMinutes);
    }

    @NotNull
    private Object[] getArgs(SecureInvokeDTO secureInvokeDTO, List<Class<?>> parameterClasses) {
        JsonNode jsonNode = JsonUtils.toJsonNode(secureInvokeDTO.getArgs());
        Object[] args = new Object[jsonNode.size()];
        for (int i = 0; i < jsonNode.size(); i++) {
            Class<?> aClass = parameterClasses.get(i);
            args[i] = JsonUtils.nodeToValue(jsonNode.get(i), aClass);
        }
        return args;
    }

    private List<Class<?>> getParameters(List<String> parameterStrings) {
        return parameterStrings.stream().map(name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                log.error("SecureInvokeService class not fund", e);
            }
            return null;
        }).collect(Collectors.toList());
    }
}
