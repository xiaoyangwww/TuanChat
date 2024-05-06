package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.Room;
import com.ywt.chat.mapper.RoomMapper;
import com.ywt.chat.service.RoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 房间表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
@Service
public class RoomDao extends ServiceImpl<RoomMapper, Room>  {

    /**
     * 更新房间最新消息
     */
    public void refreshActiveTime(Long roomId, Long msgId, Date createTime) {
        lambdaUpdate()
                .eq(Room::getId,roomId)
                .set(Room::getLastMsgId,msgId)
                .set(Room::getActiveTime,createTime)
                .update();

    }
}
