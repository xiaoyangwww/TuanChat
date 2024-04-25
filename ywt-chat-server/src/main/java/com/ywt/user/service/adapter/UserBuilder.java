package com.ywt.user.service.adapter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.ywt.common.domain.enums.YesOrNoEnum;
import com.ywt.user.domain.entity.ItemConfig;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.entity.UserBackpack;
import com.ywt.user.domain.enums.ChatActiveStatusEnum;
import com.ywt.user.domain.vo.Resp.user.BadgeResp;
import com.ywt.user.domain.vo.Resp.user.UserInfoResp;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月20日 11:05
 */
public class UserBuilder {

    public static User buildUser(String openId) {
        return User.builder().openId(openId).build();
    }

    public static User buildAuthorizeUser(Long userId, WxOAuth2UserInfo userInfo) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(userInfo.getHeadImgUrl());
        user.setName(userInfo.getNickname());
        user.setSex(userInfo.getSex());
        if (userInfo.getNickname().length() > 6) {
            user.setName("名字过长" + RandomUtil.randomInt(100000));
        } else {
            user.setName(userInfo.getNickname());
        }
        return user;
    }

    public static UserInfoResp buildUserInfo(User user,Integer modifyNameChance) {
        UserInfoResp userInfoResp = new UserInfoResp();
        BeanUtil.copyProperties(user,userInfoResp,true);
        userInfoResp.setModifyNameChance(modifyNameChance);
        return userInfoResp;
    }

    public static List<BadgeResp> buildBadgeResp(User user, List<UserBackpack> userBackpackList, List<ItemConfig> itemConfigList) {
        if (ObjectUtil.isNull(user)) {
            // 这里 user 入参可能为空，防止 NPE 问题
            return Collections.emptyList();
        }
        Set<Long> userHasItems = userBackpackList.stream().map(UserBackpack::getItemId).collect(Collectors.toSet());

        return itemConfigList.stream().map(item -> {
            BadgeResp badgeResp = new BadgeResp();
            BeanUtil.copyProperties(item, badgeResp);
            badgeResp.setObtain(userHasItems.contains(item.getId()) ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
            badgeResp.setWearing(ObjectUtil.equal(user.getItemId(), item.getId()) ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
            return badgeResp;
        }).sorted(Comparator.comparing(BadgeResp::getWearing, Comparator.reverseOrder())
                .thenComparing(BadgeResp::getObtain, Comparator.reverseOrder())).collect(Collectors.toList());


    }

    public static User buildOnlineUser(User user) {
        User update = new User();
        update.setId(user.getId());
        update.setIpInfo(user.getIpInfo());
        update.setLastOptTime(user.getLastOptTime());
        update.setActiveStatus(ChatActiveStatusEnum.ONLINE.getType());
        return update;
    }
}
