package com.ywt.user.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月25日 11:29
 */
@Data
public class IpDetail implements Serializable {
    //注册时的ip
    private String ip;
    //最新登录的ip
    private String isp;
    private String isp_id;
    private String city;
    private String city_id;
    private String country;
    private String country_id;
    private String region;
    private String region_id;
}
