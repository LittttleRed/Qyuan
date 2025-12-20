package com.example.qyuanpaperrd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户论文关系实体类
 * 对应数据库表：user_paper
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_paper")
public class UserPaper {

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
     * 作者次序
     */
    @TableField("user_rank")
    private Integer userRank;
}
