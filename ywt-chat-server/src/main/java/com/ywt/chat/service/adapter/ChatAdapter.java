package com.ywt.chat.service.adapter;

import com.ywt.chat.domain.entity.Room;
import com.ywt.chat.domain.entity.RoomFriend;
import com.ywt.chat.domain.enums.HotFlagEnum;
import com.ywt.chat.domain.enums.MessageStatusEnum;
import com.ywt.chat.domain.enums.RoomTypeEnum;
import com.ywt.common.domain.enums.NormalOrNoEnum;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月06日 17:05
 */
public class ChatAdapter {

    public static final String SEPARATOR = ",";

    /**
     * 获取聊天室的幂等key
     * @param uidList
     * @return
     */
    public static String generateRoomKey(List<Long> uidList) {
        return uidList.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(SEPARATOR));
    }

    public static Room buildRoom(RoomTypeEnum roomTypeEnum) {
        Room room = new Room();
        room.setType(roomTypeEnum.getType());
        room.setHotFlag(HotFlagEnum.NOT.getType());
        return room;
    }

    public static RoomFriend buildRoomFriend(Long roomId, List<Long> uidList) {
        String key = generateRoomKey(uidList);
        RoomFriend roomFriend = new RoomFriend();
        roomFriend.setRoomId(roomId);
        roomFriend.setUid1(uidList.get(0));
        roomFriend.setUid2(uidList.get(1));
        roomFriend.setRoomKey(key);
        roomFriend.setStatus(NormalOrNoEnum.NORMAL.getStatus());
        return roomFriend;
    }

    public static Set<Long> getFriendUidSet(Collection<RoomFriend> values, Long uid) {
        return values.stream().map(item -> getFriendUid(item, uid)).collect(Collectors.toSet());
    }

    public static Long getFriendUid(RoomFriend roomFriend, Long uid) {
        return uid.equals(roomFriend.getUid1()) ? roomFriend.getUid2() : roomFriend.getUid1();
    }
}
