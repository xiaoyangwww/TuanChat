package com.ywt.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.ywt.common.annotation.RedissonLock;
import com.ywt.common.domain.enums.YesOrNoEnum;
import com.ywt.common.service.LockService;
import com.ywt.common.utils.AssertUtil;
import com.ywt.user.dao.ItemConfigDao;
import com.ywt.user.dao.UserBackpackDao;
import com.ywt.user.domain.entity.ItemConfig;
import com.ywt.user.domain.entity.UserBackpack;
import com.ywt.user.domain.enums.IdempotentEnum;
import com.ywt.user.domain.enums.ItemTypeEnum;
import com.ywt.user.service.UserBackpackService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月23日 15:15
 */
@Service
public class UserBackpackServiceImpl implements UserBackpackService {

    @Autowired
    private UserBackpackDao userBackpackDao;

    @Autowired
    private ItemConfigDao itemConfigDao;

    @Autowired
    @Lazy
    private UserBackpackServiceImpl userBackpackService;


    @Override
    public void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        String idempotent = getIdempotent(itemId, idempotentEnum, businessId);
        userBackpackService.doAcquireItem(uid, itemId, idempotent);
    }

    @RedissonLock(key = "#idempotent",waitTime = 5000)//相同幂等如果同时发奖，需要排队等上一个执行完，取出之前数据返回
    public void doAcquireItem(Long uid, Long itemId, String idempotent) {
        // 判断该幂等是否存在
        UserBackpack userBackpack = userBackpackDao.getIdempotent(idempotent);
        if (ObjectUtil.isNotNull(userBackpack)) {
            return;
        }
        // 业务检查,勋章只能颁发一次
        ItemConfig itemConfig = itemConfigDao.getById(itemId);
        if (ItemTypeEnum.BADGE.getType().equals(itemConfig.getType())) {
            Integer count = userBackpackDao.getCountByValidItem(uid, itemId);
            if (count > 0) {
                return;
            }
        }
        // 发放物品
        UserBackpack userBack = UserBackpack.builder()
                .itemId(itemId)
                .uid(uid)
                .idempotent(idempotent)
                .status(YesOrNoEnum.NO.getCode())
                .build();
        userBackpackDao.save(userBack);
    }


    private String getIdempotent(Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        String idempotent = "%d_%d_%s";
        return String.format(idempotent, itemId, idempotentEnum.getType(), businessId);

    }


}
