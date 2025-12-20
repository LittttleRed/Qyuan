package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.*;
import com.example.qyuanpaperrd.service.PaperService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaperController.class)
@Import(com.example.qyuanpaperrd.config.TestSecurityConfig.class)
@DisplayName("PaperController 测试")
class PaperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaperService paperService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaperDTO mockPaper;
    private PageResult<PaperDTO> mockPageResult;

    @BeforeEach
    void setUp() {
        mockPaper = new PaperDTO();
        mockPaper.setPaperId(1L);
        mockPaper.setTitle("Test Paper");
        mockPaper.setSubmitter("Test Author");
        mockPaper.setAbstractText("Test abstract");
        mockPaper.setDoi("10.1000/test");
        mockPaper.setJournalSource("Test Journal");
        mockPaper.setPdfFileUrl("http://example.com/pdf");
        mockPaper.setUrl("http://example.com/paper");
        mockPaper.setCategoryId(1L);
        mockPaper.setCreated(LocalDateTime.now());
        mockPaper.setUpdated(LocalDateTime.now());
        mockPaper.setDownloadCount(0);
        mockPaper.setFavoriteCount(0);
        mockPaper.setIsFavorited(false);
        mockPaper.setIsClaimed(false);

        mockPageResult = new PageResult<>();
        mockPageResult.setRecords(Arrays.asList(mockPaper));
        mockPageResult.setCurrent(1L);
        mockPageResult.setSize(20L);
        mockPageResult.setTotal(1L);
        mockPageResult.setPages(1L);
    }

    @Test
    @DisplayName("搜索论文 - 成功")
    void searchPapers_Success() throws Exception {
        when(paperService.searchPapers(any(PaperSearchRequest.class))).thenReturn(mockPageResult);

        mockMvc.perform(get("/api/v1/papers/search")
                .param("query", "test")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Test Paper"));
    }

    @Test
    @DisplayName("获取论文详情 - 成功")
    void getPaperById_Success() throws Exception {
        when(paperService.getPaperById(1L)).thenReturn(mockPaper);

        mockMvc.perform(get("/api/v1/papers/{paperId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Test Paper"))
                .andExpect(jsonPath("$.data.doi").value("10.1000/test"));
    }

    @Test
    @DisplayName("获取论文详情 - 论文不存在")
    void getPaperById_NotFound() throws Exception {
        when(paperService.getPaperById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/papers/{paperId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("论文不存在"));
    }

    @Test
    @DisplayName("搜索论文 - 按分类搜索")
    void searchPapers_ByCategory() throws Exception {
        PaperSearchRequest request = new PaperSearchRequest();
        request.setKeyword("");
        request.setCategoryId(1L);
        request.setPage(1);
        request.setSize(20);

        when(paperService.searchPapers(any(PaperSearchRequest.class))).thenReturn(mockPageResult);

        mockMvc.perform(get("/api/v1/papers/search")
                .param("category_id", "1")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Test Paper"));
    }

    @Test
    @DisplayName("下载论文PDF - 成功")
    void downloadPaper_Success() throws Exception {
        when(paperService.downloadPaperPdf(1L)).thenReturn("http://example.com/download/paper.pdf");

        mockMvc.perform(get("/api/v1/papers/{paperId}/download", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.download_url").value("http://example.com/download/paper.pdf"));
    }

    @Test
    @DisplayName("下载论文PDF - 论文不存在")
    void downloadPaper_NotFound() throws Exception {
        when(paperService.downloadPaperPdf(999L)).thenThrow(new RuntimeException("论文不存在"));

        mockMvc.perform(get("/api/v1/papers/{paperId}/download", 999L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("下载失败：论文不存在"));
    }

    @Test
    @DisplayName("获取论文作者列表 - 成功")
    void getPaperAuthors_Success() throws Exception {
        when(paperService.getPaperById(1L)).thenReturn(mockPaper);

        mockMvc.perform(get("/api/v1/papers/{paperId}/authors", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取论文作者列表 - 论文不存在")
    void getPaperAuthors_NotFound() throws Exception {
        when(paperService.getPaperById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/papers/{paperId}/authors", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("论文不存在"));
    }

    @Test
    @DisplayName("导出论文信息 - 成功")
    void exportPapers_Success() throws Exception {
        PaperExportRequest request = new PaperExportRequest();
        request.setPaperIds(Arrays.asList(1L, 2L));
        request.setFormat("bibtex");

        when(paperService.exportPapers(any(PaperExportRequest.class))).thenReturn("http://example.com/export.bib");

        mockMvc.perform(post("/api/v1/papers/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("导出成功"))
                .andExpect(jsonPath("$.data").value("http://example.com/export.bib"));
    }

    @Test
    @DisplayName("获取论文引用关系 - 获取引用的文献")
    void getPaperReferences_Citations() throws Exception {
        List<CitationDTO> citations = Arrays.asList();
        when(paperService.getPaperCitations(1L)).thenReturn(citations);

        mockMvc.perform(get("/api/v1/papers/{paperId}/references", 1L)
                .param("direction", "citations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取论文引用关系 - 获取被引用的文献")
    void getPaperReferences_CitedBy() throws Exception {
        List<CitationDTO> citations = Arrays.asList();
        when(paperService.getPapersCitedBy(1L)).thenReturn(citations);

        mockMvc.perform(get("/api/v1/papers/{paperId}/references", 1L)
                .param("direction", "cited-by"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
