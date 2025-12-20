package com.example.qyuanpaperrd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 论文实体类
 * 对应数据库表：paper
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("paper")
public class Paper {

    /**
     * 论文ID（主键）
     */
    @TableId(value = "paper_id", type = IdType.AUTO)
    private Long paperId;

    /**
     * 论文标题
     */
    @TableField("title")
    private String title;

    /**
     * 提交人
     */
    @TableField("submitter")
    private String submitter;

    /**
     * 摘要
     */
    @TableField("abstract")
    private String abstractText;

    /**
     * DOI（唯一）
     */
    @TableField("doi")
    private String doi;

    /**
     * 期刊来源
     */
    @TableField("journal_source")
    private String journalSource;

    /**
     * PDF文件URL
     */
    @TableField("pdf_file_url")
    private String pdfFileUrl;

    /**
     * 原链接
     */
    @TableField("url")
    private String url;

    /**
     * 领域信息
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 更新时间
     */
    @TableField(value = "updated", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updated;

    /**
     * 收藏数
     */
    @TableField("favorite_count")
    private Integer favoriteCount;

    /**
     * 阅读次数
     */
    @TableField("read_count")
    private Integer readCount;

    // ========== 非数据库字段（仅用于业务逻辑） ==========

    /**
     * 期刊名称（兼容字段）
     */
    @TableField(exist = false)
    private String journal;

    /**
     * 关键词
     */
    @TableField(exist = false)
    private String keywords;

    /**
     * 卷号
     */
    @TableField(exist = false)
    private String volume;

    /**
     * 期号
     */
    @TableField(exist = false)
    private String issue;

    /**
     * 页码
     */
    @TableField(exist = false)
    private String pages;
}
