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
     * 根据专利申请号获取专利详情
     * @param patentNumber 专利申请号
     * @param userId 用户ID（可选，用于检查认领状态）
     * @return 专利详情
     */
    PatentDTO getPatentByNumber(String patentNumber, Long userId);

    /**
     * 搜索专利
     * @param keyword 关键词
     * @param inventor 发明人
     * @param assignee 专利权人
     * @param country 国家
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResult<PatentDTO> searchPatents(String keyword, String inventor, String assignee, String country, Integer pageNum, Integer pageSize);

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

    /**
     * 认领专利
     * @param patentNumber 专利申请号
     * @param userId 用户ID
     * @param proofUrl 认领证明URL
     * @return 是否成功
     */
    boolean claimPatent(String patentNumber, Long userId, String proofUrl);

    /**
     * 获取用户认领的专利列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResult<PatentDTO> getUserClaimedPatents(Long userId, Integer pageNum, Integer pageSize);
}