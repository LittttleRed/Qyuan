package com.example.qyuanes.service;

import com.example.qyuanes.dto.SimpleSearchRequest;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.util.StringUtils;

/**
 * 期刊查询构建器
 * 用于将搜索请求转换为Elasticsearch查询对象
 */
public class JournalQueryBuilder {
    
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
     * 在journal_name、keywords、publisher字段中搜索
     */
    private static void buildKeywordQuery(SimpleSearchRequest request, BoolQuery.Builder builder) {
        if (!StringUtils.hasText(request.getKeyword())) {
            return;
        }
        
        String keyword = request.getKeyword().trim();
        
        builder.must(m -> m
            .bool(b -> {
                // 策略1：多字段匹配（最高优先级）
                // 在期刊名称、关键词、出版商中搜索
                b.should(s -> s
                    .bool(bb -> bb
                        .should(ss -> ss.match(mt -> mt
                            .field("journal_name")
                            .query(keyword)
                            .boost(4.0f)  // 期刊名称权重最高
                        ))
                        .should(ss -> ss.match(mt -> mt
                            .field("keywords")
                            .query(keyword)
                            .boost(3.0f)  // 关键词权重次高
                        ))
                        .should(ss -> ss.match(mt -> mt
                            .field("publisher")
                            .query(keyword)
                            .boost(2.0f)  // 出版商权重
                        ))
                        .minimumShouldMatch("1")
                    )
                );
                
                // 策略2：前缀匹配（中等优先级）
                b.should(s -> s
                    .bool(bb -> bb
                        .should(ss -> ss.prefix(p -> p
                            .field("journal_name")
                            .value(keyword)
                            .boost(3.0f)
                        ))
                        .should(ss -> ss.prefix(p -> p
                            .field("keywords")
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
                            .field("journal_name")
                            .value("*" + keyword + "*")
                            .caseInsensitive(true)
                            .boost(2.0f)
                        ))
                        .should(ss -> ss.wildcard(w -> w
                            .field("keywords")
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

