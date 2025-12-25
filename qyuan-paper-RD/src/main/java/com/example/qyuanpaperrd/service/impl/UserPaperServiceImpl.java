package com.example.qyuanpaperrd.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.entity.Paper;
import com.example.qyuanpaperrd.entity.UserPaper;
import com.example.qyuanpaperrd.mapper.PaperMapper;
import com.example.qyuanpaperrd.mapper.UserPaperMapper;
import com.example.qyuanpaperrd.service.PaperService;
import com.example.qyuanpaperrd.service.UserPaperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: 29177
 * @time: 2025/12/25 1:44
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPaperServiceImpl implements UserPaperService {

    @Resource
    private UserPaperMapper userPaperMapper;

    @Resource
    private PaperMapper paperMapper;

    @Resource
    private PaperService paperService;

    @Override
    public PageResult<PaperDTO> getUserPapers(Long userId, Integer page, Integer size) {
        log.info("获取用户论文列表，用户ID: {}, 页码: {}, 每页大小: {}", userId, page, size);

        // 查询用户的论文关系
        List<UserPaper> userPapers = userPaperMapper.selectByUserId(userId);
        
        if (userPapers.isEmpty()) {
            // 如果用户没有关联论文，返回空结果
            return PageResult.empty((long) page, (long) size);
        }

        // 获取论文ID列表
        List<Long> paperIds = userPapers.stream()
                .map(UserPaper::getPaperId)
                .collect(Collectors.toList());

        // 计算总记录数
        Long total = (long) paperIds.size();

        // 计算当前页的起始和结束位置
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, paperIds.size());

        if (startIndex >= paperIds.size()) {
            // 如果起始位置超出范围，返回空结果
            return PageResult.empty((long) page, (long) size);
        }

        // 获取当前页的论文ID
        List<Long> currentPagePaperIds = paperIds.subList(startIndex, endIndex);

        // 根据论文ID查询论文详情
        List<Paper> papers = paperMapper.selectBatchIds(currentPagePaperIds);

        // 转换为DTO对象
        List<PaperDTO> paperDTOs = papers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(paperDTOs, total, (long) page, (long) size);
    }

    @Override
    public Object getUsers(Long paperId) {
        return userPaperMapper.selectByPaperId(paperId);
    }

    /**
     * 转换为DTO对象
     */
    private PaperDTO convertToDTO(Paper paper) {
        PaperDTO dto = new PaperDTO();
        dto.setPaperId(paper.getPaperId());
        dto.setTitle(paper.getTitle());
        dto.setSubmitter(paper.getSubmitter());
        dto.setAbstractText(paper.getAbstractText());
        dto.setDoi(paper.getDoi());
        dto.setJournalSource(paper.getJournalSource());
        dto.setPdfFileUrl(paper.getPdfFileUrl());
        dto.setUrl(paper.getUrl());
        dto.setCategoryId(paper.getCategoryId());
        dto.setUpdated(paper.getUpdated());
        dto.setDownloadCount(paper.getReadCount() != null ? paper.getReadCount() : 0);
        dto.setFavoriteCount(paper.getFavoriteCount() != null ? paper.getFavoriteCount() : 0);
        return dto;
    }
}