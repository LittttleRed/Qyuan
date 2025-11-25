package com.example.qyuanpaperrd.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 论文搜索请求DTO
 */
@Data
public class PaperSearchRequest {

    /**
     * 搜索关键词
     */
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    /**
     * 搜索字段：title,author,abstract
     */
    private String searchField;

    /**
     * 领域分类ID
     */
    private Long categoryId;

    /**
     * 作者名（可搜索姓或名）
     */
    private String authorName;

    /**
     * 期刊名称
     */
    private String journalName;

    /**
     * 年份范围开始
     */
    private Integer yearStart;

    /**
     * 年份范围结束
     */
    private Integer yearEnd;

    /**
     * 排序字段：title, author, journal, date, downloads
     */
    private String sortBy = "relevance";

    /**
     * 排序方向：asc, desc
     */
    private String sortOrder = "desc";

    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 20;
}