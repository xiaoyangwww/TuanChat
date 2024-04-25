package com.ywt.user.service;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月25日 11:56
 */
public interface IpService {
    /**
     * 更新用户的ip信息
     */
    void refreshIpDetailAsync(Long uid);
}
