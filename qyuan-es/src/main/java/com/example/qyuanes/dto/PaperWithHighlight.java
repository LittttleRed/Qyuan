package com.example.qyuanes.dto;

import com.example.qyuanes.entity.Paper;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 带高亮信息的论文DTO
 * 
 * 作用：
 * 1. 包装Paper对象和高亮信息
 * 2. 前端可以根据高亮信息渲染高亮效果
 * 3. 高亮信息以Map形式存储：字段名 -> 高亮片段列表
 * 
 * 示例：
 * - highlights.get("title") = ["<em>机器学习</em>在...中的应用"]
 * - highlights.get("abstract") = ["本文研究了<em>机器学习</em>...", "...基于<em>深度学习</em>的方法"]
 */
@Data
public class PaperWithHighlight {
    /**
     * 原始Paper对象
     */
    private Paper paper;
    
    /**
     * 高亮信息
     * Key: 字段名（如 "title", "abstract", "journal_source"）
     * Value: 该字段的高亮片段列表（可能有多个片段）
     * 
     * 注意：高亮片段中已包含高亮标签（如 <em></em>）
     * 前端可以直接使用这些标签进行渲染
     */
    private Map<String, List<String>> highlights;
    
    /**
     * 构造函数
     */
    public PaperWithHighlight() {
        this.highlights = new HashMap<>();
    }
    
    /**
     * 构造函数
     * @param paper Paper对象
     */
    public PaperWithHighlight(Paper paper) {
        this.paper = paper;
        this.highlights = new HashMap<>();
    }
    
    /**
     * 添加高亮信息
     * @param field 字段名
     * @param highlightFragment 高亮片段
     */
    public void addHighlight(String field, String highlightFragment) {
        highlights.computeIfAbsent(field, k -> new ArrayList<>()).add(highlightFragment);
    }
    
    /**
     * 添加多个高亮片段
     * @param field 字段名
     * @param highlightFragments 高亮片段列表
     */
    public void addHighlights(String field, List<String> highlightFragments) {
        if (highlightFragments != null && !highlightFragments.isEmpty()) {
            highlights.computeIfAbsent(field, k -> new ArrayList<>()).addAll(highlightFragments);
        }
    }
    
    /**
     * 获取指定字段的第一个高亮片段（如果没有则返回null）
     * 用于简单场景，如只需要显示标题的高亮
     * @param field 字段名
     * @return 第一个高亮片段，如果没有则返回null
     */
    public String getFirstHighlight(String field) {
        List<String> fragments = highlights.get(field);
        if (fragments != null && !fragments.isEmpty()) {
            return fragments.get(0);
        }
        return null;
    }
    
    /**
     * 检查是否有高亮信息
     * @return 如果有任何字段的高亮信息则返回true
     */
    public boolean hasHighlights() {
        return !highlights.isEmpty();
    }
}

