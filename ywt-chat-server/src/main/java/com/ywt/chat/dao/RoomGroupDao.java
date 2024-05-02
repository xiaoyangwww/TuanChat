package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.RoomGroup;
import com.ywt.chat.mapper.RoomGroupMapper;
import com.ywt.chat.service.RoomGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
