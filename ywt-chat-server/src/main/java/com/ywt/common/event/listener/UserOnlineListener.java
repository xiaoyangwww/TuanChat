package com.ywt.common.event.listener;

import com.ywt.chat.service.PushService;
import com.ywt.common.event.UserOnlineEvent;
import com.ywt.user.cache.UserCache;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.entity.IpInfo;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.enums.ChatActiveStatusEnum;
import com.ywt.user.service.IpService;
import com.ywt.user.service.adapter.UserBuilder;
import com.ywt.websocket.service.adapter.WebSocketAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月25日 11:14
 */
@Slf4j
@Component
public class UserOnlineListener {

    @Autowired
    private UserDao userDao;

    @Autowired
    private IpService ipService;

    @Autowired
    private UserCache userCache;

    @Autowired
    private PushService pushService;

    @Autowired
    private WebSocketAdapter webSocketAdapter;

    @Async
    @EventListener(classes = UserOnlineEvent.class)
    public void saveRedisAndPush(UserOnlineEvent event) {
        User user = event.getUser();
        userCache.online(user.getId(),user.getLastOptTime());
        pushService.sendPushMsg(webSocketAdapter.buildOnlineNotifyResp(user));
    }


    @Async
    @EventListener(classes = UserOnlineEvent.class)
    public void saveDB(UserOnlineEvent event) {
        User user = event.getUser();
        User update = UserBuilder.buildOnlineUser(user);
        userDao.updateById(update);
        // 更新用户的ip信息
        ipService.refreshIpDetailAsync(user.getId());

    }
}
