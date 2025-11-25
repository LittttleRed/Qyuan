package com.example.qyuanpaperrd.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.ClaimRequest;
import com.example.qyuanpaperrd.entity.Claim;
import com.example.qyuanpaperrd.mapper.ClaimMapper;
import com.example.qyuanpaperrd.service.ClaimService;
import com.example.qyuanpaperrd.service.PaperService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认领业务逻辑层实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl extends ServiceImpl<ClaimMapper, Claim> implements ClaimService {

  private final ClaimMapper claimMapper;
  private final PaperService paperService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long submitClaim(Long userId, ClaimRequest request) {
    // 检查论文是否存在
    if (!paperService.existsById(request.getPaperId())) {
      throw new RuntimeException("论文不存在");
    }

    // 检查用户是否已经认领过该论文
    Claim existingClaim = claimMapper.selectByUserIdAndPaperId(userId, request.getPaperId());
    if (existingClaim != null) {
      if (existingClaim.getStatus() == Claim.ClaimStatus.PENDING.getCode()) {
        throw new RuntimeException("您已提交过该论文的认领申请，请等待审核");
      } else if (existingClaim.getStatus() == Claim.ClaimStatus.APPROVED.getCode()) {
        throw new RuntimeException("您已成功认领该论文");
      }
      // 如果是驳回或未认领状态，允许重新申请
    }

    // 创建认领记录
    Claim claim = new Claim();
    claim.setUserId(userId);
    claim.setPaperId(request.getPaperId());
    claim.setClaimPicture(request.getClaimPicture());
    claim.setStatus(Claim.ClaimStatus.PENDING.getCode());

    save(claim);

    log.info("用户 {} 提交论文 {} 的认领申请，认领ID：{}", userId, request.getPaperId(), claim.getClaimId());
    return claim.getClaimId();
  }

  @Override
  public PageResult<Claim> getUserClaims(Long userId, Integer page, Integer size) {
    Page<Claim> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
    var claimPage = claimMapper.selectByUserId(pageParam, userId);

    return PageResult.of(claimPage.getRecords(), claimPage.getTotal(),
        claimPage.getCurrent(), claimPage.getSize());
  }

  @Override
  public List<Claim> getPaperClaims(Long paperId) {
    return claimMapper.selectByPaperId(paperId);
  }

  @Override
  public PageResult<Claim> getClaimsByStatus(Integer status, Integer page, Integer size) {
    Page<Claim> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
    var claimPage = claimMapper.selectByStatus(pageParam, status);

    return PageResult.of(claimPage.getRecords(), claimPage.getTotal(),
        claimPage.getCurrent(), claimPage.getSize());
  }

  @Override
  public Boolean hasUserClaimedPaper(Long userId, Long paperId) {
    Claim claim = claimMapper.selectByUserIdAndPaperId(userId, paperId);
    return claim != null && claim.getStatus() != Claim.ClaimStatus.UNCLAIMED.getCode();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean reviewClaim(Long claimId, Integer status, String reviewComment) {
    Claim claim = getById(claimId);
    if (claim == null) {
      throw new RuntimeException("认领记录不存在");
    }

    if (claim.getStatus() != Claim.ClaimStatus.PENDING.getCode()) {
      throw new RuntimeException("只能审核待审核状态的认领申请");
    }

    // 更新认领状态
    boolean result = claimMapper.updateStatus(claimId, status) > 0;

    if (result) {
      // 如果审核通过，创建用户论文关系
      if (status == Claim.ClaimStatus.APPROVED.getCode()) {
        // 这里可以调用用户论文关系服务来创建关系
        log.info("认领申请审核通过，认领ID：{}", claimId);
      }

      log.info("认领申请审核完成，认领ID：{}，状态：{}", claimId, status);
    }

    return result;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean withdrawClaim(Long claimId, Long userId) {
    Claim claim = getById(claimId);
    if (claim == null) {
      throw new RuntimeException("认领记录不存在");
    }

    if (!claim.getUserId().equals(userId)) {
      throw new RuntimeException("无权操作此认领记录");
    }

    if (claim.getStatus() != Claim.ClaimStatus.PENDING.getCode()) {
      throw new RuntimeException("只能撤回待审核状态的认领申请");
    }

    // 更新状态为未认领
    return claimMapper.updateStatus(claimId, Claim.ClaimStatus.UNCLAIMED.getCode()) > 0;
  }

  @Override
  public Map<String, Object> getUserClaimStats(Long userId) {
    Map<String, Object> stats = new HashMap<>();

    // 总认领数
    Long totalClaims = claimMapper.countByUserId(userId);
    stats.put("totalClaims", totalClaims);

    // 各状态认领数
    List<Map<String, Object>> statusCounts = claimMapper.countByStatus();
    Map<Integer, Long> statusMap = new HashMap<>();
    for (Map<String, Object> countMap : statusCounts) {
      Integer status = (Integer) countMap.get("status");
      Long count = (Long) countMap.get("count");
      statusMap.put(status, count);
    }

    stats.put("pendingClaims", statusMap.getOrDefault(Claim.ClaimStatus.PENDING.getCode(), 0L));
    stats.put("approvedClaims", statusMap.getOrDefault(Claim.ClaimStatus.APPROVED.getCode(), 0L));
    stats.put("rejectedClaims", statusMap.getOrDefault(Claim.ClaimStatus.REJECTED.getCode(), 0L));

    return stats;
  }
}
