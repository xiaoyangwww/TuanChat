package com.ywt.oss;

import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月10日 10:49
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OssProperties.class)
//读取 Spring Boot 配置文件中的 oss.enabled 属性的结果为 true 时，才会启用被注解的组件或配置
@ConditionalOnExpression("${oss.enabled}")
//当 Spring Boot 配置文件中存在名为 oss.type 的属性，并且其值为 minio 时，才会启用被注解的组件或配置。
@ConditionalOnProperty(value = "oss.type", havingValue = "minio")
public class MinIOConfiguration {

    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(MinioClient.class)
    public MinioClient minioClient(OssProperties ossProperties) {
        return MinioClient.builder()
                .endpoint(ossProperties.getEndpoint())
                .credentials(ossProperties.getAccessKey(),ossProperties.getSecretKey())
                .build();

    }

    @Bean
    @ConditionalOnBean(MinioClient.class)
    @ConditionalOnMissingBean(MinIOTemplate.class)
    public MinIOTemplate minIOTemplate(MinioClient minioClient,OssProperties ossProperties) {
        return new MinIOTemplate(minioClient,ossProperties);
    }

}
