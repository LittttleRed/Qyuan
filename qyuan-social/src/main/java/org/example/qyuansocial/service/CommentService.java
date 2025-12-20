package org.example.qyuansocial.service;

import org.example.qyuansocial.entity.Comment;

public interface CommentService {
    /**
     * 创建评论
     * @param comment 评论实体
     * @return 新评论ID
     */
    Long createComment(Comment comment);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @return 是否删除成功
     */
    boolean deleteComment(Long commentId);

    /**
     * 查看某条评论的回复列表（分页）
     * @param parentCommentId 父评论ID
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    org.example.qyuansocial.common.PageResponse<Comment> listReplies(Long parentCommentId, int page, int size);

    /**
     * 根据用户ID分页查询评论
     */
    org.example.qyuansocial.common.PageResponse<Comment> listByUser(Long userId, int page, int size);

    /**
     * 根据学术成果ID分页查询评论
     */
    org.example.qyuansocial.common.PageResponse<Comment> listByAchievement(Long achievementId, int page, int size);

    /**
     * 点赞评论（幂等地忽略不存在的评论）
     * @param commentId 评论ID
     * @param userId 用户ID，用于幂等防重复
     * @return 是否找到并点赞成功（不存在返回 false）
     */
    boolean likeComment(Long commentId, Long userId);

    /**
     * 取消点赞（幂等）
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否找到并取消成功（不存在返回 false）
     */
    boolean unlikeComment(Long commentId, Long userId);
}

