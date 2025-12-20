package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.ClaimRequest;
import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.service.UserInteractionService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserInteractionController.class)
@Import(com.example.qyuanpaperrd.config.TestSecurityConfig.class)
@DisplayName("UserInteractionController 测试")
class UserInteractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserInteractionService userInteractionService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaperDTO mockPaper;
    private PageResult<PaperDTO> mockPageResult;
    private ClaimRequest mockClaimRequest;

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

        mockClaimRequest = new ClaimRequest();
        mockClaimRequest.setPaperId(1L);
        mockClaimRequest.setClaimDescription("I am author");
        mockClaimRequest.setClaimPicture("Evidence picture");
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("收藏论文 - 成功")
    void favoritePaper_Success() throws Exception {
        doNothing().when(userInteractionService).favoritePaper(anyLong(), anyLong());

        mockMvc.perform(post("/api/v1/user/{userId}/favorites/{paperId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("收藏成功"))
                .andExpect(jsonPath("$.data").value("success"));
    }

    @Test
    @WithMockUser(username = "user2", authorities = {"ROLE_USER"})
    @DisplayName("取消收藏 - 成功")
    void unfavoritePaper_Success() throws Exception {
        doNothing().when(userInteractionService).unfavoritePaper(anyLong(), anyLong());

        mockMvc.perform(delete("/api/v1/user/{userId}/favorites/{paperId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("取消收藏成功"))
                .andExpect(jsonPath("$.data").value("success"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("获取收藏列表 - 成功")
    void getUserFavorites_Success() throws Exception {
        when(userInteractionService.getUserFavorites(anyLong(), anyInt(), anyInt())).thenReturn(mockPageResult);

        mockMvc.perform(get("/api/v1/user/{userId}/favorites", 1L)
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Test Paper"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("认领论文 - 成功")
    void claimPaper_Success() throws Exception {
        doNothing().when(userInteractionService).claimPaper(anyLong(), any(ClaimRequest.class));

        mockMvc.perform(post("/api/v1/user/{userId}/claims", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockClaimRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("认领申请已提交"))
                .andExpect(jsonPath("$.data").value("success"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("获取认领记录 - 成功")
    void getUserClaims_Success() throws Exception {
        PageResult<Object> claimResult = new PageResult<>();
        claimResult.setRecords(Arrays.asList(mockClaimRequest));
        claimResult.setCurrent(1L);
        claimResult.setSize(20L);
        claimResult.setTotal(1L);
        claimResult.setPages(1L);
        
        when(userInteractionService.getUserClaims(anyLong(), anyInt(), anyInt(), anyInt())).thenReturn(claimResult);

        mockMvc.perform(get("/api/v1/user/{userId}/claims", 1L)
                .param("status", "1")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(1));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("获取浏览历史 - 成功")
    void getUserHistory_Success() throws Exception {
        when(userInteractionService.getUserHistory(anyLong(), anyInt(), anyInt())).thenReturn(mockPageResult);

        mockMvc.perform(get("/api/v1/user/{userId}/history", 1L)
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Test Paper"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("记录浏览 - 成功")
    void recordView_Success() throws Exception {
        doNothing().when(userInteractionService).recordView(anyLong(), anyLong());

        mockMvc.perform(post("/api/v1/user/{userId}/record-view/{paperId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("浏览记录已保存"))
                .andExpect(jsonPath("$.data").value("success"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("获取用户档案 - 成功")
    void getUserProfile_Success() throws Exception {
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", 1L);
        profile.put("favoriteCount", 5);
        profile.put("claimCount", 2);
        profile.put("viewCount", 100);
        
        when(userInteractionService.getUserProfile(anyLong())).thenReturn(profile);

        mockMvc.perform(get("/api/v1/user/{userId}/profile", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.favoriteCount").value(5));
    }
}
