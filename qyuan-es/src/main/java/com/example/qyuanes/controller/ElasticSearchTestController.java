package com.example.qyuanes.controller;

import com.example.qyuanes.dto.PaperSearchRequest;
import com.example.qyuanes.dto.PaperSearchResponse;
import com.example.qyuanes.entity.Paper;
import com.example.qyuanes.service.ElasticSearchService;
import org.example.qyuancommon.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * ElasticSearch测试控制器
 * 提供ES的CRUD操作接口
 */

@RestController  //Spring注解：标识这是一个REST控制器，返回值自动转为JSON
@RequestMapping("/es")    // Spring注解：设置基础路径，所有接口都会加上这个前缀
public class ElasticSearchTestController {

    @Autowired
    private ElasticSearchService elasticSearchService; 
    /**
    * 简单搜索接口
    * 适合快速查询，只需要关键词
    * 
    * GET /es/paper/search/simple?keyword=机器学习&page=0&size=10
    */
    @GetMapping("/paper/search/simple")
    public Result<PaperSearchResponse> simpleSearch(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer size) {

    // 构建简单搜索请求
    PaperSearchRequest request = new PaperSearchRequest();
    request.setKeyword(keyword);
    request.setPage(page);
    request.setSize(size);

    PaperSearchResponse response = elasticSearchService.searchPapers(request);
    return Result.ok(response, "搜索成功");
    }
/**
 * 复杂搜索接口（POST方式）
 * 支持所有搜索条件，适合复杂查询场景
 * 
 * POST /es/paper/search
 */
@PostMapping("/paper/search")
public Result<PaperSearchResponse> advancedSearch(@RequestBody PaperSearchRequest request) {
    // 参数验证（Spring会自动使用@Valid注解进行验证）
    PaperSearchResponse response = elasticSearchService.searchPapers(request);
    return Result.ok(response, "搜索成功");
}
/**
 * 复杂搜索接口（GET方式）
 * 通过URL参数传递查询条件
 * 
 * GET /es/paper/search?keyword=AI&categoryId=1&startTime=2024-01-01T00:00:00Z
 */

@GetMapping("/paper/search")
public Result<PaperSearchResponse> advancedSearchByGet(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false) List<Integer> categoryIds,
        @RequestParam(required = false) String submitter,
        @RequestParam(required = false) String journalSource,
        @RequestParam(required = false) String startTime,
        @RequestParam(required = false) String endTime,
        @RequestParam(required = false) Integer minReadCount,
        @RequestParam(required = false) Integer maxReadCount,
        @RequestParam(required = false) Integer minFavoriteCount,
        @RequestParam(required = false) Integer maxFavoriteCount,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer size,
        @RequestParam(required = false) String sortField,
        @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
    
    // 构建搜索请求对象
    PaperSearchRequest request = new PaperSearchRequest();
    request.setKeyword(keyword);
    request.setCategoryId(categoryId);
    request.setCategoryIds(categoryIds);
    request.setSubmitter(submitter);
    request.setJournalSource(journalSource);
    request.setMinReadCount(minReadCount);
    request.setMaxReadCount(maxReadCount);
    request.setMinFavoriteCount(minFavoriteCount);
    request.setMaxFavoriteCount(maxFavoriteCount);
    request.setPage(page);
    request.setSize(size);
    request.setSortField(sortField);
    request.setSortOrder(sortOrder);
    
    // 解析时间字符串
    if (StringUtils.hasText(startTime)) {
        try {
            request.setStartTime(OffsetDateTime.parse(startTime));
        } catch (Exception e) {
            return Result.fail("开始时间格式错误，请使用ISO-8601格式");
        }
    }
    if (StringUtils.hasText(endTime)) {
        try {
            request.setEndTime(OffsetDateTime.parse(endTime));
        } catch (Exception e) {
            return Result.fail("结束时间格式错误，请使用ISO-8601格式");
        }
    }
    
    PaperSearchResponse response = elasticSearchService.searchPapers(request);
    return Result.ok(response, "搜索成功");
}



}

