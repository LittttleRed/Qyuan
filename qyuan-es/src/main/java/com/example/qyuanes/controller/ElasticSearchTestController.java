package com.example.qyuanes.controller;

import com.example.qyuanes.dto.HotSearchResponse;
import com.example.qyuanes.dto.PaperSearchRequest;
import com.example.qyuanes.dto.PaperSearchResponse;
import com.example.qyuanes.dto.SearchSuggestionResponse;
import com.example.qyuanes.entity.Paper;
import com.example.qyuanes.service.ElasticSearchService;
import com.example.qyuanes.service.HotSearchService;
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
    
    @Autowired(required = false)
    private HotSearchService hotSearchService; 
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
        @RequestParam(required = false) OffsetDateTime startTime,
        @RequestParam(required = false) OffsetDateTime endTime,
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
    request.setStartTime(startTime);
    request.setEndTime(endTime);
    request.setMinReadCount(minReadCount);
    request.setMaxReadCount(maxReadCount);
    request.setMinFavoriteCount(minFavoriteCount);
    request.setMaxFavoriteCount(maxFavoriteCount);
    request.setPage(page);
    request.setSize(size);
    request.setSortField(sortField);
    request.setSortOrder(sortOrder);
    

    
    PaperSearchResponse response = elasticSearchService.searchPapers(request);
    return Result.ok(response, "搜索成功");
}

/**
 * 搜索建议/自动补全接口
 * 根据用户输入的前缀返回搜索建议
 * 
 * GET /es/paper/suggest?prefix=软件&size=10
 * 
 * @param prefix 搜索前缀（用户正在输入的内容）
 * @param size 返回的建议数量（可选，默认10，最大50）
 * @return 搜索建议列表
 */
@GetMapping("/paper/suggest")
public Result<SearchSuggestionResponse> getSearchSuggestions(
        @RequestParam(required = false) String prefix,
        @RequestParam(required = false, defaultValue = "10") Integer size) {
    
    // 如果没有输入前缀，返回空建议
    if (prefix == null || prefix.trim().isEmpty()) {
        SearchSuggestionResponse emptyResponse = new SearchSuggestionResponse("", new java.util.ArrayList<>());
        return Result.ok(emptyResponse, "无搜索建议");
    }
    
    SearchSuggestionResponse response = elasticSearchService.searchSuggestions(prefix, size);
    return Result.ok(response, "获取搜索建议成功");
}

/**
 * 获取热门搜索关键词列表
 * 返回最近最常搜索的N个关键词
 * 
 * GET /es/paper/hot-search?size=10
 * 
 * @param size 返回的数量（可选，默认10）
 * @return 热门搜索关键词列表，按搜索次数降序排列
 */
@GetMapping("/paper/hot-search")
public Result<HotSearchResponse> getHotSearches(
        @RequestParam(required = false, defaultValue = "10") Integer size) {
    
    if (hotSearchService == null) {
        return Result.fail("热门搜索功能暂未启用");
    }
    HotSearchResponse response = hotSearchService.getHotSearches(size);
    return Result.ok(response, "获取热门搜索成功");
}

@GetMapping("/paper/hot-paper")
    public Result<Object> getHotPapers(
        @RequestParam(required = false, defaultValue = "10") Integer size) {

    Result<Object> response = elasticSearchService.getHotPapers(size);
    return Result.ok(response, "获取热门论文成功");
}


}

