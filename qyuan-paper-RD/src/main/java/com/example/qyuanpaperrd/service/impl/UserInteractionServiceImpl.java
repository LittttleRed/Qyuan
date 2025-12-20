package com.example.qyuanpaperrd.service.impl;

import java.util.Collections;

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
public class UserInteractionServiceImpl implements UserInteractionService {

  @Override
  public void claimPaper(Long userId, ClaimRequest request) {
    log.info("用户认领论文，用户ID: {}, 论文ID: {}", userId, request.getPaperId());
    // TODO: 实现论文认领功能
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
