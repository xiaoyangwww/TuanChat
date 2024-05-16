package com.ywt.common.event.listener;

import com.ywt.chat.service.PushService;
import com.ywt.common.event.UserOfflineEvent;
import com.ywt.user.cache.UserCache;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.enums.ChatActiveStatusEnum;
import com.ywt.websocket.service.WebSocketService;
import com.ywt.websocket.service.adapter.WebSocketAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月14日 11:55
 */
@Component
public class UserOfflineListener {

    @Autowired
    private UserCache userCache;

    @Autowired
    private UserDao userDao;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private WebSocketAdapter webSocketAdapter;

    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void saveRedisAndPush(UserOfflineEvent event) {
        User user = event.getUser();
        // 更新
        userCache.offline(user.getId(),user.getLastOptTime());
        //推送给所有在线用户，该用户下线
        webSocketService.sendToAllOnline(webSocketAdapter.buildOfflineNotifyResp(event.getUser()), event.getUser().getId());
    }

    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void saveDB(UserOfflineEvent event) {
        User user = event.getUser();
        User update = new User();
        update.setId(user.getId());
        update.setLastOptTime(user.getLastOptTime());
        update.setActiveStatus(ChatActiveStatusEnum.OFFLINE.getType());
        userDao.updateById(update);
    }
}
