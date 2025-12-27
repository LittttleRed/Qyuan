package com.example.qyuanes.service;

import com.example.qyuanes.dto.SimpleSearchRequest;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.util.StringUtils;

/**
 * 专利查询构建器
 * 用于将搜索请求转换为Elasticsearch查询对象
 */
public class PatentQueryBuilder {
    
    /**
     * 构建查询对象
     *
     * @param request 搜索请求（只包含keyword）
     * @return Elasticsearch Query对象
     */
    public static Query buildQuery(SimpleSearchRequest request) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        
        // 构建关键词查询
        buildKeywordQuery(request, boolQueryBuilder);
        
        // 如果有关键词，返回查询；否则返回matchAll
        if (StringUtils.hasText(request.getKeyword())) {
            return Query.of(q -> q.bool(boolQueryBuilder.build()));
        } else {
            return Query.of(q -> q.matchAll(m -> m));
        }
    }
    
    /**
     * 构建关键词查询
     * 在patent_name、abstract、inventor、assignee字段中搜索
     */
    private static void buildKeywordQuery(SimpleSearchRequest request, BoolQuery.Builder builder) {
        if (!StringUtils.hasText(request.getKeyword())) {
            return;
        }
        
        String keyword = request.getKeyword().trim();
        
        builder.must(m -> m
            .bool(b -> {
                // 策略1：多字段匹配（最高优先级）
                // 在专利名称、摘要、发明人、受让人中搜索
                b.should(s -> s
                    .bool(bb -> bb
                        .should(ss -> ss.match(mt -> mt
                            .field("patent_name")
                            .query(keyword)
                            .boost(4.0f)  // 专利名称权重最高
                        ))
                        .should(ss -> ss.match(mt -> mt
                            .field("abstract")
                            .query(keyword)
                            .boost(3.0f)  // 摘要权重次高
                        ))
                        .should(ss -> ss.match(mt -> mt
                            .field("inventor")
                            .query(keyword)
                            .boost(2.0f)  // 发明人权重
                        ))
                        .should(ss -> ss.match(mt -> mt
                            .field("assignee")
                            .query(keyword)
                            .boost(2.0f)  // 受让人权重
                        ))
                        .minimumShouldMatch("1")
                    )
                );
                
                // 策略2：前缀匹配（中等优先级）
                b.should(s -> s
                    .bool(bb -> bb
                        .should(ss -> ss.prefix(p -> p
                            .field("patent_name")
                            .value(keyword)
                            .boost(3.0f)
                        ))
                        .should(ss -> ss.prefix(p -> p
                            .field("abstract")
                            .value(keyword)
                            .boost(2.0f)
                        ))
                        .minimumShouldMatch("1")
                    )
                );
                
                // 策略3：通配符匹配（较低优先级）
                b.should(s -> s
                    .bool(bb -> bb
                        .should(ss -> ss.wildcard(w -> w
                            .field("patent_name")
                            .value("*" + keyword + "*")
                            .caseInsensitive(true)
                            .boost(2.0f)
                        ))
                        .should(ss -> ss.wildcard(w -> w
                            .field("abstract")
                            .value("*" + keyword + "*")
                            .caseInsensitive(true)
                            .boost(1.5f)
                        ))
                        .minimumShouldMatch("1")
                    )
                );
                
                return b.minimumShouldMatch("1");
            })
        );
    }
}

