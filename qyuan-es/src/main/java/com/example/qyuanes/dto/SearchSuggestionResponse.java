package com.example.qyuanes.dto;

import lombok.Data;
import java.util.List;

/**
 * 搜索建议响应DTO
 * 
 * 作用：
 * 1. 返回搜索建议列表，用于前端自动补全
 * 2. 包含建议文本和可能的额外信息（如论文ID、分类等）
 */
@Data
public class SearchSuggestionResponse {
    /**
     * 建议列表
     * 每个建议是一个完整的匹配文本（如"软件工程与地理信息系统设计课程"）
     */
    private List<String> suggestions;
    
    /**
     * 查询的前缀
     * 用户输入的搜索关键词（如"软件"）
     */
    private String prefix;
    
    /**
     * 建议总数
     */
    private Integer total;
    
    /**
     * 构造函数
     */
    public SearchSuggestionResponse() {
    }
    
    /**
     * 构造函数
     * @param prefix 搜索前缀
     * @param suggestions 建议列表
     */
    public SearchSuggestionResponse(String prefix, List<String> suggestions) {
        this.prefix = prefix;
        this.suggestions = suggestions;
        this.total = suggestions != null ? suggestions.size() : 0;
    }
}

