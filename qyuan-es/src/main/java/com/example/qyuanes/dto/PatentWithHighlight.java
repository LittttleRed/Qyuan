package com.example.qyuanes.dto;

import com.example.qyuanes.entity.Patent;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 带高亮信息的专利DTO
 */
@Data
public class PatentWithHighlight {
    /**
     * 原始Patent对象
     */
    private Patent patent;
    
    /**
     * 高亮信息
     * Key: 字段名（如 "patent_name", "abstract"）
     * Value: 该字段的高亮片段列表
     */
    private Map<String, List<String>> highlights;
    
    public PatentWithHighlight() {
        this.highlights = new HashMap<>();
    }
    
    public PatentWithHighlight(Patent patent) {
        this.patent = patent;
        this.highlights = new HashMap<>();
    }
    
    public void addHighlight(String field, String highlightFragment) {
        highlights.computeIfAbsent(field, k -> new ArrayList<>()).add(highlightFragment);
    }
    
    public void addHighlights(String field, List<String> highlightFragments) {
        if (highlightFragments != null && !highlightFragments.isEmpty()) {
            highlights.computeIfAbsent(field, k -> new ArrayList<>()).addAll(highlightFragments);
        }
    }
    
    public String getFirstHighlight(String field) {
        List<String> fragments = highlights.get(field);
        if (fragments != null && !fragments.isEmpty()) {
            return fragments.get(0);
        }
        return null;
    }
    
    public boolean hasHighlights() {
        return !highlights.isEmpty();
    }
}

