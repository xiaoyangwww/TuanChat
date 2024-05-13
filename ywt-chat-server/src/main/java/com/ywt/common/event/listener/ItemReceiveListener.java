package com.ywt.common.event.listener;

import cn.hutool.core.util.ObjectUtil;
import com.ywt.common.event.ItemReceiveEvent;
import com.ywt.user.cache.UserCache;
import com.ywt.user.dao.ItemConfigDao;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.entity.ItemConfig;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.entity.UserBackpack;
import com.ywt.user.domain.enums.ItemTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月02日 9:59
 */
@Component
public class ItemReceiveListener {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ItemConfigDao itemConfigDao;

    @Autowired
    private UserCache userCache;


    /**
     * 徽章类型，帮忙默认佩戴
     *
     * @param event
     */
    @Async
    @EventListener(classes = ItemReceiveEvent.class)
    public void wear(ItemReceiveEvent event) {
        UserBackpack userBackpack = event.getUserBackpack();

        Long itemId = userBackpack.getItemId();
        ItemConfig itemConfig = itemConfigDao.getById(itemId);
        if (ItemTypeEnum.BADGE.getType().equals(itemConfig.getType())) {
            Long uid = userBackpack.getUid();
            User user = userDao.getById(uid);
            if (ObjectUtil.isNull(user.getItemId())) {
                userDao.wearingBadge(uid,itemId);
                //删除缓存
                userCache.userInfoChange(uid);
            }
        }

    }
}
