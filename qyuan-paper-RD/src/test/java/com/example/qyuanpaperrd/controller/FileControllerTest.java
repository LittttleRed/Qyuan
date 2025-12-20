package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.service.HuaweiObsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FileController.class)
@Import(com.example.qyuanpaperrd.config.TestSecurityConfig.class)
@TestPropertySource(properties = {
    "huawei.obs.enabled=true"
})
@DisplayName("FileController 测试")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HuaweiObsService obsService;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
            "file", 
            "test.pdf", 
            MediaType.APPLICATION_PDF_VALUE, 
            "test file content".getBytes()
        );
    }

    @Test
    @WithMockUser
    @DisplayName("上传文件 - 成功")
    void uploadFile_Success() throws Exception {
        when(obsService.uploadFile(any(MockMultipartFile.class), anyString()))
            .thenReturn("papers/test-123.pdf");
        when(obsService.getPresignedUrl(anyString()))
            .thenReturn("http://example.com/presigned-url");

        mockMvc.perform(multipart("/paper/files/upload")
                .file(mockFile)
                .param("folder", "papers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objectKey").value("papers/test-123.pdf"))
                .andExpect(jsonPath("$.url").value("http://example.com/presigned-url"))
                .andExpect(jsonPath("$.message").value("文件上传成功"));
    }

    @Test
    @WithMockUser
    @DisplayName("获取文件URL - 成功")
    void getFileUrl_Success() throws Exception {
        when(obsService.getPresignedUrl("test-file.pdf"))
            .thenReturn("http://example.com/presigned-url");

        mockMvc.perform(get("/paper/files/url/{objectKey}", "test-file.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objectKey").value("test-file.pdf"))
                .andExpect(jsonPath("$.url").value("http://example.com/presigned-url"));
    }

    @Test
    @WithMockUser
    @DisplayName("删除文件 - 成功")
    void deleteFile_Success() throws Exception {
        mockMvc.perform(delete("/paper/files/{objectKey}", "test-file.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("文件删除成功"))
                .andExpect(jsonPath("$.objectKey").value("test-file.pdf"));
    }

    @Test
    @WithMockUser
    @DisplayName("检查文件是否存在 - 存在")
    void checkFileExists_Exists() throws Exception {
        when(obsService.doesObjectExist("test-file.pdf")).thenReturn(true);

        mockMvc.perform(get("/paper/files/exists/{objectKey}", "test-file.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objectKey").value("test-file.pdf"))
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("检查文件是否存在 - 不存在")
    void checkFileExists_NotExists() throws Exception {
        when(obsService.doesObjectExist("test-file.pdf")).thenReturn(false);

        mockMvc.perform(get("/paper/files/exists/{objectKey}", "test-file.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objectKey").value("test-file.pdf"))
                .andExpect(jsonPath("$.exists").value(false));
    }
}
