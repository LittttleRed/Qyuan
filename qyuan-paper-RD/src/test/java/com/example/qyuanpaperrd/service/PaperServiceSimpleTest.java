package com.example.qyuanpaperrd.service;

import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.PaperSearchRequest;
import com.example.qyuanpaperrd.common.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单的论文服务测试
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Paper Service Simple Tests")
public class PaperServiceSimpleTest {

    @Autowired
    private PaperService paperService;

    @Test
    @DisplayName("测试论文服务注入")
    public void testServiceInjection() {
        assertNotNull(paperService, "PaperService should be injected");
    }

    @Test
    @DisplayName("测试基本搜索功能")
    public void testBasicSearch() {
        // Given
        PaperSearchRequest request = new PaperSearchRequest();
        request.setKeyword("test");
        request.setPage(1);
        request.setSize(10);

        // When
        PageResult<PaperDTO> result = paperService.searchPapers(request);

        // Then
        assertNotNull(result, "Search result should not be null");
        assertNotNull(result.getRecords(), "Records should not be null");
    }

    @Test
    @DisplayName("测试获取热门论文")
    public void testGetPopularPapers() {
        // When
        var popularPapers = paperService.getPopularPapers(5);

        // Then
        assertNotNull(popularPapers, "Popular papers should not be null");
    }

    @Test
    @DisplayName("测试获取最新论文")
    public void testGetLatestPapers() {
        // When
        var latestPapers = paperService.getLatestPapers(5);

        // Then
        assertNotNull(latestPapers, "Latest papers should not be null");
    }
}