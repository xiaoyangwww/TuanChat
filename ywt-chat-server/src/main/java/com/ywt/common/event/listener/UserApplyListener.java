package com.ywt.common.event.listener;

import com.ywt.common.event.UserApplyEvent;
import com.ywt.user.dao.UserApplyDao;
import com.ywt.user.domain.entity.UserApply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月01日 16:25
 */
@Component
public class UserApplyListener {

    @Autowired
    private UserApplyDao userApplyDao;

    @TransactionalEventListener(classes = UserApplyEvent.class,fallbackExecution = true)
    public void notifyFriend(UserApplyEvent userApplyEvent) {
        UserApply userApply = userApplyEvent.getUserApply();
        // 获取好友未读消息
        Integer count = userApplyDao.getUnReadCount(userApply.getTargetId());
        // TODO 发送通知给好友  mq

    }
}
