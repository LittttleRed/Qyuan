package com.example.qyuanpaperrd.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.qyuanpaperrd.dto.PaperSearchRequest;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.entity.Paper;
import com.example.qyuanpaperrd.entity.AuthorPaper;
import com.example.qyuanpaperrd.mapper.AuthorPaperMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaperMapper 单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Paper Mapper Unit Tests")
public class PaperMapperTest {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private AuthorPaperMapper authorPaperMapper;

    @Test
    @DisplayName("测试基础映射器功能")
    void testPaperMapper_BasicFunctionality() {
        assertNotNull(paperMapper);
    }

    @Test
    @DisplayName("测试根据论文ID查询论文")
    void testSelectById() {
        // Given
        Long paperId = 1L;

        // When
        Paper result = paperMapper.selectById(paperId);

        // Then
        assertNotNull(result);
        assertEquals(paperId, result.getPaperId());
    }

    @Test
    @DisplayName("测试查询不存在的论文")
    void testSelectById_NotFound() {
        // Given
        Long nonExistentId = 99999L;

        // When
        Paper result = paperMapper.selectById(nonExistentId);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试根据DOI查询论文")
    void testSelectByDoi() {
        // Given
        String doi = "10.1234/example.2023.001";

        // When
        Paper result = paperMapper.selectByDoi(doi);

        // Then
        assertNotNull(result);
        assertEquals(doi, result.getDoi());
    }

    @Test
    @DisplayName("测试根据关键词搜索论文")
    void testSearchPapers() {
        // Given
        PaperSearchRequest request = new PaperSearchRequest();
        request.setKeyword("test");
        request.setPage(1);
        request.setSize(10);
        Page<Paper> page = new Page<>(request.getPage(), request.getSize());

        // When
        var result = paperMapper.searchPapers(page, request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    @DisplayName("测试获取热门论文")
    void testGetPopularPapers() {
        // Given
        int limit = 5;

        // When
        List<Paper> result = paperMapper.getPopularPapers(limit);

        // Then
        assertNotNull(result);
        assertTrue(result.size() <= limit);
    }

    @Test
    @DisplayName("测试获取最新论文")
    void testGetLatestPapers() {
        // Given
        int limit = 5;

        // When
        List<Paper> result = paperMapper.getLatestPapers(limit);

        // Then
        assertNotNull(result);
        assertTrue(result.size() <= limit);
    }

    @Test
    @DisplayName("测试插入论文")
    void testInsert() {
        // Given
        Paper newPaper = new Paper();
        newPaper.setTitle("Test Paper for Insert");
        newPaper.setAbstractText("This is a test paper abstract");
        newPaper.setDoi("10.1234/test.insert.2023.001");
        newPaper.setJournalSource("Test Journal");
        newPaper.setSubmitter("test@example.com");
        newPaper.setCategoryId(1L);

        // When
        int result = paperMapper.insert(newPaper);

        // Then
        assertTrue(result > 0);
        assertNotNull(newPaper.getPaperId());
    }

    @Test
    @DisplayName("测试更新论文")
    void testUpdateById() {
        // Given
        Long paperId = 1L;
        Paper existingPaper = paperMapper.selectById(paperId);
        assertNotNull(existingPaper);

        existingPaper.setTitle("Updated Title");
        existingPaper.setAbstractText("Updated abstract");

        // When
        int result = paperMapper.updateById(existingPaper);

        // Then
        assertTrue(result > 0);
    }

    @Test
    @DisplayName("测试删除论文（逻辑删除）")
    void testDeleteById() {
        // Given
        Long paperId = 1L;
        Paper existingPaper = paperMapper.selectById(paperId);
        assertNotNull(existingPaper);

        // When
        int result = paperMapper.deleteById(paperId);

        // Then
        assertTrue(result > 0);

        // 验证逻辑删除后查询不到该论文
        Paper deletedPaper = paperMapper.selectById(paperId);
        assertNull(deletedPaper);
    }

    @Test
    @DisplayName("测试获取论文的作者")
    void testGetAuthorsByPaperId() {
        // Given
        Long paperId = 1L;

        // When
        List<AuthorPaper> result = authorPaperMapper.getAuthorsByPaperId(paperId);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试批量操作性能")
    void testBatchOperationPerformance() {
        // Given
        int iterations = 100;

        // When & Then - 测试批量查询性能
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            paperMapper.selectById(1L);
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        // 批量操作应该在合理时间内完成（这里设置为5秒）
        assertTrue(duration < 5000, "批量查询操作耗时过长: " + duration + "ms");
    }
}