package com.example.qyuanpaperrd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 期刊实体类
 * 对应数据库表：journal
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("journal")
public class Journal {

    /**
     * 期刊ID（主键）
     */
    @TableId(value = "journal_id", type = IdType.AUTO)
    private Long journalId;

    /**
     * 期刊名称
     */
    @TableField("journal_name")
    private String journalName;

    /**
     * 期刊链接
     */
    @TableField("journal_url")
    private String journalUrl;

    /**
     * DOAJ链接
     */
    @TableField("doaj_url")
    private String doajUrl;

    /**
     * ISSN号（唯一）
     */
    @TableField("issn")
    private String issn;

    /**
     * 发表年份
     */
    @TableField("publish_time")
    private Integer publishTime;

    /**
     * 关键词
     */
    @TableField("keywords")
    private String keywords;

    /**
     * 包含文章数目
     */
    @TableField("article_number")
    private Integer articleNumber;

    /**
     * 语言
     */
    @TableField("languages")
    private String languages;

    /**
     * 发表者/出版社
     */
    @TableField("publisher")
    private String publisher;
}
