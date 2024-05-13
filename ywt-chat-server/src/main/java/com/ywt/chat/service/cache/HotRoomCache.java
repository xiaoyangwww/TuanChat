package com.ywt.chat.service.cache;

import cn.hutool.core.lang.Pair;
import com.ywt.common.constant.RedisKey;
import com.ywt.common.domain.vo.Req.CursorPageBaseReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.utils.CursorUtils;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import ywt.chat.common.utils.RedisUtils;

import java.util.Date;
import java.util.Set;

/**
 * 功能描述
 *
 * @author: ywt
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

    /**
     * 获取热门房间
     */
    public Set<ZSetOperations.TypedTuple<String>> getRoomRange(Double hotStart, Double hotEnd) {
        return RedisUtils.zRangeByScoreWithScores(RedisKey.getKey(RedisKey.HOT_ROOM_ZET),hotStart,hotEnd);
    }
    /**
     * 获取热门群聊翻页
     *
     * @return
     */
    public CursorPageBaseResp<Pair<Long, Double>> getRoomCursorPage(CursorPageBaseReq request) {
        return CursorUtils.getCursorPageByRedis(request,RedisKey.getKey(RedisKey.HOT_ROOM_ZET),Long::parseLong);
    }

}
