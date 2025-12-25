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
     * 构建关键词查询 - 智能查询策略
     * 在title、abstract、journal_source字段中搜索
     *
     * 智能查询策略说明：
     * 1. 对于包含空格的查询（如"Machine Learning"）：
     *    - 优先：精确短语匹配（boost=3.0）
     *    - 其次：所有词都必须匹配（boost=2.0）
     *    - 最后：部分词匹配（boost=1.0）
     *
     * 2. 对于连写词（如"MachineLearning"）：
     *    - 优先：精确匹配（作为整体，boost=3.0）
     *    - 其次：尝试匹配分开的词（如"Machine Learning"，boost=2.0）
     *    - 最后：模糊匹配（boost=1.0）
     *
     * 3. 使用should组合多种查询，ES会根据boost自动评分排序
     *    更精确的匹配会排在前面，符合人类直觉
     */
    private static void buildKeywordQuery(PaperSearchRequest request, BoolQuery.Builder builder) {
        if (!StringUtils.hasText(request.getKeyword())) {
            return; // 没有关键词，直接返回
        }

        String keyword = request.getKeyword().trim();
        boolean containsSpace = keyword.contains(" ");

        if (containsSpace) {
            // ========== 包含空格的查询（如"Machine Learning"） ==========
            buildMultiWordQuery(keyword, builder);
        } else {
            // ========== 单个词或连写词（如"MachineLearning"） ==========
            buildSingleWordQuery(keyword, builder);
        }
    }

    /**
     * 构建多词查询（包含空格）
     * 策略：精确短语 > 所有词匹配 > 部分词匹配
     */
    private static void buildMultiWordQuery(String keyword, BoolQuery.Builder builder) {
        builder.must(m -> m
                .bool(b -> {
                    // 策略1：精确短语匹配（最高优先级，boost=4.0）
                    // 匹配连续的短语，如"Machine Learning"
                    b.should(s -> s
                            .bool(bb -> bb
                                    .should(ss -> ss.matchPhrase(mp -> mp
                                            .field("title")
                                            .query(keyword)
                                            .slop(0)
                                            .boost(4.0f)  // 提高权重
                                    ))
                                    .should(ss -> ss.matchPhrase(mp -> mp
                                            .field("abstract")
                                            .query(keyword)
                                            .slop(2)  // abstract允许词之间有少量间隔
                                            .boost(4.0f)  // 提高权重
                                    ))
                                    .should(ss -> ss.matchPhrase(mp -> mp
                                            .field("journal_source")
                                            .query(keyword)
                                            .slop(0)
                                            .boost(4.0f)  // 提高权重
                                    ))
                                    .minimumShouldMatch("1")
                            )
                    );

                    // 策略2：所有词都必须匹配（中等优先级，boost=2.5）
                    // 匹配包含所有词的文档，即使不连续
                    b.should(s -> s
                            .bool(bb -> bb
                                    .should(ss -> ss.match(mt -> mt
                                            .field("title")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                                            .boost(2.5f)
                                    ))
                                    .should(ss -> ss.match(mt -> mt
                                            .field("abstract")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                                            .boost(2.5f)
                                    ))
                                    .should(ss -> ss.match(mt -> mt
                                            .field("journal_source")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                                            .boost(2.5f)
                                    ))
                                    .minimumShouldMatch("1")
                            )
                    );

                    // 策略3：部分词匹配（最低优先级，boost=1.0）
                    // 匹配包含任意词的文档（OR关系）
                    b.should(s -> s
                            .bool(bb -> bb
                                    .should(ss -> ss.match(mt -> mt
                                            .field("title")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)
                                            .boost(1.0f)
                                    ))
                                    .should(ss -> ss.match(mt -> mt
                                            .field("abstract")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)
                                            .boost(1.0f)
                                    ))
                                    .should(ss -> ss.match(mt -> mt
                                            .field("journal_source")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)
                                            .boost(1.0f)
                                    ))
                                    .minimumShouldMatch("1")
                            )
                    );

                    return b.minimumShouldMatch("1");  // 至少满足一个策略
                })
        );
    }

    /**
     * 构建单词查询（不包含空格，可能是连写词）
     * 策略：精确匹配 > 分词匹配 > 模糊匹配
     * 新增：增加前缀匹配和连续字符匹配的权重
     */
    private static void buildSingleWordQuery(String keyword, BoolQuery.Builder builder) {
        builder.must(m -> m
                .bool(b -> {
                    // ========== 策略1：连续前缀匹配（最高优先级，boost=5.0） ==========
                    // 匹配以keyword开头的文档，例如输入"软件工"匹配"软件工程"
                    b.should(s -> s
                            .bool(bb -> bb
                                    .should(ss -> ss.prefix(p -> p
                                            .field("title")
                                            .value(keyword)
                                            .boost(5.0f)  // 前缀匹配权重最高
                                    ))
                                    .should(ss -> ss.prefix(p -> p
                                            .field("abstract")
                                            .value(keyword)
                                            .boost(4.0f)  // abstract前缀匹配次高
                                    ))
                                    .should(ss -> ss.prefix(p -> p
                                            .field("journal_source")
                                            .value(keyword)
                                            .boost(3.0f)  // journal_source前缀匹配权重第三
                                    ))
                                    .minimumShouldMatch("1")
                            )
                    );

                    // ========== 策略2：连续字符匹配（高优先级，boost=4.0） ==========
                    // 匹配包含keyword作为连续子串的文档
                    b.should(s -> s
                            .bool(bb -> bb
                                    .should(ss -> ss.wildcard(w -> w
                                            .field("title")
                                            .value("*" + keyword + "*")
                                            .caseInsensitive(true)
                                            .boost(4.0f)
                                    ))
                                    .should(ss -> ss.wildcard(w -> w
                                            .field("abstract")
                                            .value("*" + keyword + "*")
                                            .caseInsensitive(true)
                                            .boost(3.0f)
                                    ))
                                    .should(ss -> ss.wildcard(w -> w
                                            .field("journal_source")
                                            .value("*" + keyword + "*")
                                            .caseInsensitive(true)
                                            .boost(2.5f)
                                    ))
                                    .minimumShouldMatch("1")
                            )
                    );

                    // ========== 策略3：精确匹配（中等优先级，boost=3.5） ==========
                    // 匹配完整的词，如"MachineLearning"作为整体
                    b.should(s -> s
                            .bool(bb -> bb
                                    .should(ss -> ss.match(mt -> mt
                                            .field("title")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                                            .boost(3.5f)
                                    ))
                                    .should(ss -> ss.match(mt -> mt
                                            .field("abstract")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                                            .boost(3.5f)
                                    ))
                                    .should(ss -> ss.match(mt -> mt
                                            .field("journal_source")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                                            .boost(3.5f)
                                    ))
                                    .minimumShouldMatch("1")
                            )
                    );

                    // ========== 策略4：尝试匹配分开的词（中等优先级，boost=2.5） ==========
                    // 如果"MachineLearning"被分词成["machine", "learning"]
                    // 尝试匹配包含这两个词的文档（即使分开写）
                    String spacedKeyword = insertSpacesBetweenCaps(keyword);
                    if (!spacedKeyword.equals(keyword)) {
                        // 如果成功插入了空格（说明是连写词），添加短语匹配
                        b.should(s -> s
                                .bool(bb -> bb
                                        .should(ss -> ss.matchPhrase(mp -> mp
                                                .field("title")
                                                .query(spacedKeyword)
                                                .slop(0)
                                                .boost(2.5f)
                                        ))
                                        .should(ss -> ss.matchPhrase(mp -> mp
                                                .field("abstract")
                                                .query(spacedKeyword)
                                                .slop(2)  // 允许词之间有少量间隔
                                                .boost(2.5f)
                                        ))
                                        .should(ss -> ss.matchPhrase(mp -> mp
                                                .field("journal_source")
                                                .query(spacedKeyword)
                                                .slop(0)
                                                .boost(2.5f)
                                        ))
                                        .minimumShouldMatch("1")
                                )
                        );

                        // 也尝试所有词都必须匹配的方式
                        b.should(s -> s
                                .bool(bb -> bb
                                        .should(ss -> ss.match(mt -> mt
                                                .field("title")
                                                .query(spacedKeyword)
                                                .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                                                .boost(2.5f)
                                        ))
                                        .should(ss -> ss.match(mt -> mt
                                                .field("abstract")
                                                .query(spacedKeyword)
                                                .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                                                .boost(2.5f)
                                        ))
                                        .should(ss -> ss.match(mt -> mt
                                                .field("journal_source")
                                                .query(spacedKeyword)
                                                .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                                                .boost(2.5f)
                                        ))
                                        .minimumShouldMatch("1")
                                )
                        );
                    }

                    // ========== 策略5：模糊匹配（最低优先级，boost=1.0-2.0） ==========
                    // 使用OR关系，匹配包含部分词的文档
                    // 根据keyword长度调整模糊匹配权重
                    float fuzzyBoost = keyword.length() >= 3 ? 2.0f : 1.0f;
                    b.should(s -> s
                            .bool(bb -> bb
                                    .should(ss -> ss.match(mt -> mt
                                            .field("title")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)
                                            .boost(fuzzyBoost)
                                    ))
                                    .should(ss -> ss.match(mt -> mt
                                            .field("abstract")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)
                                            .boost(fuzzyBoost)
                                    ))
                                    .should(ss -> ss.match(mt -> mt
                                            .field("journal_source")
                                            .query(keyword)
                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)
                                            .boost(fuzzyBoost)
                                    ))
                                    .minimumShouldMatch("1")
                            )
                    );

                    // ========== 策略6：中文短语匹配（特殊处理，boost=3.0） ==========
                    // 针对中文的连续字符匹配，使用match_phrase
                    if (isChineseOrContainsChinese(keyword)) {
                        b.should(s -> s
                                .bool(bb -> bb
                                        .should(ss -> ss.matchPhrase(mp -> mp
                                                .field("title")
                                                .query(keyword)
                                                .slop(0)
                                                .boost(3.0f)
                                        ))
                                        .should(ss -> ss.matchPhrase(mp -> mp
                                                .field("abstract")
                                                .query(keyword)
                                                .slop(2)
                                                .boost(3.0f)
                                        ))
                                        .should(ss -> ss.matchPhrase(mp -> mp
                                                .field("journal_source")
                                                .query(keyword)
                                                .slop(0)
                                                .boost(3.0f)
                                        ))
                                        .minimumShouldMatch("1")
                                )
                        );
                    }

                    return b.minimumShouldMatch("1");  // 至少满足一个策略
                })
        );
    }

    /**
     * 判断字符串是否包含中文
     */
    private static boolean isChineseOrContainsChinese(String str) {
        if (str == null) return false;
        for (char c : str.toCharArray()) {
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断字符是否为中文
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    /**
     * 在连写词的大写字母前插入空格
     * 例如："MachineLearning" -> "Machine Learning"
     * 用于智能识别连写词并尝试匹配分开的词
     */
    private static String insertSpacesBetweenCaps(String word) {
        if (word == null || word.length() < 2) {
            return word;
        }

        // 检查是否包含大写字母（除了首字母）
        boolean hasInternalCaps = false;
        for (int i = 1; i < word.length(); i++) {
            if (Character.isUpperCase(word.charAt(i))) {
                hasInternalCaps = true;
                break;
            }
        }

        if (!hasInternalCaps) {
            return word;  // 没有内部大写字母，不是连写词
        }

        // 在大写字母前插入空格（首字母除外）
        StringBuilder result = new StringBuilder();
        result.append(word.charAt(0));

        for (int i = 1; i < word.length(); i++) {
            if (Character.isUpperCase(word.charAt(i))) {
                result.append(' ');
            }
            result.append(word.charAt(i));
        }

        return result.toString();
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
                        // 将OffsetDateTime转换为ISO 8601格式字符串
                        if (request.getStartTime() != null) {
                            rangeBuilder.gte(JsonData.of(request.getStartTime().toString()));
                        }

                        // lte: less than or equal（小于等于）
                        // 将OffsetDateTime转换为ISO 8601格式字符串
                        if (request.getEndTime() != null) {
                            rangeBuilder.lte(JsonData.of(request.getEndTime().toString()));
                        }

                        return rangeBuilder;
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