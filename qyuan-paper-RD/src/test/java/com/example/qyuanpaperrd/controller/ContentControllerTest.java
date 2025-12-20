package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.PatentDTO;
import com.example.qyuanpaperrd.service.PaperService;
import com.example.qyuanpaperrd.service.PatentService;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContentController.class)
@Import(com.example.qyuanpaperrd.config.TestSecurityConfig.class)
@DisplayName("ContentController 测试")
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaperService paperService;

    @MockBean
    private PatentService patentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaperDTO mockPaper;
    private PatentDTO mockPatent;

    @BeforeEach
    void setUp() {
        mockPaper = new PaperDTO();
        mockPaper.setPaperId(1L);
        mockPaper.setTitle("Test Paper");
        mockPaper.setSubmitter("Test Author");
        mockPaper.setAbstractText("Test abstract");
        mockPaper.setDownloadCount(100);

        mockPatent = new PatentDTO();
        mockPatent.setPatentNumber("US20230012345A");
        mockPatent.setPatentName("Test Patent");
        mockPatent.setAbstractText("Test abstract");
        mockPatent.setInventor("John Doe");
        mockPatent.setAssignee("Test Company");
        mockPatent.setCitationCount(50);
    }

    @Test
    @DisplayName("获取热门内容 - 论文类型")
    void getPopularContent_Paper() throws Exception {
        List<PaperDTO> popularPapers = Arrays.asList(mockPaper);
        when(paperService.getPopularPapers(10)).thenReturn(popularPapers);

        mockMvc.perform(get("/api/v1/contents/popular")
                .param("type", "paper")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("Test Paper"));
    }

    @Test
    @DisplayName("获取热门内容 - 专利类型")
    void getPopularContent_Patent() throws Exception {
        List<PatentDTO> hotPatents = Arrays.asList(mockPatent);
        when(patentService.getHotPatents(10)).thenReturn(hotPatents);

        mockMvc.perform(get("/api/v1/contents/popular")
                .param("type", "patent")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].patentName").value("Test Patent"));
    }

    @Test
    @DisplayName("获取最新内容 - 论文类型")
    void getLatestContent_Paper() throws Exception {
        List<PaperDTO> latestPapers = Arrays.asList(mockPaper);
        when(paperService.getLatestPapers(10)).thenReturn(latestPapers);

        mockMvc.perform(get("/api/v1/contents/latest")
                .param("type", "paper")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("Test Paper"));
    }

    @Test
    @DisplayName("获取最新内容 - 专利类型")
    void getLatestContent_Patent() throws Exception {
        List<PatentDTO> latestPatents = Arrays.asList(mockPatent);
        when(patentService.getLatestPatents(10)).thenReturn(latestPatents);

        mockMvc.perform(get("/api/v1/contents/latest")
                .param("type", "patent")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].patentName").value("Test Patent"));
    }
}