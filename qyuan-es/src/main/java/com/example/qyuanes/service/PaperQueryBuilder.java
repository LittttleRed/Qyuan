package com.example.qyuanes.service;

import com.example.qyuanes.dto.PaperSearchRequest;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import org.springframework.util.StringUtils;

import java.util.List;
/**
 * 论文查询构建器
 * 
 * 作用：
 * 1. 将搜索请求DTO转换为Elasticsearch查询对象
 * 2. 封装查询逻辑，使Service层代码更清晰
 * 3. 支持灵活组合各种查询条件
 */
public class PaperQueryBuilder {
    /**
     * 构建查询对象
     * 
     * @param request 搜索请求
     * @return Elasticsearch Query对象
     */
    public static Query buildQuery(PaperSearchRequest request) {
        // 创建BoolQuery 构建器
        // BoolQuery是ES中最常用的查询类型，可以组合多个查询条件
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // ========== 关键词搜索（全文搜索） ==========
        buildKeywordQuery(request, boolQueryBuilder);

        // ========== 精确过滤条件 ==========
        buildExactFilters(request, boolQueryBuilder);

        // ========== 时间范围查询 ==========
        buildTimeRangeQuery(request, boolQueryBuilder);

        // ========== 数值范围查询 ==========
        buildNumericRangeQuery(request, boolQueryBuilder);

        // 构建最终的BoolQuery
        // 如果没有任何条件，返回matchAll查询（查询所有）
        if (request.hasSearchConditions()) {
            return Query.of(q -> q.bool(boolQueryBuilder.build()));
        } else {
            return Query.of(q -> q.matchAll(m -> m));
        }

    }

    /**
     * 构建关键词查询
     * 在title、abstract、journal_source字段中搜索
     */
    private static void buildKeywordQuery(PaperSearchRequest request, BoolQuery.Builder builder) {
        if (!StringUtils.hasText(request.getKeyword())) {
            return; // 没有关键词，直接返回
        }

        // 使用should组合多个match查询，实现OR关系
        // 只要匹配title、abstract或journal_source中的任意一个即可
        builder.must(m -> m.
            bool(b -> b
                .should(s -> s.match(mt -> mt
                    .field("title")    // 在title字段中搜索
                    .query(request.getKeyword())    // 搜索关键词
                    .fuzziness("AUTO")  // 模糊匹配（容错，如"machin"可以匹配"machine"）  存疑
                )) 
                .should(s -> s.match(mt -> mt
                    .field("abstract")                 // 在abstract字段中搜索
                    .query(request.getKeyword())
                    .fuzziness("AUTO")
                ))
                .should(s -> s.match(mt -> mt
                    .field("journal_source")           // 在journal_source字段中搜索
                    .query(request.getKeyword())
                ))
                .minimumShouldMatch("1")               // 至少匹配一个should条件
            )
        );
    }

    /**
     * 构建精确过滤条件
     */
    private static void buildExactFilters(PaperSearchRequest request, BoolQuery.Builder builder) {
        // 单个种类ID过滤（精确匹配）
        if (request.getCategoryId() != null) {
            builder.must(m -> m
                .term(t -> t
                    .field("category_id")
                    .value(request.getCategoryId())
                )
            );
        }
        // 多个种类ID过滤（OR关系）
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            builder.must(m -> m
                    .terms(t -> t
                            .field("category_id")
                            .terms(ts -> ts.value(request.getCategoryIds().stream()
                                    .map(id -> co.elastic.clients.elasticsearch._types.FieldValue.of(id))
                                    .toList()))
                    )

            );
        }
        
        // 提交者过滤（精确匹配）
        if (StringUtils.hasText(request.getSubmitter())) {
            builder.must(m -> m
                .term(t -> t
                    .field("submitter")
                    .value(request.getSubmitter())
                )
            );
        }
        
        // 期刊来源过滤（模糊匹配）
        if (StringUtils.hasText(request.getJournalSource())) {
            builder.must(m -> m
                .match(mt -> mt
                    .field("journal_source")
                    .query(request.getJournalSource())
                )
            );
        }

    }   
    
    /**
     * 构建时间范围查询
     */
    private static void buildTimeRangeQuery(PaperSearchRequest request, BoolQuery.Builder builder) {
        // 如果开始时间或结束时间有值，就添加范围查询
        if (request.getStartTime() != null || request.getEndTime() != null) {
            builder.must(m -> m
                .range(r -> {
                    var rangeBuilder = r.field("updated");
                    
                    // gte: greater than or equal（大于等于）
                    if (request.getStartTime() != null) {
                        rangeBuilder.gte(JsonData.of(request.getStartTime()));
                    }
                    
                    // lte: less than or equal（小于等于）
                    if (request.getEndTime() != null) {
                        rangeBuilder.lte(JsonData.of(request.getEndTime()));
                    }
                    
                    return rangeBuilder; // ?
                })
            );
        }
    }
    /**
     * 构建数值范围查询
     */
    private static void buildNumericRangeQuery(PaperSearchRequest request, BoolQuery.Builder builder) {
        // 阅读数范围查询
        if (request.getMinReadCount() != null || request.getMaxReadCount() != null) {
            builder.must(m -> m
                .range(r -> {
                    var rangeBuilder = r.field("read_count");
                    if (request.getMinReadCount() != null) {
                        rangeBuilder.gte(JsonData.of(request.getMinReadCount()));
                    }
                    if (request.getMaxReadCount() != null) {
                        rangeBuilder.lte(JsonData.of(request.getMaxReadCount()));
                    }
                    return rangeBuilder;
                })
            );
        }
        
        // 收藏数范围查询
        if (request.getMinFavoriteCount() != null || request.getMaxFavoriteCount() != null) {
            builder.must(m -> m
                .range(r -> {
                    var rangeBuilder = r.field("favoriate_count");
                    if (request.getMinFavoriteCount() != null) {
                        rangeBuilder.gte(JsonData.of(request.getMinFavoriteCount()));
                    }
                    if (request.getMaxFavoriteCount() != null) {
                        rangeBuilder.lte(JsonData.of(request.getMaxFavoriteCount()));
                    }
                    return rangeBuilder;
                })
            );
        }
    }

}
