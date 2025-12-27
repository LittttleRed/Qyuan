package com.example.qyuanes.service;

import com.example.qyuanes.dto.PaperSearchRequest;
import com.example.qyuanes.dto.PaperSearchResponse;
import com.example.qyuanes.dto.PaperWithHighlight;
import com.example.qyuanes.dto.SimpleSearchRequest;
import com.example.qyuanes.dto.PatentSearchResponse;
import com.example.qyuanes.dto.PatentWithHighlight;
import com.example.qyuanes.dto.JournalSearchResponse;
import com.example.qyuanes.dto.JournalWithHighlight;
import com.example.qyuanes.dto.SearchSuggestionResponse;
import com.example.qyuanes.entity.Paper;
import com.example.qyuanes.entity.Patent;
import com.example.qyuanes.entity.Journal;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
import org.example.qyuancommon.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
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
    
    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    // 索引名称常量
    private static final String PAPER_INDEX_NAME = "papers";
    private static final String PATENT_INDEX_NAME = "patents";
    private static final String JOURNAL_INDEX_NAME = "journals";

    
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
                    .index(PAPER_INDEX_NAME)
                    .query(query)  // query已经包含了boost权重，会产生_score
                    .from(request.getPage() * request.getSize())
                    .size(request.getSize())
                    .sort(sortOptions)
                    .trackTotalHits(th -> th.enabled(true));


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
        if(sortField.equals("relate")){
            sortList.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                    .score(scoreSort -> scoreSort
                            .order(SortOrder.Desc)
                    )
            ));
        }else{
        sortList.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
            .field(f -> f
                .field(sortField)
                .order(sortOrder)
            )
        ));
        }
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
 */public SearchSuggestionResponse searchSuggestions(String prefix, Integer size) {
    try {
        // ========== 1. 参数校验 ==========
        if (!StringUtils.hasText(prefix)) {
            return new SearchSuggestionResponse("", new ArrayList<>());
        }

        // 限制建议数量，避免返回过多数据
        int suggestionSize = size != null && size > 0 ? Math.min(size, 50) : 10;

        // 去掉前后空格
        String trimmedPrefix = prefix.trim();

        // ========== 2. 构建更灵活的查询 ==========
        // 使用多种查询方式来扩展匹配范围
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
                        // 添加包含匹配查询，扩展匹配范围
                        .should(s -> s
                                .match(m -> m
                                        .field("title")
                                        .query(trimmedPrefix)
                                        .fuzziness("AUTO")  // 自动模糊匹配
                                        .boost(2.5f)
                                )
                        )
                        .should(s -> s
                                .match(m -> m
                                        .field("abstract")
                                        .query(trimmedPrefix)
                                        .fuzziness("AUTO")  // 自动模糊匹配
                                        .boost(1.5f)
                                )
                        )
                        .should(s -> s
                                .match(m -> m
                                        .field("journal_source")
                                        .query(trimmedPrefix)
                                        .fuzziness("AUTO")  // 自动模糊匹配
                                        .boost(0.8f)
                                )
                        )
                        // 添加通配符查询以支持中间匹配
                        .should(s -> s
                                .wildcard(w -> w
                                        .field("title")
                                        .value("*" + trimmedPrefix + "*")
                                        .caseInsensitive(true)
                                        .boost(2.0f)
                                )
                        )
                        .should(s -> s
                                .wildcard(w -> w
                                        .field("abstract")
                                        .value("*" + trimmedPrefix + "*")
                                        .caseInsensitive(true)
                                        .boost(1.2f)
                                )
                        )
                        .should(s -> s
                                .wildcard(w -> w
                                        .field("journal_source")
                                        .value("*" + trimmedPrefix + "*")
                                        .caseInsensitive(true)
                                        .boost(0.6f)
                                )
                        )
                        .minimumShouldMatch("1")  // 至少匹配一个字段
                )
        );

        // ========== 3. 执行搜索 ==========
        SearchResponse<Paper> response = elasticsearchClient.search(s -> s
                        .index(PAPER_INDEX_NAME)
                        .query(query)
                        .size(suggestionSize * 5)  // 多查询一些，后续去重和筛选
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
            if (StringUtils.hasText(paper.getTitle())) {
                // 检查是否包含前缀，支持任意位置匹配
                if (paper.getTitle().toLowerCase().contains(trimmedPrefix.toLowerCase())) {
                    // 如果是前缀匹配，优先级更高
                    if (paper.getTitle().toLowerCase().startsWith(trimmedPrefix.toLowerCase())) {
                        suggestionsSet.add(paper.getTitle());
                    } else {
                        // 如果是包含匹配，添加到列表末尾
                        suggestionsSet.add(paper.getTitle());
                    }
                }
            }

            // 从abstract中提取包含前缀的短语
            if (suggestionsSet.size() < suggestionSize
                    && StringUtils.hasText(paper.getAbstractContent())) {
                // 在摘要中查找包含前缀的短语
                String[] sentences = paper.getAbstractContent().split("[，。；：！？\\s]+");
                for (String sentence : sentences) {
                    if (sentence.toLowerCase().contains(trimmedPrefix.toLowerCase())) {
                        // 提取包含前缀的短语或句子
                        int startIndex = sentence.toLowerCase().indexOf(trimmedPrefix.toLowerCase());
                        int start = Math.max(0, startIndex - 20);
                        int end = Math.min(sentence.length(), startIndex + trimmedPrefix.length() + 20);
                        String phrase = sentence.substring(start, end).trim();
                        if (phrase.length() > trimmedPrefix.length()) {
                            suggestionsSet.add(phrase);
                        }
                        if (suggestionsSet.size() >= suggestionSize) {
                            break;
                        }
                    }
                }
            }

            // 从journal_source中提取
            if (suggestionsSet.size() < suggestionSize
                    && StringUtils.hasText(paper.getJournal_source())
                    && paper.getJournal_source().toLowerCase().contains(trimmedPrefix.toLowerCase())) {
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

        Function<String, String> normalize = s ->
                s.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        Map<String, String> normalizedMap = new LinkedHashMap<>();
        for (String suggestion : suggestions) {
            String normalized = normalize.apply(suggestion);
            // 如果规范化后的字符串尚未出现，或当前原始字符串更短/更简单，则保留
            if (!normalizedMap.containsKey(normalized)) {
                normalizedMap.put(normalized, suggestion);
            } else {
                // 可以选择保留较短的原始字符串
                String existing = normalizedMap.get(normalized);
                if (suggestion.length() < existing.length() ||
                        suggestion.replaceAll("[^a-zA-Z0-9]", "").length() >
                                existing.replaceAll("[^a-zA-Z0-9]", "").length()) {
                    normalizedMap.put(normalized, suggestion);
                }
            }
        }

        List<String> deduplicated = new ArrayList<>(normalizedMap.values());

        if (deduplicated.size() > suggestionSize) {
            deduplicated = deduplicated.subList(0, suggestionSize);
        }


        log.info("搜索建议完成，前缀: {}, 返回建议数: {}", prefix, deduplicated.size());
        return new SearchSuggestionResponse(prefix, deduplicated);

    } catch (IOException e) {
        log.error("搜索建议失败，前缀: {}, 错误信息: {}", prefix, e.getMessage(), e);
        return new SearchSuggestionResponse(prefix, new ArrayList<>());
    } catch (Exception e) {
        log.error("搜索建议时发生未知错误，前缀: {}, 错误信息: {}", prefix, e.getMessage(), e);
        return new SearchSuggestionResponse(prefix, new ArrayList<>());
    }
}


    public Result<Object> getHotPapers(Integer size) {
        try {
            String key = "hot:papers:top10";
            
            // 获取hash结构中的所有论文数据
            Map<Object, Object> paperHash = redisTemplate.opsForHash().entries(key);
            
            if (paperHash == null || paperHash.isEmpty()) {
                log.info("Redis中没有热门论文数据");
                return Result.ok(new ArrayList<>());
            }
            
            // 限制返回数量，默认10，最大20
            int limit = (size != null && size > 0) ? Math.min(size, 20) : 10;
            
            List<Paper> hotPapers = new ArrayList<>();
            
            // 遍历hash中的数据并反序列化为Paper对象
            for (Map.Entry<Object, Object> entry : paperHash.entrySet()) {
                String paperJson = (String) entry.getValue();
                if (paperJson != null && !paperJson.isEmpty()) {
                    try {
                        Paper paper = objectMapper.readValue(paperJson, Paper.class);
                        if (paper != null) {
                            hotPapers.add(paper);
                        }
                    } catch (Exception e) {
                        log.warn("反序列化Paper失败，paperJson: {}", paperJson, e);
                    }
                }
            }
            // 按read_count排序（降序）
            hotPapers.sort((p1, p2) -> {
                Integer count1 = p1.getRead_count();
                Integer count2 = p2.getRead_count();
                if (count1 == null) count1 = 0;
                if (count2 == null) count2 = 0;
                return count2.compareTo(count1); // 降序
            });
            // 限制返回数量
            if (hotPapers.size() > limit) {
                hotPapers = hotPapers.subList(0, limit);
            }
            
            log.info("获取热门论文成功，请求数量: {}, 实际返回数量: {}", size, hotPapers.size());
            return Result.ok(hotPapers);
            
        } catch (Exception e) {
            log.error("获取热门论文失败", e);
            return Result.fail("获取热门论文失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索专利（只支持关键词搜索）
     *
     * @param request 搜索请求（只包含keyword、page、size）
     * @return 搜索结果
     */
    public PatentSearchResponse searchPatents(SimpleSearchRequest request) {
        try {
            // ========== 1. 参数校验和默认值设置 ==========
            if (request.getPage() == null || request.getPage() < 0) {
                request.setPage(0);
            }
            if (request.getSize() == null || request.getSize() < 1) {
                request.setSize(10);
            }
            if (request.getSize() > 100) {
                request.setSize(100);
            }
            
            // ========== 2. 构建查询对象 ==========
            Query query = PatentQueryBuilder.buildQuery(request);
            
            // ========== 3. 构建高亮配置 ==========
            boolean needHighlight = StringUtils.hasText(request.getKeyword());
            
            // ========== 4. 执行搜索 ==========
            SearchResponse<Patent> response = elasticsearchClient.search(s -> {
                var searchBuilder = s
                    .index(PATENT_INDEX_NAME)
                    .query(query)
                    .from(request.getPage() * request.getSize())
                    .size(request.getSize())
                    .trackTotalHits(th -> th.enabled(true));
                
                // 如果有关键词搜索，添加高亮配置
                if (needHighlight) {
                    searchBuilder.highlight(h -> h
                        .preTags("<em>")
                        .postTags("</em>")
                        .fields("patent_name", f -> f
                            .numberOfFragments(1)
                            .fragmentSize(150)
                            .requireFieldMatch(true)
                        )
                        .fields("abstract", f -> f
                            .numberOfFragments(3)
                            .fragmentSize(200)
                            .requireFieldMatch(true)
                        )
                    );
                }
                
                return searchBuilder;
            }, Patent.class);
            
            // ========== 5. 处理搜索结果 ==========
            return buildPatentSearchResponse(response, request, needHighlight);
            
        } catch (IOException e) {
            log.error("搜索专利失败，请求参数: {}, 错误信息: {}", request, e.getMessage(), e);
            return buildEmptyPatentResponse(request);
        } catch (Exception e) {
            log.error("搜索专利时发生未知错误，请求参数: {}, 错误信息: {}", request, e.getMessage(), e);
            return buildEmptyPatentResponse(request);
        }
    }
    
    /**
     * 搜索期刊（只支持关键词搜索）
     *
     * @param request 搜索请求（只包含keyword、page、size）
     * @return 搜索结果
     */
    public JournalSearchResponse searchJournals(SimpleSearchRequest request) {
        try {
            // ========== 1. 参数校验和默认值设置 ==========
            if (request.getPage() == null || request.getPage() < 0) {
                request.setPage(0);
            }
            if (request.getSize() == null || request.getSize() < 1) {
                request.setSize(10);
            }
            if (request.getSize() > 100) {
                request.setSize(100);
            }
            
            // ========== 2. 构建查询对象 ==========
            Query query = JournalQueryBuilder.buildQuery(request);
            
            // ========== 3. 构建高亮配置 ==========
            boolean needHighlight = StringUtils.hasText(request.getKeyword());
            
            // ========== 4. 执行搜索 ==========
            SearchResponse<Journal> response = elasticsearchClient.search(s -> {
                var searchBuilder = s
                    .index(JOURNAL_INDEX_NAME)
                    .query(query)
                    .from(request.getPage() * request.getSize())
                    .size(request.getSize())
                    .trackTotalHits(th -> th.enabled(true));
                
                // 如果有关键词搜索，添加高亮配置
                if (needHighlight) {
                    searchBuilder.highlight(h -> h
                        .preTags("<em>")
                        .postTags("</em>")
                        .fields("journal_name", f -> f
                            .numberOfFragments(1)
                            .fragmentSize(150)
                            .requireFieldMatch(true)
                        )
                        .fields("keywords", f -> f
                            .numberOfFragments(2)
                            .fragmentSize(200)
                            .requireFieldMatch(true)
                        )
                    );
                }
                
                return searchBuilder;
            }, Journal.class);
            
            // ========== 5. 处理搜索结果 ==========
            return buildJournalSearchResponse(response, request, needHighlight);
            
        } catch (IOException e) {
            log.error("搜索期刊失败，请求参数: {}, 错误信息: {}", request, e.getMessage(), e);
            return buildEmptyJournalResponse(request);
        } catch (Exception e) {
            log.error("搜索期刊时发生未知错误，请求参数: {}, 错误信息: {}", request, e.getMessage(), e);
            return buildEmptyJournalResponse(request);
        }
    }
    
    /**
     * 构建专利搜索响应对象（带高亮）
     */
    private PatentSearchResponse buildPatentSearchResponse(SearchResponse<Patent> response, SimpleSearchRequest request, boolean needHighlight) {
        List<PatentWithHighlight> patentsWithHighlight = response.hits().hits().stream()
            .map(hit -> {
                Patent patent = hit.source();
                if (patent == null) {
                    return null;
                }
                
                PatentWithHighlight patentWithHighlight = new PatentWithHighlight(patent);
                
                if (needHighlight && hit.highlight() != null) {
                    Map<String, List<String>> highlightMap = hit.highlight();
                    for (Map.Entry<String, List<String>> entry : highlightMap.entrySet()) {
                        patentWithHighlight.addHighlights(entry.getKey(), entry.getValue());
                    }
                }
                
                return patentWithHighlight;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        long total = response.hits().total().value();
        int totalPages = (int) Math.ceil((double) total / request.getSize());
        
        PatentSearchResponse searchResponse = new PatentSearchResponse();
        searchResponse.setPatents(patentsWithHighlight);
        searchResponse.setTotal(total);
        searchResponse.setPage(request.getPage());
        searchResponse.setSize(request.getSize());
        searchResponse.setTotalPages(totalPages);
        
        log.info("搜索专利成功，关键词: {}, 结果数: {}/{}", request.getKeyword(), patentsWithHighlight.size(), total);
        return searchResponse;
    }
    
    /**
     * 构建期刊搜索响应对象（带高亮）
     */
    private JournalSearchResponse buildJournalSearchResponse(SearchResponse<Journal> response, SimpleSearchRequest request, boolean needHighlight) {
        List<JournalWithHighlight> journalsWithHighlight = response.hits().hits().stream()
            .map(hit -> {
                Journal journal = hit.source();
                if (journal == null) {
                    return null;
                }
                
                JournalWithHighlight journalWithHighlight = new JournalWithHighlight(journal);
                
                if (needHighlight && hit.highlight() != null) {
                    Map<String, List<String>> highlightMap = hit.highlight();
                    for (Map.Entry<String, List<String>> entry : highlightMap.entrySet()) {
                        journalWithHighlight.addHighlights(entry.getKey(), entry.getValue());
                    }
                }
                
                return journalWithHighlight;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        long total = response.hits().total().value();
        int totalPages = (int) Math.ceil((double) total / request.getSize());
        
        JournalSearchResponse searchResponse = new JournalSearchResponse();
        searchResponse.setJournals(journalsWithHighlight);
        searchResponse.setTotal(total);
        searchResponse.setPage(request.getPage());
        searchResponse.setSize(request.getSize());
        searchResponse.setTotalPages(totalPages);
        
        log.info("搜索期刊成功，关键词: {}, 结果数: {}/{}", request.getKeyword(), journalsWithHighlight.size(), total);
        return searchResponse;
    }
    
    /**
     * 构建空专利响应（异常情况）
     */
    private PatentSearchResponse buildEmptyPatentResponse(SimpleSearchRequest request) {
        PatentSearchResponse response = new PatentSearchResponse();
        response.setPatents(new ArrayList<>());
        response.setTotal(0L);
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setTotalPages(0);
        return response;
    }
    
    /**
     * 构建空期刊响应（异常情况）
     */
    private JournalSearchResponse buildEmptyJournalResponse(SimpleSearchRequest request) {
        JournalSearchResponse response = new JournalSearchResponse();
        response.setJournals(new ArrayList<>());
        response.setTotal(0L);
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setTotalPages(0);
        return response;
    }
}

