package com.ywt.chat.service.cache;


import com.ywt.chat.dao.RoomDao;
import com.ywt.chat.dao.RoomFriendDao;
import com.ywt.chat.domain.entity.Room;
import com.ywt.common.constant.RedisKey;
import com.ywt.common.service.cache.AbstractRedisStringCache;
import com.ywt.user.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 房间基本信息的缓存
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-10
 */
@Component
public class RoomCache extends AbstractRedisStringCache<Long, Room> {

    @Autowired
    private RoomDao roomDao;

    @Override
    protected String getKey(Long roomId) {
        return RedisKey.getKey(RedisKey.ROOM_INFO_STRING, roomId);
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    protected Map<Long, Room> load(List<Long> roomIds) {
        List<Room> rooms = roomDao.listByIds(roomIds);
        return rooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));
    }
}
