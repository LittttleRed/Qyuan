package org.example.qyuansocial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.qyuansocial.common.PageResponse;
import org.example.qyuansocial.entity.Comment;
import org.example.qyuansocial.mapper.CommentMapper;
import org.example.qyuansocial.service.CommentService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String KEY_COMMENT_LIKES_PREFIX = "social:comment:likes:";

    public CommentServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(Comment comment) {
        if (comment.getLikeCount() == null) {
            comment.setLikeCount(0);
        }
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }
        this.save(comment);
        return comment.getCommentId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId) {
        // 简单按主键删除，符合接口幂等要求
        return this.removeById(commentId);
    }

    @Override
    public PageResponse<Comment> listReplies(Long parentCommentId, int page, int size) {
        Page<Comment> pageParam = new Page<>(page, size);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        // 表字段为 parent_comment_id
        wrapper.eq("parent_comment_id", parentCommentId)
               .orderByDesc("comment_id");
        Page<Comment> result = this.page(pageParam, wrapper);
        return new PageResponse<>(
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
    }

    @Override
    public PageResponse<Comment> listByUser(Long userId, int page, int size) {
        Page<Comment> pageParam = new Page<>(page, size);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("comment_id");
        Page<Comment> result = this.page(pageParam, wrapper);
        return new PageResponse<>(
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
    }

    @Override
    public PageResponse<Comment> listByAchievement(Long achievementId, int page, int size) {
        Page<Comment> pageParam = new Page<>(page, size);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("achievement_id", achievementId)
               .orderByDesc("comment_id");
        Page<Comment> result = this.page(pageParam, wrapper);
        return new PageResponse<>(
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long commentId, Long userId) {
        Comment existing = this.getById(commentId);
        if (existing == null || userId == null) {
            // 不存在则幂等返回 false（控制层可决定返回信息）
            return false;
        }
        String key = KEY_COMMENT_LIKES_PREFIX + commentId;
        Boolean liked = stringRedisTemplate.opsForSet().isMember(key, String.valueOf(userId));
        if (Boolean.TRUE.equals(liked)) {
            // 已点赞，幂等
            return true;
        }
        // 先写集合防重
        stringRedisTemplate.opsForSet().add(key, String.valueOf(userId));

        UpdateWrapper<Comment> wrapper = new UpdateWrapper<>();
        wrapper.eq("comment_id", commentId)
               .setSql("like_count = IFNULL(like_count,0) + 1");
        return this.update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeComment(Long commentId, Long userId) {
        Comment existing = this.getById(commentId);
        if (existing == null || userId == null) {
            return false;
        }
        String key = KEY_COMMENT_LIKES_PREFIX + commentId;
        Boolean liked = stringRedisTemplate.opsForSet().isMember(key, String.valueOf(userId));
        if (!Boolean.TRUE.equals(liked)) {
            // 本就未点赞，幂等
            return true;
        }
        // 先从集合移除
        stringRedisTemplate.opsForSet().remove(key, String.valueOf(userId));

        UpdateWrapper<Comment> wrapper = new UpdateWrapper<>();
        wrapper.eq("comment_id", commentId)
               .setSql("like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END");
        return this.update(wrapper);
    }
}

