package org.example.qyuansocial.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("user_message")
public class UserMessageRelation {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;         // 主键ID

    @TableField("user_id")
    private Long userId;     // 用户ID

    @TableField("message_id")
    private Long messageId;  // 消息ID

    @TableField("is_read")
    private Boolean isRead;  // 是否已读
}
