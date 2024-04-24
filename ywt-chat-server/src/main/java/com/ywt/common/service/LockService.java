package com.ywt.common.service;

import com.ywt.common.exception.BusinessErrorEnum;
import com.ywt.common.exception.CommonErrorEnum;
import com.ywt.common.exception.ErrorEnum;
import com.ywt.common.utils.AssertUtil;
import lombok.SneakyThrows;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class LockService {
    @Autowired
    private RedissonClient redissonClient;

    public <T> T executeWithLockThrows(String key, Integer waitTime, TimeUnit unit,SupplierThrow<T> supplierThrow) throws Throwable {
        RLock lock = redissonClient.getLock(key);
        boolean lockSuccess = lock.tryLock(waitTime, unit);
        AssertUtil.isTrue(lockSuccess, CommonErrorEnum.LOCK_LIMIT);
        try {
            return supplierThrow.get();
        } finally {
            lock.unlock();
        }

    }

    @SneakyThrows
    public <T> T executeWithLock(String key, int waitTime, TimeUnit unit, Supplier<T> supplier) {
        return executeWithLockThrows(key, waitTime, unit, supplier::get);
    }

    public <T> T executeWithLock(String key, Supplier<T> supplier) {
        return executeWithLock(key, -1, TimeUnit.MILLISECONDS, supplier);
    }




    @FunctionalInterface
    public interface SupplierThrow<T> {

        /**
         * Gets a result.
         *
         * @return a result
         */
        T get() throws Throwable;
    }
}
