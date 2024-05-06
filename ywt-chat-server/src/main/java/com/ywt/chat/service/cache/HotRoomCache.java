package com.ywt.chat.service.cache;

import com.ywt.common.constant.RedisKey;
import org.springframework.stereotype.Component;
import ywt.chat.common.utils.RedisUtils;

import java.util.Date;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月06日 10:09
 */
@Component
public class HotRoomCache {

    /**
     * 更新热门群聊的最新时间
     */
    public void refreshActiveTime(Long roomId, Date createTime) {
        RedisUtils.zAdd(RedisKey.getKey(RedisKey.HOT_ROOM_ZET),roomId,(double) createTime.getTime());
    }


}
