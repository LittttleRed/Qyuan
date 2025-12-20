package org.example.qyuansocial.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@Data
@TableName("system_message")
public class SystemMessage {
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;        // 消息ID

    @TableField("message_title")
    private String messageTitle;   // 信息标题

    @TableField("message_content")
    private String messageContent; // 信息内容

    @TableField("created_at")
    private LocalDateTime createdAt; // 创建时间
}
