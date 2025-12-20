package com.example.qyuanpaperrd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 论文引用实体类
 * 对应数据库表：paper_ref
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("paper_ref")
public class PaperRef {

    /**
     * 引用主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 论文ID
     */
    @TableField("paper_id")
    private Long paperId;

    /**
     * 引用论文标题
     */
    @TableField("ref_title")
    private String refTitle;

    /**
     * 引用论文摘要(前200字)
     */
    @TableField("abstract")
    private String abstractText;

    /**
     * 一作姓名
     */
    @TableField("auther_name")
    private String autherName;

    /**
     * 发表年份
     */
    @TableField("updated")
    private Short updated;

    /**
     * 被引用次数
     */
    @TableField("be_refed_count")
    private Integer beRefedCount;

    /**
     * 源url
     */
    @TableField("url")
    private String url;
}
