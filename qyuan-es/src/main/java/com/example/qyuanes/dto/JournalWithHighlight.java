package com.example.qyuanes.dto;

import com.example.qyuanes.entity.Journal;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 带高亮信息的期刊DTO
 */
@Data
public class JournalWithHighlight {
    /**
     * 原始Journal对象
     */
    private Journal journal;
    
    /**
     * 高亮信息
     * Key: 字段名（如 "journal_name", "keywords"）
     * Value: 该字段的高亮片段列表
     */
    private Map<String, List<String>> highlights;
    
    public JournalWithHighlight() {
        this.highlights = new HashMap<>();
    }
    
    public JournalWithHighlight(Journal journal) {
        this.journal = journal;
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

