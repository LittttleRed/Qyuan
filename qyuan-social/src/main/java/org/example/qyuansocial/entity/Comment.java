package org.example.qyuansocial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Comment {

    @JsonProperty("comment_id")
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    @JsonProperty("user_id")
    @TableField("user_id")
    private Long userId;

    @JsonProperty("user_name")
    @TableField("user_name")
    private String userName;

    @JsonProperty("user_avatar")
    @TableField("user_avatar")
    private String userAvatar;

    @JsonProperty("achievement_id")
    @TableField("achievement_id")
    private Long achievementId;

    @JsonProperty("achievement_type")
    @TableField("achievement_type")
    private String achievementType;

    // 数据库列名 parent_comment_id
    @JsonProperty("parent_comment_id")
    @TableField("parent_comment_id")
    private Long parentId;

    @JsonProperty("content")
    @TableField("content")
    private String content;

    @JsonProperty("like_count")
    @TableField("like_count")
    private Integer likeCount;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("created_at")
    private LocalDateTime createdAt;
}


