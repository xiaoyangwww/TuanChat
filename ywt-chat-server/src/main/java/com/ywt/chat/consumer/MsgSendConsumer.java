package com.ywt.chat.consumer;

import com.ywt.chat.dao.*;
import com.ywt.chat.domain.entity.*;
import com.ywt.chat.domain.vo.Resp.ChatMessageResp;
import com.ywt.chat.service.ChatService;
import com.ywt.chat.service.PushService;
import com.ywt.chat.service.cache.GroupMemberCache;
import com.ywt.chat.service.cache.HotRoomCache;
import com.ywt.chat.service.cache.RoomCache;
import com.ywt.common.constant.MQConstant;
import com.ywt.common.domain.dto.MsgSendMessageDTO;
import com.ywt.websocket.service.adapter.WebSocketAdapter;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 功能描述
 * 发送消息更新房间收信箱，并同步给房间成员信箱
 * @author: ywt
 * @date: 2024年05月06日 9:34
 */
@RocketMQMessageListener(consumerGroup = MQConstant.SEND_MSG_GROUP,topic = MQConstant.SEND_MSG_TOPIC)
@Component
public class MsgSendConsumer implements RocketMQListener<MsgSendMessageDTO> {
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private ChatService chatService;
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private HotRoomCache hotRoomCache;
    @Autowired
    private PushService pushService;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private ContactDao contactDao;


    @Override
    public void onMessage(MsgSendMessageDTO dto) {
        Message message = messageDao.getById(dto.getMsgId());
        Room room = roomCache.get(message.getRoomId());
        ChatMessageResp msgResp = chatService.getMsgResp(message, null);
        //所有房间更新房间最新消息
        roomDao.refreshActiveTime(room.getId(),message.getId(),message.getCreateTime());
        roomCache.delete(room.getId());
        if (room.isHotRoom()) {//热门群聊推送所有在线的人
            //更新热门群聊时间-redis -zset 用于会话排序
            hotRoomCache.refreshActiveTime(room.getId(),message.getCreateTime());
            // 推送所有人
            pushService.sendPushMsg(WebSocketAdapter.buildPushMsg(msgResp));
        }else {
            List<Long> memberUidList = new ArrayList<>();
            if (room.isRoomGroup()) {
                // 获取所有群成员
                memberUidList = groupMemberCache.getMemberUidList(room.getId());
            } else if (room.isRoomFriend()) {
               RoomFriend roomFriend = roomFriendDao.getByRoomId(room.getId());
               memberUidList = Arrays.asList(roomFriend.getUid1(),roomFriend.getUid2());
            }
            //更新所有群成员的会话时间以及会话最新消息id
            contactDao.refreshOrCreateActiveTime(room.getId(),memberUidList,message.getId(),message.getCreateTime());
            // 推送给相关用户
            pushService.sendPushMsg(WebSocketAdapter.buildPushMsg(msgResp),memberUidList);
        }
    }
}
