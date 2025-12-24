package com.example.qyuanes.service;

import com.example.qyuanes.dto.PaperSearchRequest;
import com.example.qyuanes.dto.PaperSearchResponse;
import com.example.qyuanes.dto.PaperWithHighlight;
import com.example.qyuanes.dto.SearchSuggestionResponse;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    
    @Autowired(required = false)
    private HotSearchService hotSearchService;  // 使用required=false，即使Redis不可用也不影响搜索功能

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
        
        // ========== 4. 构建高亮配置 ==========
        // 如果有关键词搜索，才需要高亮
        boolean needHighlight = StringUtils.hasText(request.getKeyword());
        
        // ========== 5. 执行搜索 ==========
        SearchResponse<Paper> response = elasticsearchClient.search(s -> {
            var searchBuilder = s
                .index(INDEX_NAME)                                    // 指定索引名称
                .query(query)                                         // 设置查询条件
                .from(request.getPage() * request.getSize())          // 分页起始位置（跳过前面的记录）
                .size(request.getSize())                               // 每页大小
                .sort(sortOptions)                                     // 排序
                .trackTotalHits(th -> th.enabled(true));               // 启用精确的total计数，不受10000条限制
            
            // 如果有关键词搜索，添加高亮配置
            if (needHighlight) {
                searchBuilder.highlight(h -> h
                    .preTags("<em>")                                   // 全局高亮开始标签（列表形式）
                    .postTags("</em>")                                 // 全局高亮结束标签（列表形式）
                    .fields("title", f -> f                           // 标题字段高亮
                        .numberOfFragments(1)                          // 只返回1个片段（完整标题）
                        .fragmentSize(150)                             // 片段大小（字符数），对title通常不需要片段化
                        .requireFieldMatch(true)                       // 只高亮匹配查询的字段
                    )
                    .fields("abstract", f -> f                        // 摘要字段高亮
                        .numberOfFragments(3)                          // 返回最多3个片段
                        .fragmentSize(200)                             // 每个片段200字符
                        .requireFieldMatch(true)                       // 只高亮匹配查询的字段
                    )
                    .fields("journal_source", f -> f                  // 期刊来源字段高亮
                        .numberOfFragments(1)                          // 只返回1个片段
                        .fragmentSize(100)                             // 片段大小
                        .requireFieldMatch(true)                       // 只高亮匹配查询的字段
                    )
                );
            }
            
            return searchBuilder;
        }, Paper.class);                                              // 指定返回类型为Paper
        
        // ========== 6. 处理搜索结果 ==========
        PaperSearchResponse searchResponse = buildSearchResponse(response, request, needHighlight);
        
        // ========== 7. 更新热门搜索（异步处理，不阻塞搜索响应） ==========
        // 如果有关键词搜索，记录到热门搜索
        if (needHighlight && hotSearchService != null && StringUtils.hasText(request.getKeyword())) {
            try {
                // 异步更新热门搜索，避免影响搜索响应时间
                hotSearchService.updateHotSearch(request.getKeyword());
            } catch (Exception e) {
                // 热门搜索更新失败不应该影响搜索结果，只记录日志
                log.warn("更新热门搜索失败，keyword: {}, 错误: {}", request.getKeyword(), e.getMessage());
            }
        }
        
        return searchResponse;
        
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
 * 构建搜索响应对象（带高亮）
 */
private PaperSearchResponse buildSearchResponse(SearchResponse<Paper> response, PaperSearchRequest request, boolean needHighlight) {
    // 提取论文列表（带高亮信息）
    List<PaperWithHighlight> papersWithHighlight = response.hits().hits().stream()
        .map(hit -> {
            Paper paper = hit.source();
            if (paper == null) {
                return null;
            }
            
            // 创建带高亮的Paper对象
            PaperWithHighlight paperWithHighlight = new PaperWithHighlight(paper);
            
            // 如果有高亮配置，提取高亮信息
            if (needHighlight && hit.highlight() != null) {
                Map<String, List<String>> highlightMap = hit.highlight();
                
                // 提取每个字段的高亮片段
                for (Map.Entry<String, List<String>> entry : highlightMap.entrySet()) {
                    String field = entry.getKey();
                    List<String> fragments = entry.getValue();
                    paperWithHighlight.addHighlights(field, fragments);
                }
            }
            
            return paperWithHighlight;
        })
        .filter(Objects::nonNull)       // 过滤掉null值
        .collect(Collectors.toList());
    
    // 获取总数
    long total = response.hits().total().value();
    
    // 计算总页数（向上取整）
    int totalPages = (int) Math.ceil((double) total / request.getSize());
    
    // 构建响应对象
    PaperSearchResponse searchResponse = new PaperSearchResponse();
    searchResponse.setPapers(papersWithHighlight);
    searchResponse.setTotal(total);
    searchResponse.setPage(request.getPage());
    searchResponse.setSize(request.getSize());
    searchResponse.setTotalPages(totalPages);
    
    log.info("搜索论文成功，条件: {}, 结果数: {}/{}", request, papersWithHighlight.size(), total);
    return searchResponse;
}

/**
 * 构建空响应（异常情况）
 */
private PaperSearchResponse buildEmptyResponse(PaperSearchRequest request) {
    PaperSearchResponse response = new PaperSearchResponse();
    response.setPapers(new ArrayList<>());  // 空列表
    response.setTotal(0L);
    response.setPage(request.getPage());
    response.setSize(request.getSize());
    response.setTotalPages(0);
    return response;
}

/**
 * 搜索建议/自动补全
 * 
 * 根据用户输入的前缀，返回可能的完整搜索词建议
 * 主要用于前端搜索框的自动补全功能
 * 
 * @param prefix 用户输入的前缀（如"软件"）
 * @param size 返回的建议数量（默认10，最大50）
 * @return 搜索建议列表
 * 
 * 示例：
 * 输入: "软件"
 * 输出: ["软件咨询服务业", "软件工程与地理信息系统设计课程", "软件总线体系", ...]
 */
public SearchSuggestionResponse searchSuggestions(String prefix, Integer size) {
    try {
        // ========== 1. 参数校验 ==========
        if (!StringUtils.hasText(prefix)) {
            return new SearchSuggestionResponse("", new ArrayList<>());
        }
        
        // 限制建议数量，避免返回过多数据
        int suggestionSize = 10;
        
        // 去掉前后空格
        String trimmedPrefix = prefix.trim();
        
        // ========== 2. 构建前缀查询 ==========
        // 在title、abstract、journal_source字段中搜索前缀匹配
        Query query = Query.of(q -> q
            .bool(b -> b
                .should(s -> s
                    .prefix(p -> p
                        .field("title")
                        .value(trimmedPrefix)
                        .boost(3.0f)  // title字段权重最高
                    )
                )
                .should(s -> s
                    .prefix(p -> p
                        .field("abstract")
                        .value(trimmedPrefix)
                        .boost(2.0f)  // abstract字段权重中等
                    )
                )
                .should(s -> s
                    .prefix(p -> p
                        .field("journal_source")
                        .value(trimmedPrefix)
                        .boost(1.0f)  // journal_source字段权重较低
                    )
                )
                .minimumShouldMatch("1")  // 至少匹配一个字段
            )
        );
        
        // ========== 3. 执行搜索 ==========
        SearchResponse<Paper> response = elasticsearchClient.search(s -> s
            .index(INDEX_NAME)
            .query(query)
            .size(suggestionSize * 3)  // 多查询一些，后续去重和筛选
            .sort(sort -> sort
                .field(f -> f
                    .field("_score")  // 按相关性评分排序
                    .order(SortOrder.Desc)
                )
            )
            .source(src -> src
                .filter(f -> f
                    .includes("title", "abstract", "journal_source")  // 只返回需要的字段，提高性能
                )
            )
            , Paper.class);
        
        // ========== 4. 提取并处理建议 ==========
        Set<String> suggestionsSet = new LinkedHashSet<>();  // 使用LinkedHashSet保持顺序并去重
        
        for (Hit<Paper> hit : response.hits().hits()) {
            Paper paper = hit.source();
            if (paper == null) {
                continue;
            }
            
            // 从title字段提取建议（优先级最高）
            if (StringUtils.hasText(paper.getTitle()) && paper.getTitle().startsWith(prefix)) {
                suggestionsSet.add(paper.getTitle());
                if (suggestionsSet.size() >= suggestionSize) {
                    break;
                }
            }
            
            // 如果还没达到数量，从abstract中提取（提取包含前缀的短语）
            if (suggestionsSet.size() < suggestionSize 
                && StringUtils.hasText(paper.getAbstractContent())) {
                // 在摘要中查找包含前缀的短语
                String[] words = paper.getAbstractContent().split("[，。；：！？\\s]+");
                for (String word : words) {
                    if (word.startsWith(prefix) && word.length() > prefix.length()) {
                        suggestionsSet.add(word);
                        if (suggestionsSet.size() >= suggestionSize) {
                            break;
                        }
                    }
                }
            }
            
            // 从journal_source中提取
            if (suggestionsSet.size() < suggestionSize 
                && StringUtils.hasText(paper.getJournal_source()) 
                && paper.getJournal_source().startsWith(prefix)) {
                suggestionsSet.add(paper.getJournal_source());
                if (suggestionsSet.size() >= suggestionSize) {
                    break;
                }
            }
            
            // 如果已经收集到足够的建议，提前退出
            if (suggestionsSet.size() >= suggestionSize) {
                break;
            }
        }
        
        // 转换为List（保持顺序）
        List<String> suggestions = new ArrayList<>(suggestionsSet);
        
        // 如果建议数量不足，尝试更灵活的匹配（匹配字段中任意位置包含前缀的完整词）
        if (suggestions.size() < suggestionSize) {
            // 可以在这里添加更复杂的逻辑，比如提取字段中所有包含前缀的词
            // 但为了避免过度复杂，先返回已有的结果
        }
        
        log.info("搜索建议完成，前缀: {}, 返回建议数: {}", prefix, suggestions.size());
        return new SearchSuggestionResponse(prefix, suggestions);
        
    } catch (IOException e) {
        log.error("搜索建议失败，前缀: {}, 错误信息: {}", prefix, e.getMessage(), e);
        return new SearchSuggestionResponse(prefix, new ArrayList<>());
    } catch (Exception e) {
        log.error("搜索建议时发生未知错误，前缀: {}, 错误信息: {}", prefix, e.getMessage(), e);
        return new SearchSuggestionResponse(prefix, new ArrayList<>());
    }
}


}

