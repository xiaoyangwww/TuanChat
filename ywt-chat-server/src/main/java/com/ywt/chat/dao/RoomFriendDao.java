package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.RoomFriend;
import com.ywt.chat.domain.entity.RoomGroup;
import com.ywt.chat.mapper.RoomFriendMapper;
import com.ywt.chat.service.RoomFriendService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
}
