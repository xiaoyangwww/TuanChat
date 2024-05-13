package com.ywt.common.event.listener;

import com.ywt.chat.service.PushService;
import com.ywt.common.event.UserApplyEvent;
import com.ywt.user.dao.UserApplyDao;
import com.ywt.user.domain.entity.UserApply;
import com.ywt.websocket.domain.vo.message.WSFriendApply;
import com.ywt.websocket.service.adapter.WebSocketAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月01日 16:25
 */
@Component
public class UserApplyListener {

    @Autowired
    private UserApplyDao userApplyDao;

    @Autowired
    private PushService pushService;

    @TransactionalEventListener(classes = UserApplyEvent.class,fallbackExecution = true)
    public void notifyFriend(UserApplyEvent userApplyEvent) {
        UserApply userApply = userApplyEvent.getUserApply();
        // 获取好友未读消息
        Integer count = userApplyDao.getUnReadCount(userApply.getTargetId());
        // 发送通知给好友  mq
        pushService.sendPushMsg(WebSocketAdapter.buildApplySend(new WSFriendApply(userApply.getUid(),count)),userApply.getTargetId());
    }
}
