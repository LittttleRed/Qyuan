package com.example.qyuanpaperrd.service;

import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.PaperSearchRequest;
import com.example.qyuanpaperrd.common.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用Spring Boot自动配置数据库的论文服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Paper Service Spring Boot Integration Tests")
public class PaperServiceIntegrationTestSimple {

    @Autowired
    private PaperService paperService;

    @Test
    @DisplayName("测试论文服务基础功能 - Spring Boot自动配置")
    public void testServiceInjection() {
        assertNotNull(paperService, "PaperService should be injected");
    }

    @Test
    @DisplayName("测试搜索论文功能 - Spring Boot自动配置")
    public void testSearchPapers() {
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
    @DisplayName("测试获取热门论文功能 - Spring Boot自动配置")
    public void testGetPopularPapers() {
        // Given
        int limit = 5;

        // When
        List<PaperDTO> result = paperService.getPopularPapers(limit);

        // Then
        assertNotNull(result, "Popular papers result should not be null");
    }

    @Test
    @DisplayName("测试获取最新论文功能 - Spring Boot自动配置")
    public void testGetLatestPapers() {
        // Given
        int limit = 5;

        // When
        List<PaperDTO> result = paperService.getLatestPapers(limit);

        // Then
        assertNotNull(result, "Latest papers result should not be null");
    }

    @Test
    @DisplayName("测试论文服务基本功能 - Spring Boot自动配置")
    public void testPaperService_BasicFunctionality() {
        // Then
        assertNotNull(paperService, "PaperService should be injected");
    }
}