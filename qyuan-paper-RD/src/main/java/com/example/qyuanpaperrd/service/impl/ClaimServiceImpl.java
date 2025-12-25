package com.example.qyuanpaperrd.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.example.qyuanpaperrd.entity.AuthorPaper;
import com.example.qyuanpaperrd.entity.Paper;
import com.example.qyuanpaperrd.entity.UserPaper;
import com.example.qyuanpaperrd.mapper.AuthorPaperMapper;
import com.example.qyuanpaperrd.mapper.UserPaperMapper;
import com.example.qyuanpaperrd.service.HuaweiObsService;
import com.example.qyuanpaperrd.service.MinioService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

  @Resource
  private final ClaimMapper claimMapper;


  @Resource
  private final AuthorPaperMapper authorPaperMapper;
  @Resource
  private final UserPaperMapper userPaperMapper;
  private final PaperService paperService;
  
  @Resource
  private MinioService minioService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long submitClaim(Long userId, ClaimRequest request,String paper_title) {
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

    // 处理上传的图片文件
    String pictureUrl = null;
    MultipartFile claimPicture = request.getClaimPicture();
    if (claimPicture != null && !claimPicture.isEmpty()) {
        try {
            // 上传到 MinIO
            String folder = "claims"; // 按用户ID分文件夹存储
            String objectKey = minioService.uploadFile(claimPicture, folder);
            pictureUrl = minioService.getPublicUrl(objectKey);
        } catch (Exception e) {
            log.error("上传认领图片失败", e);
            throw new RuntimeException("上传认领图片失败: " + e.getMessage());
        }
    }

    // 创建认领记录
    Claim claim = new Claim();
    claim.setUserId(userId);
    claim.setPaperId(request.getPaperId());
    claim.setClaimPicture(pictureUrl); // 保存图片URL而不是原始文件
    claim.setStatus(Claim.ClaimStatus.PENDING.getCode());
    claim.setPaperTitle(paper_title);

    save(claim);

    log.info("用户 {} 提交论文 {} 的认领申请，认领ID：{}", userId, request.getPaperId(), claim.getClaimId());
    return claim.getClaimId();
  }

  @Override
  public ArrayList<Claim> genClaims(Long userId, String last_name, String first_name, String orcid) {
    // 1. 获取作者的所有论文信息（包括论文标题）
    ArrayList<AuthorPaper> authorPapers = new ArrayList<>(
            authorPaperMapper.genClaims(orcid, first_name, last_name)
    );

    if (CollectionUtils.isEmpty(authorPapers)) {
      return new ArrayList<>();
    }
    // 2. 提取paperId列表
    List<Long> paperIds = authorPapers.stream()
            .map(AuthorPaper::getPaperId)
            .distinct()
            .collect(Collectors.toList());
    // 3. 批量查询论文标题（一次查询）
    Map<Long, String> paperTitleMap = getPaperTitleMap(paperIds);
    // 4. 构建claims列表
    ArrayList<Claim> claims = new ArrayList<>();
    authorPapers.forEach(authorPaper -> {
      Claim claim = new Claim()
              .setUserId(userId)
              .setPaperId(authorPaper.getPaperId())
              .setPaperTitle(paperTitleMap.get(authorPaper.getPaperId())) // 设置论文标题
              .setStatus(Claim.ClaimStatus.UNCLAIMED.getCode());
      claims.add(claim);
    });
    // 5. 批量保存
    this.saveBatch(claims);
    return claims;
  }
  // 批量获取论文标题的方法
  private Map<Long, String> getPaperTitleMap(List<Long> paperIds) {
    if (CollectionUtils.isEmpty(paperIds)) {
      return new HashMap<>();
    }

    // 方法1：使用MyBatis-Plus的selectBatchIds
    List<Paper> papers = paperService.listByIds(paperIds);

    return papers.stream()
            .collect(Collectors.toMap(
                    Paper::getPaperId,
                    Paper::getTitle,
                    (existing, replacement) -> existing
            ));
  }

  @Override
  public PageResult<Claim> getAllClaims(Integer page, Integer size,Integer  status) {
    // 创建查询条件
    LambdaQueryWrapper<Claim> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(status != null, Claim::getStatus, status);
    Page<Claim> pageInfo = claimMapper.selectPage(new Page<>(page, size),  queryWrapper);
    return  PageResult.of(
          pageInfo.getRecords(),
            pageInfo.getTotal(),
            pageInfo.getCurrent(),
            pageInfo.getSize()
    );
  }

  @Override
  public PageResult<Claim> getUserClaims(Long userId, Integer status ,Integer page, Integer size) {
    // 1. 构建查询条件
    LambdaQueryWrapper<Claim> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Claim::getUserId, userId);

    // 2. 如果status不为null，则添加状态条件
    if (status != null) {
      queryWrapper.eq(Claim::getStatus, status);
    }

    // 3. 按创建时间倒序排列（假设有createTime字段）
    // 如果没有createTime字段，可以使用claim_id倒序
    queryWrapper.orderByDesc(Claim::getClaimId);
    // 或者使用：queryWrapper.orderByDesc(Claim::getClaimId);

    // 4. 创建分页对象
    Page<Claim> pageInfo = new Page<>(page, size);

    // 5. 执行分页查询
    Page<Claim> resultPage = claimMapper.selectPage(pageInfo, queryWrapper);

    // 6. 返回自定义的分页结果
    return PageResult.of(
            resultPage.getRecords(),
            resultPage.getTotal(),
            resultPage.getCurrent(),
            resultPage.getSize()
    );
  }

  @Override
  public void updateClaim(Long claim_id, Integer status,Long userId,Long paperId) {
    claimMapper.updateStatus(claim_id,status);
    if(status == 3){
      UserPaper userPaper = new UserPaper();
      userPaper.setUserId(userId);
      userPaper.setPaperId(paperId);
      userPaperMapper.insert(userPaper);
    }
  }

}
