package com.example.qyuanes.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 简单搜索请求DTO
 * 用于Patent和Journal的简单搜索（只支持关键词搜索）
 */
@Data
public class SimpleSearchRequest {
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 页码（从0开始）
     */
    @Min(value = 0, message = "页码不能小于0")
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小至少为1")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;
}

