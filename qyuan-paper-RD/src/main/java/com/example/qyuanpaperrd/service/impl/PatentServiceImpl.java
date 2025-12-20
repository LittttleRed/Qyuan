package com.example.qyuanpaperrd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.PatentDTO;
import com.example.qyuanpaperrd.entity.Patent;
import com.example.qyuanpaperrd.entity.PatentClaim;
import com.example.qyuanpaperrd.mapper.PatentClaimMapper;
import com.example.qyuanpaperrd.mapper.PatentMapper;
import com.example.qyuanpaperrd.service.PatentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 专利服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatentServiceImpl implements PatentService {

    private final PatentMapper patentMapper;
    private final PatentClaimMapper patentClaimMapper;

    @Override
    @Cacheable(value = "patent", key = "'number:' + #patentNumber + ':user:' + #userId", unless = "#result == null")
    public PatentDTO getPatentByNumber(String patentNumber, Long userId) {
        // 查询专利信息
        Patent patent = patentMapper.selectById(patentNumber);
        if (patent == null) {
            return null;
        }

        // 转换为DTO
        PatentDTO patentDTO = new PatentDTO();
        BeanUtils.copyProperties(patent, patentDTO);

        // 如果提供了用户ID，检查认领状态
        if (userId != null) {
            PatentClaim claim = patentClaimMapper.findByUserIdAndPatentNumber(userId, patentNumber);
            if (claim != null) {
                patentDTO.setIsClaimed(true);
                patentDTO.setClaimStatus(PatentClaim.ClaimStatus.fromCode(claim.getStatus()).getDescription());
            } else {
                patentDTO.setIsClaimed(false);
                patentDTO.setClaimStatus(PatentClaim.ClaimStatus.UNCLAIMED.getDescription());
            }
        } else {
            // 用户ID为null时，默认未认领
            patentDTO.setIsClaimed(false);
            patentDTO.setClaimStatus(PatentClaim.ClaimStatus.UNCLAIMED.getDescription());
        }

        return patentDTO;
    }

    @Override
    public PageResult<PatentDTO> searchPatents(String keyword, String inventor, String assignee, String country, Integer pageNum, Integer pageSize) {
        List<Patent> patents;

        // 优先使用具体的搜索条件
        if (inventor != null && !inventor.trim().isEmpty()) {
            patents = patentMapper.searchByInventor(inventor.trim());
        } else if (assignee != null && !assignee.trim().isEmpty()) {
            patents = patentMapper.searchByAssignee(assignee.trim());
        } else if (country != null && !country.trim().isEmpty()) {
            patents = patentMapper.searchByCountry(country.trim());
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            patents = patentMapper.searchPatents(keyword.trim());
        } else {
            QueryWrapper<Patent> queryWrapper = new QueryWrapper<>();
            patents = patentMapper.selectList(queryWrapper);
        }

        // 手动分页
        int total = patents.size();
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        if (startIndex >= total) {
            return PageResult.of(java.util.Collections.emptyList(), (long)total, (long)pageNum, (long)pageSize);
        }

        List<Patent> pageData = patents.subList(startIndex, endIndex);
        List<PatentDTO> patentDTOs = pageData.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(patentDTOs, (long)total, (long)pageNum, (long)pageSize);
    }

    @Override
    @Cacheable(value = "hotPatents", key = "#limit")
    public List<PatentDTO> getHotPatents(Integer limit) {
        List<Patent> patents = patentMapper.getHotPatents(limit);
        return patents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "latestPatents", key = "#limit")
    public List<PatentDTO> getLatestPatents(Integer limit) {
        List<Patent> patents = patentMapper.getLatestPatents(limit);
        return patents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "patent", key = "'number:' + #patentNumber + ':user:' + #userId")
    public boolean claimPatent(String patentNumber, Long userId, String proofUrl) {
        try {
            // 检查专利是否存在
            Patent patent = patentMapper.selectById(patentNumber);
            if (patent == null) {
                log.warn("专利不存在: {}", patentNumber);
                return false;
            }

            // 检查是否已经认领
            PatentClaim existingClaim = patentClaimMapper.findByUserIdAndPatentNumber(userId, patentNumber);
            if (existingClaim != null) {
                log.warn("用户 {} 已经认领过专利 {}", userId, patentNumber);
                return false;
            }

            // 创建认领记录
            PatentClaim claim = new PatentClaim()
                    .setUserId(userId)
                    .setPatentNumber(patentNumber)
                    .setClaimPicture(proofUrl)
                    .setStatus(PatentClaim.ClaimStatus.PENDING.getCode())
                    .setCreatedAt(LocalDateTime.now())
                    .setUpdatedAt(LocalDateTime.now());

            int result = patentClaimMapper.insert(claim);
            return result > 0;
        } catch (Exception e) {
            log.error("认领专利失败: patentNumber={}, userId={}", patentNumber, userId, e);
            return false;
        }
    }

    @Override
    public PageResult<PatentDTO> getUserClaimedPatents(Long userId, Integer pageNum, Integer pageSize) {
        // 获取用户已认领的专利号列表
        List<String> claimedPatentNumbers = patentClaimMapper.findClaimedPatentNumbersByUserId(userId);

        if (claimedPatentNumbers.isEmpty()) {
            return PageResult.empty((long)pageNum, (long)pageSize);
        }

        // 查询专利信息
        List<Patent> patents = patentMapper.selectBatchIds(claimedPatentNumbers);

        // 转换为DTO并设置认领状态
        List<PatentDTO> patentDTOs = patents.stream()
                .map(patent -> {
                    PatentDTO dto = convertToDTO(patent);
                    dto.setIsClaimed(true);
                    dto.setClaimStatus(PatentClaim.ClaimStatus.APPROVED.getDescription());
                    return dto;
                })
                .collect(Collectors.toList());

        // 手动分页
        int total = patentDTOs.size();
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        if (startIndex >= total) {
            return PageResult.of(java.util.Collections.emptyList(), (long)total, (long)pageNum, (long)pageSize);
        }

        List<PatentDTO> pageData = patentDTOs.subList(startIndex, endIndex);

        return PageResult.of(pageData, (long)total, (long)pageNum, (long)pageSize);
    }

    /**
     * 将Patent实体转换为PatentDTO
     */
    private PatentDTO convertToDTO(Patent patent) {
        PatentDTO dto = new PatentDTO();
        BeanUtils.copyProperties(patent, dto);
        return dto;
    }

    }