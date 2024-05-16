package com.ywt.user.dao;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ywt.common.domain.enums.NormalOrNoEnum;
import com.ywt.common.domain.enums.YesOrNoEnum;
import com.ywt.common.domain.vo.Req.CursorPageBaseReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.utils.CursorUtils;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.enums.ChatActiveStatusEnum;
import com.ywt.user.mapper.UserMapper;
import com.ywt.user.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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
                .eq(User::getId, uid)
                .set(User::getName, newName)
                .update();
    }

    /**
     * 将数据库的用户状态改为拉黑
     */
    public void invalidUid(Long id) {
        lambdaUpdate().eq(User::getId, id)
                .set(User::getStatus, YesOrNoEnum.YES.getCode())
                .update();
    }

    /**
     * 获取好友列表
     */
    public List<User> getFriendList(List<Long> friendUids) {
        return lambdaQuery()
                .in(User::getId, friendUids)
                .select(User::getId, User::getActiveStatus, User::getName, User::getAvatar)
                .list();
    }

    /**
     * 佩戴徽章
     *
     * @param uid
     * @param itemId
     */
    public void wearingBadge(Long uid, Long itemId) {
        lambdaUpdate()
                .set(User::getItemId, itemId)
                .eq(User::getId, uid)
                .update();
    }

    public Long getOnlineNum(List<Long> members) {
        return lambdaQuery()
                .eq(User::getActiveStatus, ChatActiveStatusEnum.ONLINE.getType())
                .in(CollectionUtil.isNotEmpty(members), User::getId, members)
                .count().longValue();
    }

    public List<User> getMemberList() {
        return lambdaQuery()
                .eq(User::getStatus, NormalOrNoEnum.NORMAL.getStatus())
                .orderByDesc(User::getLastOptTime)//最近活跃的1000个人，可以用lastOptTime字段，但是该字段没索引，updateTime可平替
                .last("limit 1000")//毕竟是大群聊，人数需要做个限制
                .select(User::getId, User::getName, User::getAvatar)
                .list();

    }

    public CursorPageBaseResp<User> getCursorPage(List<Long> memberUidList, CursorPageBaseReq request, ChatActiveStatusEnum online) {
        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
            wrapper.eq(User::getActiveStatus, online.getType());//筛选上线或者离线的
            wrapper.in(CollectionUtils.isNotEmpty(memberUidList), User::getId, memberUidList);//普通群对uid列表做限制
        }, User::getLastOptTime);
    }
}
