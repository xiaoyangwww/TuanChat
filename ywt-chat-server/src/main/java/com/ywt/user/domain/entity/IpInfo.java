package com.ywt.user.domain.entity;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月25日 11:29
 */
@Data
public class IpInfo implements Serializable {
    //注册时的ip
    private String createIp;
    //注册时的ip详情
    private IpDetail createIpDetail;
    //最新登录的ip
    private String updateIp;
    //最新登录的ip详情
    private IpDetail updateIpDetail;

    public void refreshIp(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return;
        }
        updateIp = ip;
        if (createIp == null) {
            createIp = ip;
        }
    }

    public String needRefreshIp() {
        boolean needRefreshIp = Optional.ofNullable(updateIpDetail)
                .map(IpDetail::getIp)
                .filter(ip -> ObjectUtil.equal(updateIp, ip))
                .isPresent();
        return needRefreshIp ? null : updateIp;
    }

    public void updateIpDetail(IpDetail ipDetail) {
        if (Objects.equals(createIp, ipDetail.getIp())) {
            createIpDetail = ipDetail;
        }
        if (Objects.equals(updateIp, ipDetail.getIp())) {
            updateIpDetail = ipDetail;
        }
    }
}
