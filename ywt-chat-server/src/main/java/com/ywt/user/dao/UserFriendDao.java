package com.ywt.user.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.ywt.common.domain.enums.YesOrNoEnum;
import com.ywt.common.domain.vo.Req.CursorPageBaseReq;
import com.ywt.common.domain.vo.Req.PageBaseReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.utils.CursorUtils;
import com.ywt.user.domain.entity.UserApply;
import com.ywt.user.domain.entity.UserFriend;
import com.ywt.user.mapper.UserFriendMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户联系人表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-04-29
 */
@Service
public class UserFriendDao extends ServiceImpl<UserFriendMapper, UserFriend> {

    public CursorPageBaseResp<UserFriend> getFriendPage(Long uid, CursorPageBaseReq cursorPageBaseReq) {
        return CursorUtils.getCursorPageByMysql(this, cursorPageBaseReq,
                wrapper -> wrapper.eq(UserFriend::getUid, uid)
                        .eq(UserFriend::getDeleteStatus, YesOrNoEnum.NO.getCode()),
                UserFriend::getId);

    }

    public List<UserFriend> getFriendList(Long uid, List<Long> uidList) {
        return lambdaQuery().eq(UserFriend::getUid, uid)
                .in(UserFriend::getFriendUid,uidList)
                .list();

    }

    public UserFriend getFriend(Long targetUid, Long uid) {
        return lambdaQuery().eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, targetUid)
                .one();

    }


    public List<UserFriend> getUserFriend(Long uid, Long friendUid) {
        return lambdaQuery()
                .eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, friendUid)
                .or()
                .eq(UserFriend::getUid, friendUid)
                .eq(UserFriend::getFriendUid, uid)
                .select(UserFriend::getId)
                .list();
    }


}
