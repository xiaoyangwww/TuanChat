package com.ywt.chat.service;

import com.ywt.chat.domain.vo.Req.ChatMessageMemberReq;
import com.ywt.chat.domain.vo.Req.member.MemberAddReq;
import com.ywt.chat.domain.vo.Req.member.MemberDelReq;
import com.ywt.chat.domain.vo.Req.member.MemberReq;
import com.ywt.chat.domain.vo.Resp.ChatMemberListResp;
import com.ywt.chat.domain.vo.Resp.GroupAddReq;
import com.ywt.chat.domain.vo.Resp.MemberResp;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.websocket.domain.vo.message.ChatMemberResp;

import java.util.List;

public interface RoomAppService {

    /**
     * 群组详情
     * @param uid
     * @param roomId
     * @return
     */
    MemberResp getGroupDetail(Long uid, long roomId);


    /**
     * 房间内的所有群成员列表
     *
     * @param uid
     * @param request
     * @return
     */
    List<ChatMemberListResp> getMemberList(Long uid, ChatMessageMemberReq request);

    /**
     * 移除成员
     * @param uid
     * @param request
     */
    void delMember(Long uid, MemberDelReq request);

    /**
     * 新增群组
     * @param uid
     * @param request
     * @return
     */
    Long addGroup(Long uid, GroupAddReq request);

    /**
     * 邀请好友
     * @param uid
     * @param request
     */
    void addMember(Long uid, MemberAddReq request);

    /**
     * 群成员列表
     * @param request
     * @return
     */
    CursorPageBaseResp<ChatMemberResp> getMemberPage(MemberReq request);

}
