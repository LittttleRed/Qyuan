package com.example.qyuanpaperrd.service.impl;

import java.util.ArrayList;
import java.util.Collections;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.qyuanpaperrd.entity.AuthorPaper;
import com.example.qyuanpaperrd.entity.Claim;
import com.example.qyuanpaperrd.mapper.AuthorPaperMapper;
import com.example.qyuanpaperrd.mapper.ClaimMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.ClaimRequest;
import com.example.qyuanpaperrd.service.UserInteractionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户交互服务实现类
 * TODO: 后续需要实现具体的用户交互功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInteractionServiceImpl extends ServiceImpl<ClaimMapper,Claim> implements UserInteractionService{

  @Resource
  private AuthorPaperMapper authorPaperMapper;

  @Resource
  private ClaimMapper claimMapper;


    @Override
    public void genClaim(Long userId, Long paperId, String claimPicture) {
         claimMapper.insert(new Claim()
            .setUserId(userId)
            .setPaperId(paperId)
            .setClaimPicture(claimPicture)
            .setStatus(Claim.ClaimStatus.PENDING.getCode())
        );
    }

    @Override
  public void claimPaper(Long userId, ClaimRequest request) {
    log.info("用户认领论文，用户ID: {}, 论文ID: {}", userId, request.getPaperId());

  }

  @Override
  public PageResult<Object> getUserClaims(Long userId, Integer status, Integer page, Integer size) {
    log.info("获取用户认领记录，用户ID: {}, 状态: {}", userId, status);
    // TODO: 实现获取认领记录
    return new PageResult<>(
        Collections.emptyList(),
        0L,
        (long) page,
        (long) size);
  }
}
