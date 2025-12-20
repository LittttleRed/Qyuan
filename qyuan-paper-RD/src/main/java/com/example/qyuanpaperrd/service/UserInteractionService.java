package com.example.qyuanpaperrd.service;

import com.example.qyuanpaperrd.dto.ClaimRequest;
import com.example.qyuanpaperrd.common.PageResult;

/**
 * 用户交互服务接口 - 用户个人功能
 */
public interface UserInteractionService {

    /**
     * 认领论文
     */
    void claimPaper(Long userId, ClaimRequest request);

    /**
     * 获取用户认领记录
     */
    PageResult<Object> getUserClaims(Long userId, Integer status, Integer page, Integer size);
}

