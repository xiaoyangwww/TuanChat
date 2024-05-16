package com.ywt.chat.service;

import com.ywt.chat.domain.entity.RoomFriend;
import com.ywt.chat.domain.entity.RoomGroup;
import com.ywt.chat.domain.vo.Resp.ChatRoomResp;
import com.ywt.common.domain.vo.Req.CursorPageBaseReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;

import java.util.List;

/**
 * <p>
 * 房间表 服务类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
public interface RoomService {

    /**
     * 创建单聊聊天室
     * @param uids
     * @return
     */
    RoomFriend createRoomFriend(List<Long> uids);

    /**
     * 禁用房间
     * @param uidList
     */
    void disableChat(List<Long> uidList);

    /**
     * 会话列表
     * @param request
     * @param uid
     * @return
     */
    CursorPageBaseResp<ChatRoomResp> getContactPage(CursorPageBaseReq request, Long uid);

    /**
     * 获取会话详情
     * @param uid
     * @param id
     * @return
     */
    ChatRoomResp getContactDetail(Long uid, long id);

    /**
     * 联系人列表发消息用
     * @param uid
     * @param friendId
     * @return
     */
    ChatRoomResp getContactDetailByFriend(Long uid, Long friendId);

    RoomFriend getFriendRoom(Long uid, Long friendUid);

    /**
     * 创建一个群聊
     * @param uid
     * @return
     */
    RoomGroup createGroupRoom(Long uid);
}
