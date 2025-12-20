package org.example.qyuansocial.service;

import org.example.qyuansocial.common.PageResponse;
import org.example.qyuansocial.entity.Follow;

public interface FollowService {
    /**
     * 获取我关注的人列表（分页）
     * @param followerId 关注者ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResponse<Follow> listIFollow(Long followerId, int page, int size);

    /**
     * 获取关注我的人列表（分页）
     * @param followedId 被关注者ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResponse<Follow> listFollowMe(Long followedId, int page, int size);

    /**
     * 创建关注关系
     * @param follow 关注关系对象
     * @return 关注ID，如果已存在则返回已存在的关注ID
     */
    Long createFollow(Follow follow);

    /**
     * 取消关注
     * @param followId 关注ID
     * @return 是否成功
     */
    boolean cancelFollow(Long followId);

    /**
     * 查询两个用户之间的关注关系
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return true 表示 userId 已关注 targetUserId
     */
    boolean isFollowing(Long userId, Long targetUserId);

    /**
     * 查询某个用户关注了多少人
     */
    long countFollowings(Long userId);

    /**
     * 查询某个用户被多少人关注
     */
    long countFollowers(Long userId);
}
