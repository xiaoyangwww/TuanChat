package com.ywt.chat.service;

import com.ywt.chat.domain.entity.RoomFriend;

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
}
