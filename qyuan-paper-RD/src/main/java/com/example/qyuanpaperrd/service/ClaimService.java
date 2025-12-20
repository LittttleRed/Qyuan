package com.example.qyuanpaperrd.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.qyuanpaperrd.entity.Claim;
import com.example.qyuanpaperrd.dto.ClaimRequest;
import com.example.qyuanpaperrd.common.PageResult;

/**
 * 认领业务逻辑层接口
 */
public interface ClaimService extends IService<Claim> {

    /**
     * 提交论文认领申请
     */
    Long submitClaim(Long userId, ClaimRequest request);

    /**
     * 获取用户的认领记录
     */
    PageResult<Claim> getUserClaims(Long userId, Integer page, Integer size);

    /**
     * 获取论文的认领记录
     */
    java.util.List<Claim> getPaperClaims(Long paperId);

    /**
     * 获取指定状态的认领记录
     */
    PageResult<Claim> getClaimsByStatus(Integer status, Integer page, Integer size);

    /**
     * 检查用户是否已认领该论文
     */
    Boolean hasUserClaimedPaper(Long userId, Long paperId);

    /**
     * 审核认领申请
     */
    Boolean reviewClaim(Long claimId, Integer status, String reviewComment);

    /**
     * 撤回认领申请
     */
    Boolean withdrawClaim(Long claimId, Long userId);

    /**
     * 获取用户认领统计
     */
    java.util.Map<String, Object> getUserClaimStats(Long userId);
}