package com.example.qyuanpaperrd.controller;
import com.example.qyuanpaperrd.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.*;
import com.example.qyuanpaperrd.service.PaperService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 简化的基础功能测试 - 验证服务和API是否正常工作
 */
@WebMvcTest(controllers = PaperController.class)
@Import(TestSecurityConfig.class)
@DisplayName("基础功能测试")
class SimpleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaperService paperService;

    @Test
    @DisplayName("验证搜索论文API基础功能")
    void testSearchPapersAPI() throws Exception {
        // 模拟服务返回结果
        PageResult<PaperDTO> mockResult = new PageResult<>();
        PaperDTO mockPaper = new PaperDTO();
        mockPaper.setTitle("Test Paper");
        mockPaper.setPaperId(1L);
        mockResult.setRecords(Arrays.asList(mockPaper));
        mockResult.setCurrent(1L);
        mockResult.setSize(20L);
        mockResult.setTotal(1L);
        mockResult.setPages(1L);
        
        when(paperService.searchPapers(any(PaperSearchRequest.class))).thenReturn(mockResult);

        mockMvc.perform(get("/api/v1/papers/search")
                .param("query", "test")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].title").value("Test Paper"))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("验证获取论文详情API基础功能")
    void testGetPaperByIdAPI() throws Exception {
        PaperDTO mockPaper = new PaperDTO();
        mockPaper.setTitle("Test Paper Details");
        mockPaper.setPaperId(1L);
        
        when(paperService.getPaperById(1L)).thenReturn(mockPaper);

        mockMvc.perform(get("/api/v1/papers/{paperId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Test Paper Details"))
                .andExpect(jsonPath("$.data.paperId").value(1));
    }

    @Test
    @DisplayName("验证论文不存在的情况")
    void testGetPaperByIdNotFound() throws Exception {
        when(paperService.getPaperById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/papers/{paperId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("论文不存在"));
    }

    @Test
    @DisplayName("验证热门论文API基础功能")
    void testGetPopularPapersAPI() throws Exception {
        PaperDTO mockPaper = new PaperDTO();
        mockPaper.setTitle("Popular Paper");
        mockPaper.setPaperId(1L);
        
        when(paperService.getPopularPapers(10)).thenReturn(Arrays.asList(mockPaper));

        mockMvc.perform(get("/api/v1/papers/popular")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Popular Paper"));
    }

    @Test
    @DisplayName("验证最新论文API基础功能")
    void testGetLatestPapersAPI() throws Exception {
        PaperDTO mockPaper = new PaperDTO();
        mockPaper.setTitle("Latest Paper");
        mockPaper.setPaperId(1L);
        
        when(paperService.getLatestPapers(10)).thenReturn(Arrays.asList(mockPaper));

        mockMvc.perform(get("/api/v1/papers/latest")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Latest Paper"));
    }

    @Test
    @DisplayName("验证按分类获取论文API基础功能")
    void testGetPapersByCategoryAPI() throws Exception {
        PageResult<PaperDTO> mockResult = new PageResult<>();
        PaperDTO mockPaper = new PaperDTO();
        mockPaper.setTitle("Category Paper");
        mockPaper.setPaperId(1L);
        mockResult.setRecords(Arrays.asList(mockPaper));
        mockResult.setCurrent(1L);
        mockResult.setSize(20L);
        mockResult.setTotal(1L);
        mockResult.setPages(1L);
        
        when(paperService.getPapersByCategory(1L, 1, 20)).thenReturn(mockResult);

        mockMvc.perform(get("/api/v1/papers/category/{categoryId}", 1L)
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].title").value("Category Paper"))
                .andExpect(jsonPath("$.data.current").value(1));
    }
}
