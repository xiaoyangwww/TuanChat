package com.ywt.user.service;

import com.ywt.common.domain.vo.Req.CursorPageBaseReq;
import com.ywt.common.domain.vo.Req.PageBaseReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.domain.vo.Resp.PageBaseResp;
import com.ywt.user.domain.vo.Req.friend.FriendApplyReq;
import com.ywt.user.domain.vo.Req.friend.FriendApproveReq;
import com.ywt.user.domain.vo.Req.friend.FriendCheckReq;
import com.ywt.user.domain.vo.Resp.friend.FriendApplyResp;
import com.ywt.user.domain.vo.Resp.friend.FriendCheckResp;
import com.ywt.user.domain.vo.Resp.friend.FriendResp;
import com.ywt.user.domain.vo.Resp.friend.FriendUnreadResp;

/**
 * <p>
 * 用户联系人表 服务类
 * </p>
 *
 * @author ywt
 * @since 2024-04-29
 */
public interface UserFriendService  {

    /**
     * 查询用户的联系人列表
     */
    CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq cursorPageBaseReq);

    /**
     * 批量判断是否是自己好友
     */
    FriendCheckResp check(Long uid, FriendCheckReq checkReq);

    /**
     * 申请好友
     */
    void apply(Long uid, FriendApplyReq request);

    /**
     * 同意好友请求
     * @param uid
     * @param friendApproveReq
     */
    void applyApprove(Long uid, FriendApproveReq friendApproveReq);

    /**
     * 删除好友
     */
    void deleteFriend(Long uid, Long friendUid);

    /**
     * 申请未读数
     * @param uid
     * @return
     */
    FriendUnreadResp unread(Long uid);

    /**
     * 好友申请列表
     */
    PageBaseResp<FriendApplyResp> getPageApplyFriend(Long uid, PageBaseReq request);

}
