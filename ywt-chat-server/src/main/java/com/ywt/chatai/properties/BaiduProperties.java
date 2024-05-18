package com.ywt.chatai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "chatai.baidu")
public class BaiduProperties {

    /**
     * 是否使用baiduAI
     */
    private boolean use = false;
    /**
     * 机器人 id
     */
    private Long AIUserId;
    /**
     * 模型名称
     */
    private String modelName = "ERNIE-Tiny-8K";
    /**
     * apiKey
     */
    private String apiKey;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * 获取模型token的url
     */
    private String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s";

    /**
     * 请求url
     */
    private String url = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant?access_token=%s";;

    /**
     * 超时
     */
    private Integer timeout = 60 * 1000;

    /**
     * 用户每小时条数限制
     */
    private Integer limit = 2;

    /**
     * 最大令牌
     */
    private Integer maxTokens = 2048;
}
