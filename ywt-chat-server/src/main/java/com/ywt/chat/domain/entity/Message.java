package com.ywt.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ywt.chat.domain.entity.msg.MessageExtra;
import lombok.*;

/**
 * <p>
 * 消息表
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("message")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话表id
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 消息发送者uid
     */
    @TableField("from_uid")
    private Long fromUid;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 回复的消息内容
     */
    @TableField("reply_msg_id")
    private Long replyMsgId;

    /**
     * 消息状态 0正常 1删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 与回复的消息间隔多少条
     */
    @TableField("gap_count")
    private Integer gapCount;

    /**
     * 消息类型 1正常文本 2.撤回消息....
     *
     * @see com.ywt.chat.domain.enums.MessageTypeEnum
     */
    @TableField("type")
    private Integer type;

    /**
     * 消息扩展字段
     */
    @TableField(value = "extra", typeHandler = JacksonTypeHandler.class)
    private MessageExtra extra;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private Date updateTime;


}
