package org.example.qyuansocial.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("follow")
public class Follow {
    @TableId(value = "follow_id", type = IdType.AUTO)
    private Long followId;        // 关注ID

    @TableField("follower_id")
    private Long followerId;      // 关注者ID

    @TableField("follower_name")
    private String followerName;  // 关注者名称

    @TableField("follower_avatar")
    private String followerAvatar;// 关注者头像

    @TableField("followed_id")
    private Long followedId;      // 被关注者ID

    @TableField("followed_name")
    private String followedName;  // 被关注者名称

    @TableField("followed_avatar")
    private String followedAvatar;// 被关注者头像
}
