package com.example.qyuanpaperrd.service;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.PatentDTO;
import com.example.qyuanpaperrd.entity.Patent;

import java.util.List;

/**
 * 专利服务接口
 */
public interface PatentService {

    /**
     * 根据专利ID获取专利详情
     * @param patentId 专利ID
     * @return 专利详情
     */
    PatentDTO getPatentById(Integer patentId);

    /**
     * 获取热门专利
     * @param limit 限制数量
     * @return 专利列表
     */
    List<PatentDTO> getHotPatents(Integer limit);

    /**
     * 获取最新专利
     * @param limit 限制数量
     * @return 专利列表
     */
    List<PatentDTO> getLatestPatents(Integer limit);
}