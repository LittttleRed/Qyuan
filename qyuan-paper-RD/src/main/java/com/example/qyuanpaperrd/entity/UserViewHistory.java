package com.example.qyuanpaperrd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户浏览历史实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_view_history")
public class UserViewHistory {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 论文ID
     */
    @TableField("paper_id")
    private Long paperId;

    /**
     * 浏览时间
     */
    @TableField("view_time")
    private LocalDateTime viewTime;

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean deleted;

    public UserViewHistory() {
        this.viewTime = LocalDateTime.now();
        this.deleted = false;
    }

    public UserViewHistory(Long userId, Long paperId) {
        this();
        this.userId = userId;
        this.paperId = paperId;
    }
}