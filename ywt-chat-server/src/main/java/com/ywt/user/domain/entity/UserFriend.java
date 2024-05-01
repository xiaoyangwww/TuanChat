package com.ywt.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户联系人表
 * </p>
 *
 * @author ywt
 * @since 2024-04-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_friend")
public class UserFriend implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * uid
     */
    @TableField("uid")
    private Long uid;

    /**
     * 好友uid
     */
    @TableField("friend_uid")
    private Long friendUid;

    /**
     * 逻辑删除(0-正常,1-删除)
     */
    @TableField("delete_status")
    @TableLogic(value = "0",delval = "1")
    private Integer deleteStatus;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;


}
