package com.ywt.common.utils;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月21日 15:45
 */
public class NettyUtil {

    public final static AttributeKey<String> TOKEN = AttributeKey.valueOf("token");

    public final static AttributeKey<String> IP = AttributeKey.valueOf("ip");

    public static AttributeKey<Long> UID = AttributeKey.valueOf("uid");

    public static <T> void setAttr(Channel channel, AttributeKey<T> attributeKey, T data) {
        Attribute<T> attr = channel.attr(attributeKey);
        attr.set(data);
    }

    public static <T> T getAttr(Channel channel, AttributeKey<T> attributeKey) {
        Attribute<T> attr = channel.attr(attributeKey);
        return attr.get();
    }
}
