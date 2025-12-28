package com.example.qyuanpaperrd.service;

import com.example.qyuanpaperrd.dto.JournalDTO;

/**
 * 期刊服务接口
 */
public interface JournalService {

    /**
     * 根据期刊ID获取期刊详情
     * @param journalId 期刊ID
     * @return 期刊详情
     */
    JournalDTO getJournalById(Long journalId);
}