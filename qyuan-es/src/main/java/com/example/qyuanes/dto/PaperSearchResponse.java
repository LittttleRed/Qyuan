
package com.example.qyuanes.dto;

import com.example.qyuanes.entity.Paper;
import lombok.Data;
import java.util.List;

/**
 * 论文搜索响应DTO
 * 
 * 作用：
 * 1. 统一返回格式，包含数据和分页信息
 * 2. 便于前端统一处理分页逻辑
 * 3. 可以扩展聚合统计信息（如按类别分组统计）
 */

@Data
public class PaperSearchResponse { 
    /**
     * 论文列表
     */
    private List<Paper> papers;
    
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
     * 计算公式：总记录数 / 每页大小，向上取整
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