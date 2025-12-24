package com.example.qyuanes.dto;

import lombok.Data;
import java.util.List;

/**
 * 热门搜索响应DTO
 * 
 * 作用：
 * 1. 返回热门搜索关键词列表
 * 2. 包含关键词和搜索次数信息
 */
@Data
public class HotSearchResponse {
    /**
     * 热门搜索关键词列表
     * 按搜索次数降序排列
     */
    private List<HotSearchItem> keywords;
    
    /**
     * 热门搜索项
     */
    @Data
    public static class HotSearchItem {
        /**
         * 搜索关键词
         */
        private String keyword;
        
        /**
         * 搜索次数
         */
        private Long count;
        
        /**
         * 排名（从1开始）
         */
        private Integer rank;
        
        public HotSearchItem() {
        }
        
        public HotSearchItem(String keyword, Long count, Integer rank) {
            this.keyword = keyword;
            this.count = count;
            this.rank = rank;
        }
    }
    
    /**
     * 构造函数
     */
    public HotSearchResponse() {
    }
    
    /**
     * 构造函数
     * @param keywords 热门搜索关键词列表
     */
    public HotSearchResponse(List<HotSearchItem> keywords) {
        this.keywords = keywords;
    }
}

