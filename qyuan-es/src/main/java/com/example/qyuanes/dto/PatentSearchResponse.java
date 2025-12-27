package com.example.qyuanes.dto;

import com.example.qyuanes.entity.Patent;
import lombok.Data;
import java.util.List;

/**
 * 专利搜索响应DTO
 */
@Data
public class PatentSearchResponse {
    /**
     * 专利列表（带高亮信息）
     */
    private List<PatentWithHighlight> patents;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码（从0开始）
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return page != null && page > 0;
    }
    
    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return totalPages != null && page != null && page < totalPages - 1;
    }
}

