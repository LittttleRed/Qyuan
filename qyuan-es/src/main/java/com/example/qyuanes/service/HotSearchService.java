package com.example.qyuanes.service;

import com.example.qyuanes.dto.HotSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 热门搜索服务
 * 
 * 功能：
 * 1. 记录搜索关键词及其搜索次数
 * 2. 获取热门搜索关键词列表
 * 3. 使用Redis ZSet存储，自动按搜索次数排序
 */
@Slf4j
@Service
public class HotSearchService {
    
    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    
    /**
     * Redis Key前缀
     */
    private static final String HOT_SEARCH_KEY = "hot:search:keywords";
    
    /**
     * 默认保留的热门搜索数量
     */
    private static final int DEFAULT_MAX_SIZE = 10;
    
    /**
     * 热门搜索数据的过期时间（天）
     * 如果30天内没有任何搜索更新，该Key会被自动删除
     */
    private static final int EXPIRATION_DAYS = 30;
    
    /**
     * 更新热门搜索
     * 每次搜索时调用此方法，增加关键词的搜索次数
     * 
     * @param keyword 搜索关键词
     */
    public void updateHotSearch(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return; // 空关键词不记录
        }
        
        try {
            // 去掉前后空格并转换为小写，避免大小写不同导致的重复统计
            // 例如："Machine Learning" 和 "machine learning" 会被视为同一个关键词
            keyword = keyword.trim().toLowerCase();
            
            // 使用ZSet存储：Key是关键词，Score是搜索次数
            // ZINCRBY命令：如果key不存在则创建并设置score为increment，如果存在则增加increment
            Double newScore = redisTemplate.opsForZSet().incrementScore(HOT_SEARCH_KEY, keyword, 1.0);
            
            // 每次更新时刷新过期时间（30天）
            // 如果30天内没有任何搜索操作，整个Key会被Redis自动删除
            redisTemplate.expire(HOT_SEARCH_KEY, Duration.ofDays(EXPIRATION_DAYS));
            
            log.debug("更新热门搜索: keyword={}, newCount={}", keyword, newScore != null ? newScore.longValue() : 0);
            
            // 可选：定期清理，只保留TOP N（这里不主动清理，让Redis自然过期或定期清理）
            // 如果担心数据量过大，可以设置定期任务清理
            
        } catch (Exception e) {
            // Redis操作失败不应该影响搜索功能，只记录日志
            log.warn("更新热门搜索失败，keyword: {}, 错误: {}", keyword, e.getMessage());
        }
    }
    
    /**
     * 获取热门搜索列表
     * 
     * @param size 返回的数量（默认10）
     * @return 热门搜索列表，按搜索次数降序排列
     */
    public HotSearchResponse getHotSearches(Integer size) {
        try {
            // 参数校验
            int limit = (size != null && size > 0) ? Math.min(size, 50) : 10;
            
            // 使用ZREVRANGE获取分数最高的N个关键词（降序）
            // WITHSCORES表示同时返回分数（搜索次数）
            Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(HOT_SEARCH_KEY, 0, limit - 1);
            
            if (tuples == null || tuples.isEmpty()) {
                log.debug("热门搜索列表为空");
                return new HotSearchResponse(new ArrayList<>());
            }
            
            // 转换为DTO列表
            List<HotSearchResponse.HotSearchItem> items = new ArrayList<>();
            int rank = 1;
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                String keyword = tuple.getValue();
                Long count = tuple.getScore() != null ? tuple.getScore().longValue() : 0L;
                
                items.add(new HotSearchResponse.HotSearchItem(keyword, count, rank++));
            }
            
            log.debug("获取热门搜索列表成功，数量: {}", items.size());
            return new HotSearchResponse(items);
            
        } catch (Exception e) {
            log.error("获取热门搜索列表失败，错误: {}", e.getMessage(), e);
            return new HotSearchResponse(new ArrayList<>());
        }
    }
    
    /**
     * 清理热门搜索数据（只保留TOP N）
     * 可以设置定时任务定期清理，防止数据过多
     * 
     * @param maxSize 保留的最大数量
     */
    public void trimHotSearches(int maxSize) {
        try {
            // 获取当前总数
            Long total = redisTemplate.opsForZSet().count(HOT_SEARCH_KEY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            
            if (total != null && total > maxSize) {
                // 删除排名在maxSize之后的所有数据
                // ZREMRANGEBYRANK key start stop
                Long removed = redisTemplate.opsForZSet().removeRange(HOT_SEARCH_KEY, 0, total - maxSize - 1);
                log.info("清理热门搜索数据，保留TOP {}, 删除了 {} 条记录", maxSize, removed);
            }
        } catch (Exception e) {
            log.error("清理热门搜索数据失败，错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 清空所有热门搜索数据
     * 谨慎使用，一般用于重置数据
     */
    public void clearAllHotSearches() {
        try {
            Boolean deleted = redisTemplate.delete(HOT_SEARCH_KEY);
            log.info("清空所有热门搜索数据: {}", deleted ? "成功" : "失败");
        } catch (Exception e) {
            log.error("清空热门搜索数据失败，错误: {}", e.getMessage(), e);
        }
    }
}

