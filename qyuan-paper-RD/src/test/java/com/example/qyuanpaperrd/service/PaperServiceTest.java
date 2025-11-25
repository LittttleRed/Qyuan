package com.example.qyuanpaperrd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.PaperSearchRequest;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.entity.Paper;
import com.example.qyuanpaperrd.entity.AuthorPaper;
import com.example.qyuanpaperrd.mapper.PaperMapper;
import com.example.qyuanpaperrd.mapper.AuthorPaperMapper;
import com.example.qyuanpaperrd.service.impl.PaperServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * PaperService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Paper Service Unit Tests")
public class PaperServiceTest {

    @Mock
    private PaperMapper paperMapper;

    @Mock
    private AuthorPaperMapper authorPaperMapper;

    @InjectMocks
    private PaperServiceImpl paperService;

    private PaperDTO mockPaper1;
    private PaperDTO mockPaper2;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        mockPaper1 = new PaperDTO();
        mockPaper1.setPaperId(1L);
        mockPaper1.setTitle("Machine Learning Advances");
        mockPaper1.setAbstractText("This paper discusses recent advances in machine learning.");
        mockPaper1.setDoi("10.1234/ml.2023.001");
        mockPaper1.setJournalSource("Journal of Machine Learning");
        mockPaper1.setSubmitter("test@example.com");
        mockPaper1.setCreated(LocalDateTime.now());
        mockPaper1.setUpdated(LocalDateTime.now());

        mockPaper2 = new PaperDTO();
        mockPaper2.setPaperId(2L);
        mockPaper2.setTitle("Deep Learning Applications");
        mockPaper2.setAbstractText("Applications of deep learning in various domains.");
        mockPaper2.setDoi("10.1234/dl.2023.002");
        mockPaper2.setJournalSource("Neural Computing Journal");
        mockPaper2.setSubmitter("researcher@example.com");
        mockPaper2.setCreated(LocalDateTime.now());
        mockPaper2.setUpdated(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试搜索论文功能 - 成功")
    void testSearchPapers_Success() {
        // Given
        PaperSearchRequest request = new PaperSearchRequest();
        request.setKeyword("machine");
        request.setPage(1);
        request.setSize(10);

        // Create mock Paper entities (not DTOs)
        Paper mockPaperEntity1 = new Paper();
        mockPaperEntity1.setPaperId(1L);
        mockPaperEntity1.setTitle("Machine Learning Advances");
        mockPaperEntity1.setDoi("10.1234/ml.2023.001");

        Paper mockPaperEntity2 = new Paper();
        mockPaperEntity2.setPaperId(2L);
        mockPaperEntity2.setTitle("Deep Learning Applications");
        mockPaperEntity2.setDoi("10.1234/dl.2023.002");

        List<Paper> mockPapers = Arrays.asList(mockPaperEntity1, mockPaperEntity2);

        // Create a mock IPage<Paper> result
        Page<Paper> page = new Page<>(1, 10);
        page.setRecords(mockPapers);
        page.setTotal(2);
        page.setCurrent(1);
        page.setSize(10);

        when(paperMapper.searchPapers(any(), any(PaperSearchRequest.class))).thenReturn(page);

        // When
        PageResult<PaperDTO> result = paperService.searchPapers(request);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getTotal());
        assertEquals(1L, result.getCurrent());
        assertEquals(10L, result.getSize());
        assertNotNull(result.getRecords());
        assertEquals(2, result.getRecords().size());

        verify(paperMapper, times(1)).searchPapers(any(), eq(request));
    }

    @Test
    @DisplayName("测试获取热门论文功能 - 成功")
    void testGetPopularPapers_Success() {
        // Given
        int limit = 5;

        // Create mock Paper entities
        Paper mockPaperEntity1 = new Paper();
        mockPaperEntity1.setPaperId(1L);
        mockPaperEntity1.setTitle("Machine Learning Advances");
        mockPaperEntity1.setDoi("10.1234/ml.2023.001");

        Paper mockPaperEntity2 = new Paper();
        mockPaperEntity2.setPaperId(2L);
        mockPaperEntity2.setTitle("Deep Learning Applications");
        mockPaperEntity2.setDoi("10.1234/dl.2023.002");

        List<Paper> mockPopularPapers = Arrays.asList(mockPaperEntity1, mockPaperEntity2);

        when(paperMapper.selectPopularPapers(limit)).thenReturn(mockPopularPapers);

        // When
        List<PaperDTO> result = paperService.getPopularPapers(limit);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Machine Learning Advances", result.get(0).getTitle());

        verify(paperMapper, times(1)).selectPopularPapers(limit);
    }

    @Test
    @DisplayName("测试获取最新论文功能 - 成功")
    void testGetLatestPapers_Success() {
        // Given
        int limit = 5;

        // Create mock Paper entities
        Paper mockPaperEntity1 = new Paper();
        mockPaperEntity1.setPaperId(1L);
        mockPaperEntity1.setTitle("Machine Learning Advances");
        mockPaperEntity1.setDoi("10.1234/ml.2023.001");

        Paper mockPaperEntity2 = new Paper();
        mockPaperEntity2.setPaperId(2L);
        mockPaperEntity2.setTitle("Deep Learning Applications");
        mockPaperEntity2.setDoi("10.1234/dl.2023.002");

        List<Paper> mockLatestPapers = Arrays.asList(mockPaperEntity2, mockPaperEntity1);

        when(paperMapper.selectLatestPapers(limit)).thenReturn(mockLatestPapers);

        // When
        List<PaperDTO> result = paperService.getLatestPapers(limit);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Deep Learning Applications", result.get(0).getTitle());

        verify(paperMapper, times(1)).selectLatestPapers(limit);
    }

    @Test
    @DisplayName("测试搜索论文功能 - 空结果")
    void testSearchPapers_EmptyResult() {
        // Given
        PaperSearchRequest request = new PaperSearchRequest();
        request.setKeyword("nonexistent");
        request.setPage(1);
        request.setSize(10);

        // Create a mock IPage<Paper> result with empty records
        Page<Paper> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList());
        page.setTotal(0L);
        page.setCurrent(1L);
        page.setSize(10L);

        when(paperMapper.searchPapers(any(), any(PaperSearchRequest.class))).thenReturn(page);

        // When
        PageResult<PaperDTO> result = paperService.searchPapers(request);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());

        verify(paperMapper, times(1)).searchPapers(any(), eq(request));
    }

    @Test
    @DisplayName("测试获取热门论文功能 - 空结果")
    void testGetPopularPapers_EmptyResult() {
        // Given
        int limit = 5;

        when(paperMapper.selectPopularPapers(limit)).thenReturn(Arrays.asList());

        // When
        List<PaperDTO> result = paperService.getPopularPapers(limit);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(paperMapper, times(1)).selectPopularPapers(limit);
    }
}