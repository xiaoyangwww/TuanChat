package com.ywt.user.service.impl;

import com.ywt.user.cache.UserCache;
import com.ywt.user.domain.enums.RoleEnum;
import com.ywt.user.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月27日 11:02
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private UserCache userCache;

    @Override
    public boolean hasPower(Long uid, RoleEnum roleEnum) {
        Set<Long> roleSet = userCache.getRoleSet(uid);
        return isAdmin(roleSet) || roleSet.contains(roleEnum.getType());
    }

    //超级管理员
    private boolean isAdmin(Set<Long> roleSet) {
        return Objects.requireNonNull(roleSet).contains(RoleEnum.ADMIN.getType());
    }


}
