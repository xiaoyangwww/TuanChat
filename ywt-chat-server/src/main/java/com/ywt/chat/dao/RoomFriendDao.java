package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.RoomFriend;
import com.ywt.chat.mapper.RoomFriendMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywt.common.domain.enums.NormalOrNoEnum;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 单聊房间表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
@Service
public class RoomFriendDao extends ServiceImpl<RoomFriendMapper, RoomFriend> {

    public List<RoomFriend> listByRoomIds(List<Long> roomIds) {
        return lambdaQuery()
                .in(RoomFriend::getRoomId,roomIds)
                .list();
    }

    public RoomFriend getByRoomId(Long roomId) {
        return lambdaQuery()
                .eq(RoomFriend::getRoomId, roomId)
                .one();
    }

    public RoomFriend getByRoomKey(String key) {
        return lambdaQuery()
                .eq(RoomFriend::getRoomKey, key)
                .one();
    }

    public void restoreRoom(Long id) {
        lambdaUpdate()
                .set(RoomFriend::getStatus, NormalOrNoEnum.NORMAL.getStatus())
                .eq(RoomFriend::getId,id)
                .update();
    }

    public void disableRoom(Long id) {
        lambdaUpdate()
                .set(RoomFriend::getStatus, NormalOrNoEnum.NOT_NORMAL.getStatus())
                .eq(RoomFriend::getId,id)
                .update();
    }

    public RoomFriend get(Long roomId) {
        return lambdaQuery()
                .eq(RoomFriend::getRoomId, roomId)
                .one();
    }
}
