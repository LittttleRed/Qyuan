package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.ClaimRequest;
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

import java.util.Arrays;

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

    private ClaimRequest mockClaimRequest;

    @BeforeEach
    void setUp() {
        mockClaimRequest = new ClaimRequest();
        mockClaimRequest.setPaperId(1L);
        // claimPicture 是 MultipartFile 类型，需要 mock 对象
        // 在实际测试中应该使用 MockMultipartFile
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("认领论文 - 成功")
    void claimPaper_Success() throws Exception {
        doNothing().when(userInteractionService).claimPaper(anyLong(), any(ClaimRequest.class));

        mockMvc.perform(post("/paper/user/claims")
                .header("USER-ID", "1")
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

        mockMvc.perform(get("/paper/user/{userId}/claims", 1L)
                .param("status", "1")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(1));
    }
}
