package com.ywt.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.ywt.common.event.UserBlackEvent;
import com.ywt.common.event.UserRegisterEvent;
import com.ywt.common.utils.AssertUtil;
import com.ywt.common.utils.RequestHolder;
import com.ywt.user.cache.ItemCache;
import com.ywt.user.dao.BlackDao;
import com.ywt.user.dao.UserBackpackDao;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.entity.*;
import com.ywt.user.domain.enums.BlackTypeEnum;
import com.ywt.user.domain.enums.ItemEnum;
import com.ywt.user.domain.enums.ItemTypeEnum;
import com.ywt.user.domain.enums.RoleEnum;
import com.ywt.user.domain.vo.Req.user.ModifyNameReq;
import com.ywt.user.domain.vo.Resp.user.BadgeResp;
import com.ywt.user.domain.vo.Resp.user.UserInfoResp;
import com.ywt.user.service.RoleService;
import com.ywt.user.service.UserService;
import com.ywt.user.service.adapter.UserBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月20日 11:01
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBackpackDao userBackpackDao;

    @Autowired
    private ItemCache itemCache;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private RoleService roleService;

    @Autowired
    private BlackDao blackDao;



    @Override
    public Long register(User user) {
        userDao.save(user);
        // 发送用户注册事件
        applicationEventPublisher.publishEvent(new UserRegisterEvent(this,user));
        return user.getId();
    }

    @Override
    public UserInfoResp getUserInfo(Long uid) {
        User user = userDao.getById(uid);
        UserInfoResp userInfoResp = null;
        if (ObjectUtil.isNotNull(user)) {
            // 剩余改名次数
            Integer modifyNameChance = userBackpackDao.getCountByValidItem(uid, ItemEnum.MODIFY_NAME_CARD.getId());
            userInfoResp = UserBuilder.buildUserInfo(user,modifyNameChance);
        }
        return userInfoResp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyName(ModifyNameReq modifyNameReq) {
        String newName = modifyNameReq.getName();
        Long uid = RequestHolder.get().getUid();
        // 判断名字中有没有敏感词
//        AssertUtil.isFalse(sensitiveWordBs.hasSensitiveWord(modifyNameReq.getName()), "名字中包含敏感词，请重新输入");
        // 判断用户名是否重复
        User user = userDao.getUserByName(modifyNameReq.getName());
        AssertUtil.isEmpty(user,"名字已经被抢占了，请换一个哦~~");
        // 改名卡是否使用过
        UserBackpack userBackpack = userBackpackDao.getModifyCardByValidItem(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        AssertUtil.isNotEmpty(userBackpack,"改名次数不够了，等后续活动送改名卡哦");
        // 使用改名卡
        boolean isSuccess = userBackpackDao.useModifyCard(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        if (isSuccess) {
            // 改名
            userDao.updateName(newName,uid);
        }

    }

    @Override
    public List<BadgeResp> badges(Long uid) {
        // 获取全部徽章
        List<ItemConfig> itemConfigList = itemCache.getByType(ItemTypeEnum.BADGE.getType());
        // 获取用户背包里拥有的徽章
        List<UserBackpack> userBackpackList = userBackpackDao.getUserBadges(uid,itemConfigList);
        // 获取用户佩戴的徽章
        User user = userDao.getById(uid);
        // 封装
        return UserBuilder.buildBadgeResp(user,userBackpackList,itemConfigList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void black(Long uid) {
        User user = userDao.getById(uid);
        boolean hasPower = roleService.hasPower(uid, RoleEnum.ADMIN);
        AssertUtil.isTrue(hasPower,"没有权限哦！");

        Black userBlack = new Black();
        // 拉黑用户id
        userBlack.setType(BlackTypeEnum.UID.getType());
        userBlack.setTarget(uid.toString());
        blackDao.save(userBlack);
        // 拉黑用户ip
        IpInfo ipInfo = user.getIpInfo();
        if (ipInfo.getCreateIp().equals(ipInfo.getUpdateIp())) {
            Black ipBlack = blackIP(ipInfo.getCreateIp());
            blackDao.save(ipBlack);
        }else {
            Black createIpBlack = blackIP(ipInfo.getCreateIp());
            Black updateIpBlack = blackIP(ipInfo.getUpdateIp());
            blackDao.save(createIpBlack);
            blackDao.save(updateIpBlack);
        }
        // 发送用户被拉黑的消息
        applicationEventPublisher.publishEvent(new UserBlackEvent(this,user));

    }

    private Black blackIP(String ip) {
        Black black = new Black();
        black.setType(BlackTypeEnum.IP.getType());
        black.setTarget(ip);
        return black;
    }
}
