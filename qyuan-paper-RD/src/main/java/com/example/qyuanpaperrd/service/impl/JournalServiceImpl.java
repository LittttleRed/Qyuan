package com.example.qyuanpaperrd.service.impl;

import com.example.qyuanpaperrd.dto.JournalDTO;
import com.example.qyuanpaperrd.entity.Journal;
import com.example.qyuanpaperrd.mapper.JournalMapper;
import com.example.qyuanpaperrd.service.JournalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 期刊服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

    private final JournalMapper journalMapper;

    @Override
    @Cacheable(value = "journal", key = "'id:' + #journalId", unless = "#result == null")
    public JournalDTO getJournalById(Long journalId) {
        // 查询期刊信息
        Journal journal = journalMapper.selectById(journalId);
        if (journal == null) {
            return null;
        }

        // 转换为DTO
        JournalDTO journalDTO = new JournalDTO();
        BeanUtils.copyProperties(journal, journalDTO);

        return journalDTO;
    }
}