package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.dto.PaperExportRequest;
import com.example.qyuanpaperrd.dto.PaperSearchRequest;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.dto.PaperDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaperController 完整集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DisplayName("Paper Controller Integration Tests")
public class PaperControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/papers";
    }

    @Test
    @DisplayName("测试搜索论文功能 - GET请求")
    void testSearchPapers_GetRequest_Success() {
        // Given
        String url = baseUrl + "/search?keyword=machine&page=1&size=10";

        // When
        ResponseEntity<Result> response = restTemplate.getForEntity(url, Result.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @DisplayName("测试搜索论文功能 - POST请求")
    void testSearchPapers_PostRequest_Success() {
        // Given
        PaperSearchRequest request = new PaperSearchRequest();
        request.setKeyword("deep learning");
        request.setPage(1);
        request.setSize(5);

        // When
        ResponseEntity<Result> response = restTemplate.postForEntity(baseUrl + "/search", request, Result.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @DisplayName("测试搜索论文功能 - 空关键词")
    void testSearchPapers_EmptyKeyword() {
        // Given
        String url = baseUrl + "/search?keyword=&page=1&size=10";

        // When
        ResponseEntity<Result> response = restTemplate.getForEntity(url, Result.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("测试获取热门论文")
    void testGetPopularPapers_Success() {
        // Given
        String url = baseUrl + "/popular?limit=5";

        // When
        ResponseEntity<Result> response = restTemplate.getForEntity(url, Result.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @DisplayName("测试获取最新论文")
    void testGetLatestPapers_Success() {
        // Given
        String url = baseUrl + "/latest?limit=5";

        // When
        ResponseEntity<Result> response = restTemplate.getForEntity(url, Result.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @DisplayName("测试导出论文 - BibTeX格式")
    void testExportPapers_BibTeX_Success() {
        // Given
        String url = baseUrl + "/export?paperIds=1,2,3&format=bibtex&includeCitations=false&includeAuthors=false&includeAbstract=false";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("@article{"));
    }

    @Test
    @DisplayName("测试导出论文 - RIS格式")
    void testExportPapers_RIS_Success() {
        // Given
        String url = baseUrl + "/export?paperIds=1,2,3&format=ris&includeCitations=true&includeAuthors=true&includeAbstract=true";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("TY  - JOUR"));
    }

    @Test
    @DisplayName("测试导出论文 - POST请求 BibTeX格式")
    void testExportPapers_PostRequest_BibTeX_Success() {
        // Given
        PaperExportRequest request = new PaperExportRequest();
        request.setPaperIds(Arrays.asList(1L, 2L, 3L));
        request.setFormat("bibtex");
        request.setIncludeCitations(false);
        request.setIncludeAuthors(false);
        request.setIncludeAbstract(false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaperExportRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/export", entity, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("@article{"));
    }

    @Test
    @DisplayName("测试导出论文 - POST请求 RIS格式")
    void testExportPapers_PostRequest_RIS_Success() {
        // Given
        PaperExportRequest request = new PaperExportRequest();
        request.setPaperIds(Arrays.asList(1L, 2L, 4L));
        request.setFormat("ris");
        request.setIncludeCitations(true);
        request.setIncludeAuthors(true);
        request.setIncludeAbstract(true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaperExportRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/export", entity, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("TY  - JOUR"));
    }

    @Test
    @DisplayName("测试导出请求 - 不支持的格式")
    void testExportPapers_UnsupportedFormat() {
        // Given
        String url = baseUrl + "/export?paperIds=1&format=pdf";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("不支持的导出格式"));
    }

    @Test
    @DisplayName("测试导出请求 - 未指定论文ID")
    void testExportPapers_NoPaperIds() {
        // Given
        String url = baseUrl + "/export?format=bibtex";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("论文ID列表不能为空"));
    }

    @Test
    @DisplayName("测试导出请求 - 空格式参数")
    void testExportPapers_EmptyFormat() {
        // Given
        String url = baseUrl + "/export?paperIds=1,2";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("导出格式不能为空"));
    }

    @Test
    @DisplayName("测试导出请求 - 无效的论文ID")
    void testExportPapers_InvalidPaperIds() {
        // Given
        String url = baseUrl + "/export?paperIds=99999,100000&format=bibtex";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // 应该返回空的导出结果，因为没有找到对应的论文
    }

    @Test
    @DisplayName("测试API响应格式一致性")
    void testApiResponseConsistency() {
        // Given
        String searchUrl = baseUrl + "/search?keyword=test&page=1&size=10";
        String popularUrl = baseUrl + "/popular?limit=5";
        String latestUrl = baseUrl + "/latest?limit=5";

        // When
        ResponseEntity<Result> searchResponse = restTemplate.getForEntity(searchUrl, Result.class);
        ResponseEntity<Result> popularResponse = restTemplate.getForEntity(popularUrl, Result.class);
        ResponseEntity<Result> latestResponse = restTemplate.getForEntity(latestUrl, Result.class);

        // Then
        // 验证所有API响应都有相同的基础结构
        for (ResponseEntity<Result> response : Arrays.asList(searchResponse, popularResponse, latestResponse)) {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertNotNull(response.getBody().getData());
            assertNotNull(response.getBody().getMessage());
            assertEquals(200, response.getBody().getCode());
        }
    }

    @Test
    @DisplayName("测试错误处理的一致性")
    void testErrorHandlingConsistency() {
        // Given
        String invalidExportUrl1 = baseUrl + "/export?paperIds=1&format=pdf";
        String invalidExportUrl2 = baseUrl + "/export?format=bibtex";

        // When
        ResponseEntity<String> response1 = restTemplate.getForEntity(invalidExportUrl1, String.class);
        ResponseEntity<String> response2 = restTemplate.getForEntity(invalidExportUrl2, String.class);

        // Then
        // 验证所有错误响应都返回400状态码
        for (ResponseEntity<String> response : Arrays.asList(response1, response2)) {
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isEmpty());
        }
    }

    @Test
    @DisplayName("测试性能基准 - 搜索响应时间")
    void testSearchPerformance() {
        // Given
        String url = baseUrl + "/search?keyword=machine&page=1&size=10";

        // When
        long startTime = System.currentTimeMillis();
        ResponseEntity<Result> response = restTemplate.getForEntity(url, Result.class);
        long endTime = System.currentTimeMillis();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());

        // 搜索操作应该在2秒内完成
        long duration = endTime - startTime;
        assertTrue(duration < 2000, "搜索响应时间过长: " + duration + "ms");
    }
}