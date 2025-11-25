package com.example.qyuanpaperrd.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.AuthorDTO;
import com.example.qyuanpaperrd.dto.CitationDTO;
import com.example.qyuanpaperrd.dto.PaperAddRequest;
import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.PaperExportRequest;
import com.example.qyuanpaperrd.dto.PaperSearchRequest;
import com.example.qyuanpaperrd.entity.AuthorPaper;
import com.example.qyuanpaperrd.entity.Paper;
import com.example.qyuanpaperrd.mapper.AuthorPaperMapper;
import com.example.qyuanpaperrd.mapper.PaperMapper;
import com.example.qyuanpaperrd.service.PaperService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 论文业务逻辑层实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

  private final PaperMapper paperMapper;
  private final AuthorPaperMapper authorPaperMapper;

  @Override
  public PageResult<PaperDTO> searchPapers(PaperSearchRequest request) {
    Page<Paper> page = new Page<>(request.getPage(), request.getSize());
    IPage<Paper> paperPage = paperMapper.searchPapers(page, request);

    List<PaperDTO> paperDTOs = paperPage.getRecords().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());

    return PageResult.of(paperDTOs, paperPage.getTotal(), paperPage.getCurrent(), paperPage.getSize());
  }

  @Override
  public PaperDTO getPaperById(Long paperId) {
    Paper paper = getById(paperId);
    if (paper == null) {
      return null;
    }
    return convertToDTO(paper);
  }

  @Override
  public PaperDTO getPaperByDoi(String doi) {
    Paper paper = paperMapper.selectByDoi(doi);
    if (paper == null) {
      return null;
    }
    return convertToDTO(paper);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public PaperDTO addPaper(PaperAddRequest request) {
    // 检查DOI是否已存在
    if (StringUtils.hasText(request.getDoi()) && existsByDoi(request.getDoi())) {
      throw new RuntimeException("DOI已存在：" + request.getDoi());
    }

    // 创建论文实体
    Paper paper = new Paper();
    BeanUtils.copyProperties(request, paper);
    if (StringUtils.hasText(request.getAbstractText())) {
      paper.setAbstractText(request.getAbstractText());
    }

    // 保存论文
    save(paper);

    // 保存作者信息
    if (request.getAuthors() != null && !request.getAuthors().isEmpty()) {
      List<AuthorPaper> authorPapers = request.getAuthors().stream()
          .map(authorReq -> {
            AuthorPaper authorPaper = new AuthorPaper();
            authorPaper.setPaperId(paper.getPaperId());
            authorPaper.setAuthorLastName(authorReq.getLastName());
            authorPaper.setAuthorFirstName(authorReq.getFirstName());
            authorPaper.setAuthorRank(authorReq.getRank());
            authorPaper.setAuthorOrcid(authorReq.getOrcid());
            return authorPaper;
          })
          .collect(Collectors.toList());

      authorPaperMapper.insertBatch(authorPapers);
    }

    // 返回论文DTO
    return convertToDTO(paper);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public PaperDTO updatePaper(Long paperId, PaperAddRequest request) {
    Paper existingPaper = getById(paperId);
    if (existingPaper == null) {
      return null;
    }

    // 检查DOI是否被其他论文使用
    if (StringUtils.hasText(request.getDoi())) {
      Paper paperWithSameDoi = paperMapper.selectByDoi(request.getDoi());
      if (paperWithSameDoi != null && !paperWithSameDoi.getPaperId().equals(paperId)) {
        throw new RuntimeException("DOI已被其他论文使用：" + request.getDoi());
      }
    }

    // 更新论文信息
    Paper paper = new Paper();
    BeanUtils.copyProperties(request, paper);
    if (StringUtils.hasText(request.getAbstractText())) {
      paper.setAbstractText(request.getAbstractText());
    }
    paper.setPaperId(paperId);

    boolean result = updateById(paper);

    // 更新作者信息
    if (request.getAuthors() != null) {
      // 删除原有作者关系
      authorPaperMapper.deleteByPaperId(paperId);

      if (!request.getAuthors().isEmpty()) {
        List<AuthorPaper> authorPapers = request.getAuthors().stream()
            .map(authorReq -> {
              AuthorPaper authorPaper = new AuthorPaper();
              authorPaper.setPaperId(paperId);
              authorPaper.setAuthorLastName(authorReq.getLastName());
              authorPaper.setAuthorFirstName(authorReq.getFirstName());
              authorPaper.setAuthorRank(authorReq.getRank());
              authorPaper.setAuthorOrcid(authorReq.getOrcid());
              return authorPaper;
            })
            .collect(Collectors.toList());

        authorPaperMapper.insertBatch(authorPapers);
      }
    }

    // 返回更新后的论文DTO
    Paper updatedPaper = getById(paperId);
    return convertToDTO(updatedPaper);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean deletePaper(Long paperId) {
    // 删除作者关系
    authorPaperMapper.deleteByPaperId(paperId);
    // 删除论文
    return removeById(paperId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int batchImportPapers(List<PaperAddRequest> papers) {
    log.info("批量导入论文，数量: {}", papers.size());
    int count = 0;
    for (PaperAddRequest request : papers) {
      try {
        addPaper(request);
        count++;
      } catch (Exception e) {
        log.error("导入论文失败: {}", request.getTitle(), e);
      }
    }
    return count;
  }

  @Override
  public List<PaperDTO> getPopularPapers(Integer limit) {
    List<Paper> papers = paperMapper.selectPopularPapers(limit != null ? limit : 10);
    return papers.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<PaperDTO> getLatestPapers(Integer limit) {
    List<Paper> papers = paperMapper.selectLatestPapers(limit != null ? limit : 10);
    return papers.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public PageResult<PaperDTO> getPapersByCategory(Long categoryId, Integer page, Integer size) {
    Page<Paper> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
    IPage<Paper> paperPage = paperMapper.selectByCategoryId(pageParam, categoryId);

    List<PaperDTO> paperDTOs = paperPage.getRecords().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());

    return PageResult.of(paperDTOs, paperPage.getTotal(), paperPage.getCurrent(), paperPage.getSize());
  }

  @Override
  public String downloadPaperPdf(Long paperId) {
    Paper paper = getById(paperId);
    if (paper == null) {
      throw new RuntimeException("论文不存在");
    }

    if (!StringUtils.hasText(paper.getPdfFileUrl())) {
      throw new RuntimeException("PDF文件不存在");
    }

    // 增加下载次数
    incrementDownloadCount(paperId);

    return paper.getPdfFileUrl();
  }

  @Override
  public void incrementDownloadCount(Long paperId) {
    // 这里可以单独维护一个下载统计表，或者通过Redis计数
    log.info("论文下载次数增加，论文ID：{}", paperId);
  }

  @Override
  public Boolean existsById(Long paperId) {
    return getById(paperId) != null;
  }

  @Override
  public Boolean existsByDoi(String doi) {
    if (!StringUtils.hasText(doi)) {
      return false;
    }
    return paperMapper.selectByDoi(doi) != null;
  }

  /**
   * 转换为DTO对象
   */
  private PaperDTO convertToDTO(Paper paper) {
    PaperDTO dto = new PaperDTO();
    BeanUtils.copyProperties(paper, dto);

    // 获取作者信息
    List<AuthorPaper> authorPapers = authorPaperMapper.selectByPaperId(paper.getPaperId());
    if (authorPapers != null && !authorPapers.isEmpty()) {
      List<AuthorDTO> authors = authorPapers.stream()
          .map(ap -> {
            AuthorDTO authorDTO = new AuthorDTO();
            authorDTO.setId(ap.getId());
            authorDTO.setPaperId(ap.getPaperId());
            authorDTO.setLastName(ap.getAuthorLastName());
            authorDTO.setFirstName(ap.getAuthorFirstName());
            authorDTO.setFullName(ap.getAuthorLastName() + " " + ap.getAuthorFirstName());
            authorDTO.setRank(ap.getAuthorRank());
            authorDTO.setOrcid(ap.getAuthorOrcid());
            return authorDTO;
          })
          .collect(Collectors.toList());
      dto.setAuthors(authors);
    }

    return dto;
  }

  @Override
  public String exportPapers(PaperExportRequest request) {
    // TODO: 实现导出功能，暂时返回简单字符串
    return "Export functionality not implemented yet";
  }

  @Override
  public List<CitationDTO> getPaperCitations(Long paperId) {
    // TODO: 实现引用查询功能
    return new ArrayList<>();
  }

  @Override
  public List<CitationDTO> getPapersCitedBy(Long paperId) {
    // TODO: 实现被引用查询功能
    return new ArrayList<>();
  }

  // TODO: 实现导出功能相关的辅助方法
  // private String generateBibTeXExport(List<Paper> papers, PaperExportRequest
  // request) { ... }
  // private String generateRISExport(List<Paper> papers, PaperExportRequest
  // request) { ... }
}
