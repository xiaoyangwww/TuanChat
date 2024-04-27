package com.ywt.user.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ywt.common.domain.enums.YesOrNoEnum;
import com.ywt.user.domain.entity.User;
import com.ywt.user.mapper.UserMapper;
import com.ywt.user.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-04-15
 */
@Service
public class UserDao extends ServiceImpl<UserMapper, User> {

    public User getUserByOpenId(String openId) {
        return lambdaQuery().eq(User::getOpenId, openId).one();
    }

    public User getUserByName(String name) {
        return lambdaQuery().eq(User::getName, name).one();
    }


    public void updateName(String newName, Long uid) {
        lambdaUpdate()
                .eq(User::getId,uid)
                .set(User::getName,newName)
                .update();
    }

    /**
     * 将数据库的用户状态改为拉黑
     */
    public void invalidUid(Long id) {
        lambdaUpdate().eq(User::getId,id)
                .set(User::getStatus, YesOrNoEnum.YES.getCode())
                .update();
    }
}
