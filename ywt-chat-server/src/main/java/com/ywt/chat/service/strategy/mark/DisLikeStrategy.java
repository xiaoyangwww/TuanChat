package com.ywt.chat.service.strategy.mark;

import com.ywt.chat.domain.enums.MessageMarkTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月12日 11:49
 */
@Component
public class DisLikeStrategy extends AbstractMsgMarkStrategy{
    @Override
    protected MessageMarkTypeEnum getTypeEnum() {
        return MessageMarkTypeEnum.DISLIKE;
    }

    @Override
    public void doMark(Long uid, Long msgId) {
        super.doMark(uid, msgId);
        //同时取消点赞的动作
        MsgMarkFactory.getStrategyNoNull(MessageMarkTypeEnum.LIKE.getType()).unMark(uid,msgId);
    }
}