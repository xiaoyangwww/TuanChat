package com.ywt.user.service;

import com.ywt.user.domain.entity.UserBackpack;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ywt.user.domain.enums.IdempotentEnum;

/**
 * <p>
 * 用户背包表 服务类
 * </p>
 *
 * @author ywt
 * @since 2024-04-21
 */
public interface UserBackpackService  {
    /**
     * 用户获取一个物品
     *
     * @param uid            用户id
     * @param itemId         物品id
     * @param idempotentEnum 幂等类型
     * @param businessId     上层业务发送的唯一标识
     */
    void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String businessId);

}
