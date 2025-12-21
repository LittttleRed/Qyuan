package com.example.qyuanes.scheduler;

import com.example.qyuanes.entity.Paper;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 热门论文定时任务：
 * - @Component：让 Spring 扫描并注册为 Bean。
 * - @Slf4j：提供 log 日志对象。
 * - @RequiredArgsConstructor：为 final 字段生成构造函数，便于自动注入。
 * 方法级：
 * - @Scheduled：声明定时任务，fixedRate=60_000 表示每 60 秒触发一次。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotPaperScheduler {

    private static final String HOT_KEY = "hot:papers:top10";

    private final ElasticsearchClient esClient;       // ES 客户端，由配置类提供
    private final StringRedisTemplate redisTemplate;  // Redis 客户端，由 Redis starter 自动配置
    private final ObjectMapper objectMapper;          // JSON 序列化工具

    @Scheduled(fixedRate = 60_000) // 每 60 秒执行一次（上次开始到下次开始的间隔）
    public void refreshHotPapers() {
        try {
            // 从 ES 查询 read_count 倒序的前 10 条
            SearchResponse<Paper> resp = esClient.search(s -> s
                    .index("papers")
                    .size(10)
                    .sort(so -> so.field(f -> f.field("read_count").order(SortOrder.Desc)))
                    .query(q -> q.matchAll(m -> m)), Paper.class);

            List<Paper> top10 = resp.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();


            // 将 Paper 逐条序列化为 JSON，使用 paper_id 作为 Hash 字段
            Map<String, String> paperHash = top10.stream()
                    .filter(p -> p.getPaper_id() != null)
                    .collect(Collectors.toMap(
                            p -> String.valueOf(p.getPaper_id()),
                            p -> {
                                try {
                                    return objectMapper.writeValueAsString(p);
                                } catch (Exception e) {
                                    log.warn("序列化 Paper 失败，paper_id={}", p.getPaper_id(), e);
                                    return "";
                                }
                            },
                            (a, b) -> a // 如果出现重复 id，保留第一个
                    ));

            if (!paperHash.isEmpty()) {
                redisTemplate.opsForHash().putAll(HOT_KEY, paperHash);
                // 设置 70 秒过期（Hash 只能对整个 key 过期）
                redisTemplate.expire(HOT_KEY, Duration.ofSeconds(70));
            }
        } catch (Exception e) {
            log.error("刷新热门论文缓存失败", e);
        }
    }
}
