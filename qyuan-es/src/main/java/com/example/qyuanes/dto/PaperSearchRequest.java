package com.example.qyuanes.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.OffsetDateTime;
import java.util.List;


/**
 * 论文搜索请求DTO
 * 
 * 作用：
 * 1. 封装前端传递的搜索条件，避免Controller直接使用Entity
 * 2. 提供数据验证（如分页参数范围）
 * 3. 支持简单搜索和复杂查询的统一入口
 */


@Data 
public class PaperSearchRequest {
    // ========== 简单搜索字段 ==========
    
    /**
     * 关键词搜索
     * 会在 title、abstract、journal_source 等字段中搜索
     */
    private String keyword;

    // ========== 精确过滤字段 ==========
    
    /**
     * 种类ID（精确匹配）
     */
    private Integer categoryId;
    /**
     * 多个种类ID（OR关系）
     */
    private List<Integer> categoryIds;
    /**
     * 提交者（精确匹配）
     */
    private String submitter;
    /**
     * 期刊来源（模糊匹配）
     */
    private String journalSource;
    // ========== 时间范围查询 ==========
    /**
     * 发布时间起始（updated字段）
     */
    private OffsetDateTime startTime;
    /**
     * 发布时间结束
     */
    private OffsetDateTime endTime;

    // ========== 数值范围查询 ==========
    /**
     * 最小阅读数
     */
    private Integer minReadCount;
    /**
     * 最大阅读数
     */
    private Integer maxReadCount;
    /**
     * 最小收藏数
     */
    private Integer minFavoriteCount;
    /**
     * 最大收藏数
     */
    private Integer maxFavoriteCount;

    // ========== 分页参数 ==========
    /**
     * 页码（从0开始）
     * @Min(0) 确保页码不能为负数
     */
    @Min(value = 0, message = "页码不能小于0")
    private Integer page;

    /**
     * 每页大小
     * @Min(1) 至少返回1条
     * @Max(100) 最多返回100条，防止一次性查询过多数据
     */
    @Min(value = 1, message = "每页大小至少为1")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;

    // ========== 排序参数 ==========
    /**
     * 排序字段
     * 可选值：updated, read_count, favorite_count, paper_id
     */
    private String sortField = "updated";
    /**
     * 排序方向
     * asc: 升序, desc: 降序
     */
    private String sortOrder = "desc";
    /**
     * 多字段排序（高级功能）
     * 例如：[{"field": "read_count", "order": "desc"}, {"field": "updated", "order": "desc"}]
     */
    private List<SortOption> sortOptions;
    /**
     * 排序选项内部类
     */
    @Data
    public static class SortOption {
        private String field;
        private String order = "desc";
    }   
    // ========== 辅助方法 ==========
    public boolean hasSearchConditions() {
        return keyword != null || categoryId != null || categoryIds != null
            || submitter != null || journalSource != null
            || startTime != null || endTime != null
            || minReadCount != null || maxReadCount != null
            || minFavoriteCount != null || maxFavoriteCount != null;
    }

}
