package com.ywt.user.service;

import com.ywt.user.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author ywt
 * @since 2024-04-15
 */
public interface UserService {

    /**
     * 用户注册
     */
    Long register(User user);
}
