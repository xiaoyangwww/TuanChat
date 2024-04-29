package com.ywt.user.service;

import com.ywt.user.domain.enums.RoleEnum;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author ywt
 * @since 2024-04-25
 */
public interface RoleService {

    /**
     * 判断用户是否有权限
     */
    boolean hasPower(Long uid, RoleEnum roleEnum);
}
