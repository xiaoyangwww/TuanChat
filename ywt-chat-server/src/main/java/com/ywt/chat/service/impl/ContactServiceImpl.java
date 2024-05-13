package com.ywt.chat.service.impl;

import com.ywt.chat.dao.ContactDao;
import com.ywt.chat.domain.dto.MsgReadInfoDTO;
import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.service.ContactService;
import com.ywt.common.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月13日 18:02
 */
@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactDao contactDao;


    @Override
    public Collection<MsgReadInfoDTO> getMsgReadInfo(List<Message> messages) {
        Map<Long, List<Message>> roomGroup = messages.stream().collect(Collectors.groupingBy(Message::getRoomId));
        AssertUtil.isTrue(roomGroup.size() == 1,"只能查相同房间下的消息");
        Long roomId = roomGroup.keySet().iterator().next();
        Integer totalCount =  contactDao.getTotalCount(roomId);
        return messages.parallelStream().map(message -> {
            MsgReadInfoDTO msgReadInfoDTO = new MsgReadInfoDTO();
            Integer readCount = contactDao.getReadCount(roomId, message);
            msgReadInfoDTO.setReadCount(readCount);
            msgReadInfoDTO.setReadCount(totalCount - readCount - 1);
            return msgReadInfoDTO;
        }).collect(Collectors.toList());
    }
}
