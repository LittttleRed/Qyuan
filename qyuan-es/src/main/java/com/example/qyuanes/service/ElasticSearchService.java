package com.example.qyuanes.service;

import com.example.qyuanes.dto.PaperSearchRequest;
import com.example.qyuanes.dto.PaperSearchResponse;
import com.example.qyuanes.entity.Paper;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ElasticSearch服务层
 * 封装ES的CRUD操作
 */
@Slf4j   // Lombok注解：自动生成log日志对象
@Service   // Spring注解：标识这是业务逻辑层，会被Spring扫描并注册为Bean
public class ElasticSearchService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    // 索引名称常量
    private static final String INDEX_NAME = "papers";

    
/**
 * 搜索论文（核心方法）
 * 
 * @param request 搜索请求
 * @return 搜索结果
 */
public PaperSearchResponse searchPapers(PaperSearchRequest request) {
    try {
        // ========== 1. 参数校验和默认值设置 ==========
        // 确保分页参数有效
        if (request.getPage() == null || request.getPage() < 0) {
            request.setPage(0);
        }
        if (request.getSize() == null || request.getSize() < 1) {
            request.setSize(10);
        }
        if (request.getSize() > 100) {
            request.setSize(100); // 限制最大每页100条
        }
        
        // ========== 2. 构建查询对象 ==========
        // 使用查询构建器将DTO转换为ES查询
        Query query = PaperQueryBuilder.buildQuery(request);
        
        // ========== 3. 构建排序 ==========
        List<co.elastic.clients.elasticsearch._types.SortOptions> sortOptions = buildSortOptions(request);
        
        // ========== 4. 执行搜索 ==========
        SearchResponse<Paper> response = elasticsearchClient.search(s -> s
            .index(INDEX_NAME)                                    // 指定索引名称
            .query(query)                                         // 设置查询条件
            .from(request.getPage() * request.getSize())          // 分页起始位置（跳过前面的记录）
            .size(request.getSize())                               // 每页大小
            .sort(sortOptions)                                     // 排序
            .trackTotalHits(th -> th.enabled(true))                // 启用精确的total计数，不受10000条限制
            , Paper.class);                                        // 指定返回类型为Paper
        
        // ========== 5. 处理搜索结果 ==========
        return buildSearchResponse(response, request);
        
    } catch (IOException e) {
        log.error("搜索论文失败，请求参数: {}, 错误信息: {}", request, e.getMessage(), e);
        // 返回空结果，而不是抛出异常
        return buildEmptyResponse(request);
    } catch (Exception e) {
        log.error("搜索论文时发生未知错误，请求参数: {}, 错误信息: {}", request, e.getMessage(), e);
        // 返回空结果，而不是抛出异常
        return buildEmptyResponse(request);
    }
}

/**
 * 构建排序选项
 */
private List<co.elastic.clients.elasticsearch._types.SortOptions> buildSortOptions(PaperSearchRequest request) {
    List<co.elastic.clients.elasticsearch._types.SortOptions> sortList = new ArrayList<>();
    
    // 如果指定了多字段排序
    if (request.getSortOptions() != null && !request.getSortOptions().isEmpty()) {
        for (PaperSearchRequest.SortOption sortOption : request.getSortOptions()) {
            SortOrder order = "asc".equalsIgnoreCase(sortOption.getOrder()) 
                ? SortOrder.Asc 
                : SortOrder.Desc;
            sortList.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                .field(f -> f
                    .field(sortOption.getField())
                    .order(order)
                )
            ));
        }
    } else {
        // 单字段排序（默认）
        String sortField = StringUtils.hasText(request.getSortField()) 
            ? request.getSortField() 
            : "updated";
        SortOrder sortOrder = "asc".equalsIgnoreCase(request.getSortOrder()) 
            ? SortOrder.Asc 
            : SortOrder.Desc;
        
        sortList.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
            .field(f -> f
                .field(sortField)
                .order(sortOrder)
            )
        ));
    }
    
    return sortList;
}

/**
 * 构建搜索响应对象
 */
private PaperSearchResponse buildSearchResponse(SearchResponse<Paper> response, PaperSearchRequest request) {
    // 提取论文列表
    List<Paper> papers = response.hits().hits().stream()
        .map(Hit::source)              // 从Hit对象中提取Paper对象
        .filter(Objects::nonNull)       // 过滤掉null值
        .collect(Collectors.toList());
    
    // 获取总数
    long total = response.hits().total().value();
    
    // 计算总页数（向上取整）
    int totalPages = (int) Math.ceil((double) total / request.getSize());
    
    // 构建响应对象
    PaperSearchResponse searchResponse = new PaperSearchResponse();
    searchResponse.setPapers(papers);
    searchResponse.setTotal(total);
    searchResponse.setPage(request.getPage());
    searchResponse.setSize(request.getSize());
    searchResponse.setTotalPages(totalPages);
    
    log.info("搜索论文成功，条件: {}, 结果数: {}/{}", request, papers.size(), total);
    return searchResponse;
}

/**
 * 构建空响应（异常情况）
 */
private PaperSearchResponse buildEmptyResponse(PaperSearchRequest request) {
    PaperSearchResponse response = new PaperSearchResponse();
    response.setPapers(new ArrayList<>());
    response.setTotal(0L);
    response.setPage(request.getPage());
    response.setSize(request.getSize());
    response.setTotalPages(0);
    return response;
}


}

