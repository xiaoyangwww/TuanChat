package com.ywt.chat.mapper;

import com.ywt.chat.domain.entity.Contact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会话列表 Mapper 接口
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
public interface ContactMapper extends BaseMapper<Contact> {

    void refreshOrCreateActiveTime(@Param("roomId") Long roomId,@Param("memberUidList") List<Long> memberUidList,@Param("msgId") Long msgId,@Param("activeTime")  Date activeTime);
}
