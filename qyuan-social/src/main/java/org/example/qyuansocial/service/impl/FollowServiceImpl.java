package org.example.qyuansocial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.qyuansocial.common.PageResponse;
import org.example.qyuansocial.entity.Follow;
import org.example.qyuansocial.mapper.FollowMapper;
import org.example.qyuansocial.service.FollowService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 关注服务实现类
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String KEY_FOLLOWINGS_PREFIX = "social:followings:";
    private static final String KEY_FOLLOWERS_PREFIX = "social:followers:";

    public FollowServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    @Override
    public PageResponse<Follow> listIFollow(Long followerId, int page, int size) {
        // 创建分页对象
        Page<Follow> pageParam = new Page<>(page, size);
        
        // 构建查询条件：查询followerId等于指定值的记录（我关注的人）
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", followerId);
        queryWrapper.orderByDesc("follow_id"); // 按关注ID倒序排列
        
        // 执行分页查询
        Page<Follow> result = this.page(pageParam, queryWrapper);
        
        // 转换为PageResponse
        return new PageResponse<>(
            result.getTotal(),
            (int) result.getCurrent(),
            (int) result.getSize(),
            result.getRecords()
        );
    }

    @Override
    public PageResponse<Follow> listFollowMe(Long followedId, int page, int size) {
        // 创建分页对象
        Page<Follow> pageParam = new Page<>(page, size);
        
        // 构建查询条件：查询followedId等于指定值的记录（关注我的人）
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followed_id", followedId);
        queryWrapper.orderByDesc("follow_id"); // 按关注ID倒序排列
        
        // 执行分页查询
        Page<Follow> result = this.page(pageParam, queryWrapper);
        
        // 转换为PageResponse
        return new PageResponse<>(
            result.getTotal(),
            (int) result.getCurrent(),
            (int) result.getSize(),
            result.getRecords()
        );
    }

    @Override
    public Long createFollow(Follow follow) {
        // 检查是否已存在相同的关注关系
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", follow.getFollowerId())
                   .eq("followed_id", follow.getFollowedId());
        
        Follow existingFollow = this.getOne(queryWrapper);
        if (existingFollow != null) {
            // 已存在关注关系，补写 Redis，保持幂等
            stringRedisTemplate.opsForSet().add(
                    KEY_FOLLOWINGS_PREFIX + existingFollow.getFollowerId(),
                    String.valueOf(existingFollow.getFollowedId())
            );
            stringRedisTemplate.opsForSet().add(
                    KEY_FOLLOWERS_PREFIX + existingFollow.getFollowedId(),
                    String.valueOf(existingFollow.getFollowerId())
            );
            return existingFollow.getFollowId();
        }

        // 保存关注关系（MySQL 为主）
        this.save(follow);

        // 同步写入 Redis 关系集合，加速后续查询
        stringRedisTemplate.opsForSet().add(
                KEY_FOLLOWINGS_PREFIX + follow.getFollowerId(),
                String.valueOf(follow.getFollowedId())
        );
        stringRedisTemplate.opsForSet().add(
                KEY_FOLLOWERS_PREFIX + follow.getFollowedId(),
                String.valueOf(follow.getFollowerId())
        );

        return follow.getFollowId();
    }

    @Override
    public boolean cancelFollow(Long followId) {
        // 检查关注关系是否存在
        Follow existingFollow = this.getById(followId);
        if (existingFollow == null) {
            // 不存在关注关系，返回true（幂等操作：重复取消关注视为成功，就像微博/抖音一样）
            return true;
        }

        // 根据followId删除关注关系（先删库）
        boolean removed = this.removeById(followId);
        if (removed) {
            // 再从 Redis 中移除对应关系
            stringRedisTemplate.opsForSet().remove(
                    KEY_FOLLOWINGS_PREFIX + existingFollow.getFollowerId(),
                    String.valueOf(existingFollow.getFollowedId())
            );
            stringRedisTemplate.opsForSet().remove(
                    KEY_FOLLOWERS_PREFIX + existingFollow.getFollowedId(),
                    String.valueOf(existingFollow.getFollowerId())
            );
        }
        return removed;
    }

    @Override
    public boolean isFollowing(Long userId, Long targetUserId) {
        String keyFollowings = KEY_FOLLOWINGS_PREFIX + userId;
        // 1. 有缓存时直接用 Redis 判断
        Boolean hasKey = stringRedisTemplate.hasKey(keyFollowings);
        if (Boolean.TRUE.equals(hasKey)) {
            Boolean isMember = stringRedisTemplate.opsForSet()
                    .isMember(keyFollowings, String.valueOf(targetUserId));
            return Boolean.TRUE.equals(isMember);
        }

        // 2. 无缓存回退到 MySQL
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", userId)
               .eq("followed_id", targetUserId);
        long count = this.count(wrapper);

        // 3. 如果存在关系，顺便预热 Redis
        if (count > 0) {
            stringRedisTemplate.opsForSet().add(
                    KEY_FOLLOWINGS_PREFIX + userId,
                    String.valueOf(targetUserId)
            );
            stringRedisTemplate.opsForSet().add(
                    KEY_FOLLOWERS_PREFIX + targetUserId,
                    String.valueOf(userId)
            );
            return true;
        }
        return false;
    }

    @Override
    public long countFollowings(Long userId) {
        String keyFollowings = KEY_FOLLOWINGS_PREFIX + userId;
        Boolean hasKey = stringRedisTemplate.hasKey(keyFollowings);
        if (Boolean.TRUE.equals(hasKey)) {
            Long size = stringRedisTemplate.opsForSet().size(keyFollowings);
            return size != null ? size : 0L;
        }

        // Redis 中没有缓存，查询 MySQL
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", userId);
        long count = this.count(wrapper);

        // 预热 Redis：把所有关系写入集合
        if (count > 0) {
            List<Follow> follows = this.list(wrapper);
            for (Follow follow : follows) {
                stringRedisTemplate.opsForSet().add(
                        KEY_FOLLOWINGS_PREFIX + follow.getFollowerId(),
                        String.valueOf(follow.getFollowedId())
                );
                stringRedisTemplate.opsForSet().add(
                        KEY_FOLLOWERS_PREFIX + follow.getFollowedId(),
                        String.valueOf(follow.getFollowerId())
                );
            }
        }
        return count;
    }

    @Override
    public long countFollowers(Long userId) {
        String keyFollowers = KEY_FOLLOWERS_PREFIX + userId;
        Boolean hasKey = stringRedisTemplate.hasKey(keyFollowers);
        if (Boolean.TRUE.equals(hasKey)) {
            Long size = stringRedisTemplate.opsForSet().size(keyFollowers);
            return size != null ? size : 0L;
        }

        // Redis 中没有缓存，查询 MySQL
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("followed_id", userId);
        long count = this.count(wrapper);

        if (count > 0) {
            List<Follow> follows = this.list(wrapper);
            for (Follow follow : follows) {
                stringRedisTemplate.opsForSet().add(
                        KEY_FOLLOWINGS_PREFIX + follow.getFollowerId(),
                        String.valueOf(follow.getFollowedId())
                );
                stringRedisTemplate.opsForSet().add(
                        KEY_FOLLOWERS_PREFIX + follow.getFollowedId(),
                        String.valueOf(follow.getFollowerId())
                );
            }
        }
        return count;
    }
}

