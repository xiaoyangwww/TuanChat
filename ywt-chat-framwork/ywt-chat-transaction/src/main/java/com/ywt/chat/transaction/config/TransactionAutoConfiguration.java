package com.ywt.chat.transaction.config;


import com.ywt.chat.transaction.annotation.SecureInvokeConfigurer;
import com.ywt.chat.transaction.aspect.SecureInvokeAspect;
import com.ywt.chat.transaction.dao.SecureInvokeRecordDao;
import com.ywt.chat.transaction.mapper.SecureInvokeRecordMapper;
import com.ywt.chat.transaction.service.MQProducer;
import com.ywt.chat.transaction.service.SecureInvokeService;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;
import org.springframework.util.function.SingletonSupplier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-08-06
 */
@Configuration
@EnableScheduling //  // 启用定时任务执行功能
@MapperScan(basePackageClasses = SecureInvokeRecordMapper.class)
@Import({SecureInvokeAspect.class, SecureInvokeRecordDao.class})
public class TransactionAutoConfiguration {

    @Nullable
    protected Executor executor;

    /**
     * Collect any {@link AsyncConfigurer} beans through autowiring.
     *
     */
    @Autowired  //ObjectProvider<SecureInvokeConfigurer> configurers 获取 SecureInvokeConfigurer实现类的bean对象 ThreadPoolConfig
    void setConfigurers(ObjectProvider<SecureInvokeConfigurer> configurers) {
        // 定义一个Supplier方法体来获取SecureInvokeConfigurer一个实现类的bean对象，因为configurers可能有多个bean
        Supplier<SecureInvokeConfigurer> configurer = SingletonSupplier.of(() -> {
            List<SecureInvokeConfigurer> candidates = configurers.stream().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(candidates)) {
                return null;
            }
            // SecureInvokeConfigurer有多个实现类bean，报错
            if (candidates.size() > 1) {
                throw new IllegalStateException("Only one SecureInvokeConfigurer may exist");
            }
            return candidates.get(0);
        });
        // configurer.get() 调用上面的方法体，如果没有线程池， Java 并行框架中的共享 ForkJoinPool 线程池
        executor = Optional.ofNullable(configurer.get()).map(SecureInvokeConfigurer::getSecureInvokeExecutor).orElse(ForkJoinPool.commonPool());
    }

    @Bean
    public SecureInvokeService getSecureInvokeService(SecureInvokeRecordDao dao) {
        return new SecureInvokeService(dao, executor);
    }

    @Bean
    public MQProducer getMQProducer() {
        return new MQProducer();
    }
}
