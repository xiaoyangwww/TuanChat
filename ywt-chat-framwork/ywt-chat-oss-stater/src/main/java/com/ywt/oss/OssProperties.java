package com.ywt.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月10日 10:54
 */
@ConfigurationProperties(prefix = "oss")
@Data
public class OssProperties {
    /**
     * 是否开启
     */
    Boolean enabled;

    /**
     * 存储对象服务器类型
     */
    OssType type;

    /**
     * OSS 访问端点，集群时需提供统一入口
     */
    String endpoint;

    /**
     * 用户名
     */
    String accessKey;

    /**
     * 密码
     */
    String secretKey;

    /**
     * 存储桶
     */
    String bucketName;
}
