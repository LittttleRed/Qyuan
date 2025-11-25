package com.example.qyuanpaperrd.service;

import com.example.qyuanpaperrd.dto.ClaimRequest;
import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.common.PageResult;

import java.util.Map;

/**
 * 用户交互服务接口 - 用户个人功能
 */
public interface UserInteractionService {

    /**
     * 收藏论文
     */
    void favoritePaper(Long userId, Long paperId);

    /**
     * 取消收藏
     */
    void unfavoritePaper(Long userId, Long paperId);

    /**
     * 获取用户收藏列表
     */
    PageResult<PaperDTO> getUserFavorites(Long userId, Integer page, Integer size);

    /**
     * 认领论文
     */
    void claimPaper(Long userId, ClaimRequest request);

    /**
     * 获取用户认领记录
     */
    PageResult<Object> getUserClaims(Long userId, Integer status, Integer page, Integer size);

    /**
     * 获取用户浏览历史
     */
    PageResult<PaperDTO> getUserHistory(Long userId, Integer page, Integer size);

    /**
     * 记录浏览行为
     */
    void recordView(Long userId, Long paperId);

    /**
     * 获取用户档案
     */
    Map<String, Object> getUserProfile(Long userId);
}

