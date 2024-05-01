package com.ywt.user.service.adapter;

import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.entity.UserApply;
import com.ywt.user.domain.entity.UserFriend;
import com.ywt.user.domain.enums.ApplyReadStatusEnum;
import com.ywt.user.domain.enums.ApplyStatusEnum;
import com.ywt.user.domain.enums.ApplyTypeEnum;
import com.ywt.user.domain.vo.Req.friend.FriendApplyReq;
import com.ywt.user.domain.vo.Resp.friend.FriendApplyResp;
import com.ywt.user.domain.vo.Resp.friend.FriendResp;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月30日 17:55
 */
public class FriendAdapter {


    public static List<FriendResp> buildFriend(List<UserFriend> list, List<User> userList) {
        Map<Long,User> userMap = userList.stream().collect(Collectors.toMap(User::getId, user -> user));
        return list.stream().map(userFriend -> {
            FriendResp resp = new FriendResp();
            resp.setUid(userFriend.getFriendUid());
            User user = userMap.get(userFriend.getFriendUid());
            if (Objects.nonNull(user)) {
                resp.setActiveStatus(user.getActiveStatus());
            }
            return resp;
        }).collect(Collectors.toList());

    }

    /**
     * 申请好友信息
     * @param uid
     * @param request
     * @return
     */
    public static UserApply buildUserApply(Long uid, FriendApplyReq request) {
        UserApply userApply = new UserApply();
        userApply.setUid(uid);
        userApply.setType(ApplyTypeEnum.ADD_FRIEND.getCode());
        userApply.setTargetId(request.getTargetUid());
        userApply.setMsg(request.getMsg());
        userApply.setStatus(ApplyStatusEnum.WAIT_APPROVAL.getCode());
        userApply.setReadStatus(ApplyReadStatusEnum.UNREAD.getCode());
        return userApply;

    }

    public static List<FriendApplyResp> buildFriendApplyList(List<UserApply> records) {
        return records.stream().map(userApply -> {
            FriendApplyResp friendApplyResp = new FriendApplyResp();
            friendApplyResp.setUid(userApply.getUid());
            friendApplyResp.setType(userApply.getType());
            friendApplyResp.setApplyId(userApply.getId());
            friendApplyResp.setMsg(userApply.getMsg());
            friendApplyResp.setStatus(userApply.getStatus());
            return friendApplyResp;
        }).collect(Collectors.toList());
    }
}
