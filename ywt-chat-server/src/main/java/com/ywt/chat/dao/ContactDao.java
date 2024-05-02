package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.Contact;
import com.ywt.chat.mapper.ContactMapper;
import com.ywt.chat.service.ContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会话列表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
@Service
public class ContactDao extends ServiceImpl<ContactMapper, Contact>  {

}
