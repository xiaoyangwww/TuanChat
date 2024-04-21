package com.ywt.common.constant;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月20日 17:00
 */
public class RedisKey {

    private static final String BASE_KEY = "chat:";

    /**
     * 用户token存放
     */
    public static final String USER_TOKEN_STRING = "userToken:uid_%d";

    public static String getKey(String key,Object ...objects) {
        return String.format(key,objects);
    }

}
