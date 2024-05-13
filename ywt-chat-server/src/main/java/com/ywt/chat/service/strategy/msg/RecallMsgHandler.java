package com.ywt.chat.service.strategy.msg;


import com.ywt.chat.dao.MessageDao;
import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.domain.entity.msg.MessageExtra;
import com.ywt.chat.domain.entity.msg.MsgRecall;
import com.ywt.chat.domain.enums.MessageTypeEnum;
import com.ywt.common.event.MessageRecallEvent;
import com.ywt.user.cache.UserCache;
import com.ywt.chat.domain.dto.ChatMsgRecallDTO;
import com.ywt.user.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * Description: 撤回文本消息
 * Author: ywt
 * Date: 2023-06-04
 */
@Component
public class RecallMsgHandler extends AbstractMsgHandler<Object> {
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private UserCache userCache;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.RECALL;
    }

    @Override
    public void saveMsg(Message msg, Object body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object showMsg(Message msg) {
        MsgRecall recall = msg.getExtra().getRecall();
        User userInfo = userCache.getUserInfo(recall.getRecallUid());
        if (!Objects.equals(recall.getRecallUid(), msg.getFromUid())) {
            return "管理员\"" + userInfo.getName() + "\"撤回了一条成员消息";
        }
        return "\"" + userInfo.getName() + "\"撤回了一条消息";
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return "原消息已被撤回";
    }

    public void recall(Long recallUid, Message message) {//todo 消息覆盖问题用版本号解决
        MessageExtra extra = message.getExtra();
        extra.setRecall(new MsgRecall(recallUid, new Date()));
        Message update = new Message();
        update.setId(message.getId());
        update.setType(MessageTypeEnum.RECALL.getType());
        update.setExtra(extra);
        messageDao.updateById(update);
        applicationEventPublisher.publishEvent(new MessageRecallEvent(this, new ChatMsgRecallDTO(message.getId(), message.getRoomId(), recallUid)));

    }

    @Override
    public String showContactMsg(Message msg) {
        return "撤回了一条消息";
    }
}
