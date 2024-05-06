package com.ywt.chat.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.ywt.chat.dao.RoomDao;
import com.ywt.chat.dao.RoomFriendDao;
import com.ywt.chat.domain.entity.Room;
import com.ywt.chat.domain.entity.RoomFriend;
import com.ywt.chat.domain.enums.RoomTypeEnum;
import com.ywt.chat.service.RoomService;
import com.ywt.chat.service.adapter.ChatAdapter;
import com.ywt.common.domain.enums.NormalOrNoEnum;
import com.ywt.common.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月06日 16:51
 */
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private RoomFriendDao roomFriendDao;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomFriend createRoomFriend(List<Long> uidList) {
        AssertUtil.isNotEmpty(uidList, "房间创建失败，好友数量不对");
        AssertUtil.equal(uidList.size(), 2, "房间创建失败，好友数量不对");
        // 获取聊天室的幂等key
        String key = ChatAdapter.generateRoomKey(uidList);
        RoomFriend roomFriend = roomFriendDao.getByRoomKey(key);
        if (ObjectUtil.isNotNull(roomFriend)) {
            // 如果存在房间就恢复，适用于恢复好友场景
            restoreRoomIfNeed(roomFriend);
        } else {
            Room room = createRoom(RoomTypeEnum.FRIEND);
            roomFriend = createRoomFriend(room.getId(), uidList);
        }
        return roomFriend;
    }

    @Override
    public void disableChat(List<Long> uidList) {
        AssertUtil.isNotEmpty(uidList, "房间禁用失败，好友数量不对");
        AssertUtil.equal(uidList.size(), 2, "房间禁用失败，好友数量不对");
        RoomFriend roomFriend = roomFriendDao.getByRoomKey(ChatAdapter.generateRoomKey(uidList));
        if (ObjectUtil.isNull(roomFriend)){
            return;
        }
        // 禁用房间
        roomFriendDao.disableRoom(roomFriend.getId());
    }

    private void restoreRoomIfNeed(RoomFriend roomFriend) {
        if (roomFriend.getStatus().equals(NormalOrNoEnum.NOT_NORMAL.getStatus())) {
            roomFriendDao.restoreRoom(roomFriend.getId());
        }
    }

    public RoomFriend createRoomFriend(Long roomId, List<Long> uidList) {
        RoomFriend roomFriend = ChatAdapter.buildRoomFriend(roomId, uidList);
        roomFriendDao.save(roomFriend);
        return roomFriend;
    }


    private Room createRoom(RoomTypeEnum roomTypeEnum) {
        Room room = ChatAdapter.buildRoom(roomTypeEnum);
        roomDao.save(room);
        return room;

    }
}
