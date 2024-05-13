package com.ywt.common.event.listener;

import com.ywt.common.event.UserRegisterEvent;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.enums.IdempotentEnum;
import com.ywt.user.domain.enums.ItemEnum;
import com.ywt.user.domain.enums.ItemTypeEnum;
import com.ywt.user.service.UserBackpackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月24日 15:24
 */
@Component
@Slf4j
public class UserRegisterListener {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBackpackService userBackpackService;

    /**
     * 用户注册时，发送改名卡
     * @param event
     */
    @Async
    @EventListener(UserRegisterEvent.class)
    public void sendCard(UserRegisterEvent event) {
        User user = event.getUser();
        userBackpackService.acquireItem(user.getId(), ItemEnum.MODIFY_NAME_CARD.getId(), IdempotentEnum.UID, user.getId().toString());
    }

    /**
     * 前10或前100的用户注册，发送徽章
     * @param event
     */
    @Async
    @EventListener(UserRegisterEvent.class)
    public void sendBadge(UserRegisterEvent event) {
        User user = event.getUser();
        int count = userDao.count();
        if (count <= 13 ) {
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP10_BADGE.getId(), IdempotentEnum.UID, user.getId().toString());
        }else if (count <= 103) {
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP100_BADGE.getId(), IdempotentEnum.UID, user.getId().toString());
        }
    }
}
