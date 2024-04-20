package com.ywt.user.service.adapter;

import com.ywt.user.domain.entity.User;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月20日 11:05
 */
public class UserBuilder {

    public static User build(String openId) {
        return User.builder().openId(openId).build();
    }

    public static User build(Long userId, WxOAuth2UserInfo userInfo) {
        String openid = userInfo.getOpenid();
        String nickname = userInfo.getNickname();
        Integer sex = userInfo.getSex();
        String headImgUrl = userInfo.getHeadImgUrl();
        return User.builder()
                .id(userId)
                .name(nickname)
                .openId(openid)
                .sex(sex)
                .avatar(headImgUrl)
                .build();
    }
}
