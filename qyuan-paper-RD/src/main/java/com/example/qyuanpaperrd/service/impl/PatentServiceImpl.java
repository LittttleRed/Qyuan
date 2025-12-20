package com.example.qyuanpaperrd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.PatentDTO;
import com.example.qyuanpaperrd.entity.Patent;
import com.example.qyuanpaperrd.mapper.PatentMapper;
import com.example.qyuanpaperrd.service.PatentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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


    /**
     * 将Patent实体转换为PatentDTO
     */
    private PatentDTO convertToDTO(Patent patent) {
        PatentDTO dto = new PatentDTO();
        BeanUtils.copyProperties(patent, dto);
        return dto;
    }

    }