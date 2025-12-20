package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.dto.PaperAddRequest;
import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.service.PaperService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaperManagementController.class)
@Import(com.example.qyuanpaperrd.config.TestSecurityConfig.class)
@DisplayName("PaperManagementController 测试")
class PaperManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaperService paperService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaperDTO mockPaper;
    private PaperAddRequest mockPaperAddRequest;

    @BeforeEach
    void setUp() {
        // 创建模拟论文DTO
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

        // 创建模拟新增请求
        mockPaperAddRequest = new PaperAddRequest();
        mockPaperAddRequest.setTitle("Test Paper");
        mockPaperAddRequest.setSubmitter("Test Author");
        mockPaperAddRequest.setAbstractText("Test abstract");
        mockPaperAddRequest.setDoi("10.1000/test");
        mockPaperAddRequest.setJournalSource("Test Journal");
        mockPaperAddRequest.setPdfFileUrl("http://example.com/pdf");
        mockPaperAddRequest.setUrl("http://example.com/paper");
        mockPaperAddRequest.setCategoryId(1L);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("添加论文 - 成功（管理员角色）")
    void addPaper_Success_AdminRole() throws Exception {
        when(paperService.addPaper(any(PaperAddRequest.class))).thenReturn(mockPaper);

        mockMvc.perform(post("/api/v1/admin/papers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPaperAddRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("论文添加成功"))
                .andExpect(jsonPath("$.data.title").value("Test Paper"));
    }

    @Test
    @WithMockUser(username = "editor", authorities = {"ROLE_EDITOR"})
    @DisplayName("添加论文 - 成功（编辑角色）")
    void addPaper_Success_EditorRole() throws Exception {
        when(paperService.addPaper(any(PaperAddRequest.class))).thenReturn(mockPaper);

        mockMvc.perform(post("/api/v1/admin/papers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPaperAddRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("论文添加成功"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("添加论文 - 用户角色也可访问（权限检查已禁用）")
    void addPaper_UserRole_Allowed() throws Exception {
        when(paperService.addPaper(any(PaperAddRequest.class))).thenReturn(mockPaper);

        mockMvc.perform(post("/api/v1/admin/papers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPaperAddRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("论文添加成功"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("更新论文 - 成功")
    void updatePaper_Success() throws Exception {
        when(paperService.updatePaper(eq(1L), any(PaperAddRequest.class))).thenReturn(mockPaper);

        mockMvc.perform(put("/api/v1/admin/papers/{paperId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPaperAddRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("论文更新成功"))
                .andExpect(jsonPath("$.data.title").value("Test Paper"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("更新论文 - 论文不存在")
    void updatePaper_NotFound() throws Exception {
        when(paperService.updatePaper(eq(999L), any(PaperAddRequest.class)))
                .thenThrow(new RuntimeException("论文不存在"));

        mockMvc.perform(put("/api/v1/admin/papers/{paperId}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockPaperAddRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("更新失败：论文不存在"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("删除论文 - 成功（仅管理员角色）")
    void deletePaper_Success() throws Exception {
        when(paperService.deletePaper(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/admin/papers/{paperId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("论文删除成功"))
                .andExpect(jsonPath("$.data").value("success"));
    }

    @Test
    @WithMockUser(username = "editor", authorities = {"ROLE_EDITOR"})
    @DisplayName("删除论文 - 编辑角色也可访问（权限检查已禁用）")
    void deletePaper_EditorRole_Allowed() throws Exception {
        when(paperService.deletePaper(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/admin/papers/{paperId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("论文删除成功"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("删除论文 - 论文不存在")
    void deletePaper_NotFound() throws Exception {
        when(paperService.deletePaper(999L)).thenThrow(new RuntimeException("论文不存在"));

        mockMvc.perform(delete("/api/v1/admin/papers/{paperId}", 999L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("删除失败：论文不存在"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("批量导入论文 - 成功")
    void batchImportPapers_Success() throws Exception {
        List<PaperAddRequest> papers = Arrays.asList(mockPaperAddRequest);
        when(paperService.batchImportPapers(anyList())).thenReturn(1);

        mockMvc.perform(post("/api/v1/admin/papers/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(papers)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量导入成功，共导入1篇论文"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("批量导入论文 - 空列表")
    void batchImportPapers_EmptyList() throws Exception {
        List<PaperAddRequest> papers = Arrays.asList();
        when(paperService.batchImportPapers(anyList())).thenReturn(0);

        mockMvc.perform(post("/api/v1/admin/papers/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(papers)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量导入成功，共导入0篇论文"))
                .andExpect(jsonPath("$.data").value(0));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("批量导入论文 - 验证失败（缺失必填字段）")
    void batchImportPapers_ValidationFailed() throws Exception {
        PaperAddRequest invalidRequest = new PaperAddRequest();
        invalidRequest.setTitle(""); // 空标题，违反@NotBlank约束
        invalidRequest.setSubmitter("Test Author");
        invalidRequest.setUrl("http://example.com/paper");

        List<PaperAddRequest> papers = Arrays.asList(invalidRequest);

        mockMvc.perform(post("/api/v1/admin/papers/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(papers)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("添加论文 - 验证失败（缺失必填字段）")
    void addPaper_ValidationFailed() throws Exception {
        PaperAddRequest invalidRequest = new PaperAddRequest();
        invalidRequest.setTitle(""); // 空标题，违反@NotBlank约束
        invalidRequest.setSubmitter("Test Author");
        invalidRequest.setUrl("http://example.com/paper");

        mockMvc.perform(post("/api/v1/admin/papers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}