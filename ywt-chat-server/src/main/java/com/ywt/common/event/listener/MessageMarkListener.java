package com.ywt.common.event.listener;

import com.ywt.chat.dao.MessageDao;
import com.ywt.chat.dao.MessageMarkDao;
import com.ywt.chat.domain.dto.ChatMessageMarkDTO;
import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.domain.enums.MessageMarkTypeEnum;
import com.ywt.chat.domain.enums.MessageTypeEnum;
import com.ywt.chat.service.PushService;
import com.ywt.chat.service.cache.MsgCache;
import com.ywt.common.event.MessageMarkEvent;
import com.ywt.user.domain.entity.UserBackpack;
import com.ywt.user.domain.enums.IdempotentEnum;
import com.ywt.user.domain.enums.ItemEnum;
import com.ywt.user.service.UserBackpackService;
import com.ywt.websocket.service.adapter.WebSocketAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月12日 11:17
 */
@Component
public class MessageMarkListener {

    @Autowired
    private MessageMarkDao messageMarkDao;

    @Autowired
    private PushService pushService;

    @Autowired
    private MsgCache msgCache;

    @Autowired
    private UserBackpackService userBackpackService;

    /**
     * 发放徽章
     * @param event
     */
    @Async
    @TransactionalEventListener(classes = MessageMarkEvent.class, fallbackExecution = true)
    public void changeMsgType(MessageMarkEvent event) {
        ChatMessageMarkDTO dto = event.getDto();
        Message msg = msgCache.getMsg(dto.getMsgId());
        if (!msg.getType().equals(MessageTypeEnum.TEXT.getType())) {
            return;
        }
        //消息被标记次数
        Integer markCount = messageMarkDao.getMarkCount(dto.getMsgId(), dto.getMarkType());
        MessageMarkTypeEnum messageMarkTypeEnum = MessageMarkTypeEnum.of(dto.getMarkType());
        if (markCount < messageMarkTypeEnum.getRiseNum()) {
            return;
        }
        if (MessageMarkTypeEnum.LIKE.equals(messageMarkTypeEnum)) {
            userBackpackService.acquireItem(dto.getUid(), ItemEnum.LIKE_BADGE.getId(), IdempotentEnum.MSG_ID,msg.getId().toString());
        }
    }

    /**
     * 通知所有人
     * @param event
     */
    @Async
    @TransactionalEventListener(classes = MessageMarkEvent.class, fallbackExecution = true)
    public void notifyAll(MessageMarkEvent event) {//后续可做合并查询，目前异步影响不大
        ChatMessageMarkDTO dto = event.getDto();
        Integer count = messageMarkDao.getMarkCount(dto.getMsgId(),dto.getMarkType());
        pushService.sendPushMsg(WebSocketAdapter.buildMarkMsg(dto,count));

    }
}
