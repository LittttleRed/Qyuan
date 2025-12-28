package com.example.qyuanpaperrd.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 期刊数据传输对象
 */
@Data
@Accessors(chain = true)
public class JournalDTO {

    /**
     * 期刊ID
     */
    private Long journalId;

    /**
     * 期刊名称
     */
    private String journalName;

    /**
     * 期刊链接
     */
    private String journalUrl;

    /**
     * DOAJ链接
     */
    private String doajUrl;

    /**
     * ISSN号
     */
    private String issn;

    /**
     * 发表年份
     */
    private Integer publishTime;

    /**
     * 关键词
     */
    private String keywords;

    /**
     * 包含文章数目
     */
    private Integer articleNumber;

    /**
     * 语言
     */
    private String languages;

    /**
     * 发表者/出版社
     */
    private String publisher;
}