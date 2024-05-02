package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.WxMsg;
import com.ywt.chat.mapper.WxMsgMapper;
import com.ywt.chat.service.WxMsgService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 微信消息表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
@Service
public class WxMsgDao extends ServiceImpl<WxMsgMapper, WxMsg> {

}
