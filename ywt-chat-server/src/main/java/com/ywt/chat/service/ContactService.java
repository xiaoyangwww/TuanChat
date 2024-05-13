package com.ywt.chat.service;

import com.ywt.chat.domain.dto.MsgReadInfoDTO;
import com.ywt.chat.domain.entity.Contact;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ywt.chat.domain.entity.Message;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 会话列表 服务类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
public interface ContactService  {

    /**
     *
     * @param messages
     * @return
     */
    Collection<MsgReadInfoDTO> getMsgReadInfo( List<Message> messages);
}
