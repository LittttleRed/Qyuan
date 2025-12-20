package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.dto.PatentDTO;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PatentController.class)
@Import(com.example.qyuanpaperrd.config.TestSecurityConfig.class)
@DisplayName("PatentController 测试")
class PatentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatentService patentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PatentDTO mockPatent;
    private PageResult<PatentDTO> mockPageResult;

    @BeforeEach
    void setUp() {
        mockPatent = new PatentDTO();
        mockPatent.setPatentNumber("US20230012345A");
        mockPatent.setPatentName("Test Patent");
        mockPatent.setAbstractText("Test abstract");
        mockPatent.setInventor("John Doe");
        mockPatent.setAssignee("Test Company");
        mockPatent.setCountry("US");
        mockPatent.setApplicationDate(LocalDateTime.of(2023, 1, 15, 0, 0));
        mockPatent.setAuthorizationDate(LocalDateTime.of(2023, 5, 20, 0, 0));
        mockPatent.setCitationCount(10);

        mockPageResult = new PageResult<>();
        mockPageResult.setRecords(Arrays.asList(mockPatent));
        mockPageResult.setCurrent(1L);
        mockPageResult.setSize(10L);
        mockPageResult.setTotal(1L);
        mockPageResult.setPages(1L);
    }

    @Test
    @DisplayName("获取专利详情 - 成功")
    void getPatentByNumber_Success() throws Exception {
        when(patentService.getPatentByNumber("US20230012345A", null))
                .thenReturn(mockPatent);

        mockMvc.perform(get("/paper/patents/{patentNumber}", "US20230012345A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.patentNumber").value("US20230012345A"))
                .andExpect(jsonPath("$.data.patentName").value("Test Patent"));
    }

    @Test
    @DisplayName("获取专利详情 - 专利不存在")
    void getPatentByNumber_NotFound() throws Exception {
        when(patentService.getPatentByNumber("INVALID123", null))
                .thenReturn(null);

        mockMvc.perform(get("/paper/patents/{patentNumber}", "INVALID123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("专利不存在"));
    }

    @Test
    @DisplayName("搜索专利 - 关键词搜索")
    void searchPatents_ByKeyword() throws Exception {
        when(patentService.searchPatents(eq("test"), eq(null), eq(null), eq(null), eq(1), eq(10)))
                .thenReturn(mockPageResult);

        mockMvc.perform(get("/paper/patents/search")
                .param("keyword", "test")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].patentName").value("Test Patent"));
    }

    @Test
    @DisplayName("搜索专利 - 按发明人搜索")
    void searchPatents_ByInventor() throws Exception {
        when(patentService.searchPatents(eq(null), eq("John Doe"), eq(null), eq(null), eq(1), eq(10)))
                .thenReturn(mockPageResult);

        mockMvc.perform(get("/paper/patents/search")
                .param("inventor", "John Doe")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].inventor").value("John Doe"));
    }

    @Test
    @DisplayName("搜索专利 - 按专利权人搜索")
    void searchPatents_ByAssignee() throws Exception {
        when(patentService.searchPatents(eq(null), eq(null), eq("Test Company"), eq(null), eq(1), eq(10)))
                .thenReturn(mockPageResult);

        mockMvc.perform(get("/paper/patents/search")
                .param("assignee", "Test Company")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].assignee").value("Test Company"));
    }

    @Test
    @DisplayName("搜索专利 - 按国家搜索")
    void searchPatents_ByCountry() throws Exception {
        when(patentService.searchPatents(eq(null), eq(null), eq(null), eq("US"), eq(1), eq(10)))
                .thenReturn(mockPageResult);

        mockMvc.perform(get("/paper/patents/search")
                .param("country", "US")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].country").value("US"));
    }

    @Test
    @DisplayName("搜索专利 - 无参数默认搜索")
    void searchPatents_Default() throws Exception {
        when(patentService.searchPatents(eq(null), eq(null), eq(null), eq(null), eq(1), eq(10)))
                .thenReturn(mockPageResult);

        mockMvc.perform(get("/paper/patents/search")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].patentName").value("Test Patent"));
    }
}
