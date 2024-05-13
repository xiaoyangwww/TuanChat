package com.ywt.common.event.listener;

import com.ywt.common.event.UserOnlineEvent;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.entity.IpInfo;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.enums.ChatActiveStatusEnum;
import com.ywt.user.service.IpService;
import com.ywt.user.service.adapter.UserBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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



    @Async
    @TransactionalEventListener(classes = UserOnlineEvent.class, fallbackExecution = true)
    public void saveDB(UserOnlineEvent event) {
        User user = event.getUser();
        User update = UserBuilder.buildOnlineUser(user);
        userDao.updateById(update);
        // 更新用户的ip信息
        ipService.refreshIpDetailAsync(user.getId());

    }
}
