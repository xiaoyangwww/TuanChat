package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.RoomGroup;
import com.ywt.chat.mapper.RoomGroupMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 群聊房间表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
@Service
public class RoomGroupDao extends ServiceImpl<RoomGroupMapper, RoomGroup>  {

    public List<RoomGroup> listByRoomIds(List<Long> roomIds) {
        return lambdaQuery()
                .in(RoomGroup::getRoomId,roomIds)
                .list();
    }

    public RoomGroup getByRoomId(Long roomId) {
        return lambdaQuery().eq(RoomGroup::getRoomId,roomId).one();
    }
}
