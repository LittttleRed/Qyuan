package com.example.qyuanpaperrd.service;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.PaperDTO;

/**
 * @description:
 * @author: 29177
 * @time: 2025/12/25 1:43
 */
public interface UserPaperService {

    /**
     * 获取用户的论文列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页论文结果
     */
    PageResult<PaperDTO> getUserPapers(Long userId, Integer page, Integer size);

    Object getUsers(Long paperId);
}