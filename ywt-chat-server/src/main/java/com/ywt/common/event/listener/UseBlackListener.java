package com.ywt.common.event.listener;

import com.ywt.common.event.UserBlackEvent;
import com.ywt.user.cache.UserCache;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.entity.User;
import com.ywt.websocket.domain.enums.WSRespTypeEnum;
import com.ywt.websocket.domain.vo.message.WSBlack;
import com.ywt.websocket.domain.vo.resp.WSBaseResp;
import com.ywt.websocket.service.WebSocketService;
import com.ywt.websocket.service.adapter.WebSocketAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月25日 11:14
 */
@Slf4j
@Component
public class UseBlackListener {

    @Autowired
    private UserCache userCache;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private UserDao userDao;

    /**
     * 通知使用用户某用户被拉黑
     */
    @Async
    @EventListener(classes = UserBlackEvent.class)
    public void sendAllUser(UserBlackEvent event) {
        User user = event.getUser();
        WSBaseResp<WSBlack> wsBlackWSBaseResp = WebSocketAdapter.buildBlackResp(user);
        webSocketService.sendToAllOnline(wsBlackWSBaseResp, user.getId());
    }

    /**
     * 删除黑名单缓存
     */
    @Async
    @EventListener(classes = UserBlackEvent.class)
    public void deleteBlackMap(UserBlackEvent event) {
       userCache.evictBlackMap();
       userCache.remove(event.getUser().getId());
    }

    /**
     * 将数据库的用户状态改为拉黑
     * @param event
     */
    @Async
    @EventListener(classes = UserBlackEvent.class)
    public void changeUserStatus(UserBlackEvent event) {
        userDao.invalidUid(event.getUser().getId());
    }

}
