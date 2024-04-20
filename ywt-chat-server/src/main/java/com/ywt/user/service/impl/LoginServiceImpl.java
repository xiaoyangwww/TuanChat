package com.ywt.user.service.impl;

import com.ywt.user.service.LoginService;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月20日 12:47
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Override
    public void renewalTokenIfNecessary(String token) {

    }

    @Override
    public String login(Long uid) {
        return null;
    }

    @Override
    public Long getValidUid(String token) {
        return null;
    }

}
