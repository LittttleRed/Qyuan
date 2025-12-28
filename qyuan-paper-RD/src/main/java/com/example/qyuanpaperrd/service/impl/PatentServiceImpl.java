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
    public PatentDTO getPatentById(Integer patentId) {
        Patent patent = patentMapper.selectById(patentId);
        if (patent == null) {
            return null;
        }
        return convertToDTO(patent);
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