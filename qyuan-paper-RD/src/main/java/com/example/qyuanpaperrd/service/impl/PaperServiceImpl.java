package com.example.qyuanpaperrd.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.AuthorDTO;
import com.example.qyuanpaperrd.dto.BibTeXExportDTO;
import com.example.qyuanpaperrd.dto.CitationDTO;
import com.example.qyuanpaperrd.dto.PaperAddRequest;
import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.PaperExportRequest;
import com.example.qyuanpaperrd.dto.RISExportDTO;
import com.example.qyuanpaperrd.dto.PaperSearchRequest;
import com.example.qyuanpaperrd.entity.AuthorPaper;
import com.example.qyuanpaperrd.entity.Paper;
import com.example.qyuanpaperrd.entity.PaperRef;
import com.example.qyuanpaperrd.mapper.AuthorPaperMapper;
import com.example.qyuanpaperrd.mapper.PaperMapper;
import com.example.qyuanpaperrd.mapper.PaperRefMapper;
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
  private final PaperRefMapper paperRefMapper;

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
  @Cacheable(value = "paper", key = "#paperId", unless = "#result == null")
  public PaperDTO getPaperById(Long paperId) {
    Paper paper = getById(paperId);
    if (paper == null) {
      return null;
    }
    return convertToDTO(paper);
  }

  @Override
  @Cacheable(value = "paper", key = "'doi:' + #doi", unless = "#result == null")
  public PaperDTO getPaperByDoi(String doi) {
    Paper paper = paperMapper.selectByDoi(doi);
    if (paper == null) {
      return null;
    }
    return convertToDTO(paper);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @Caching(evict = {
      @CacheEvict(value = "paper", allEntries = true),
      @CacheEvict(value = "popularPapers", allEntries = true),
      @CacheEvict(value = "latestPapers", allEntries = true)
  })
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
  @Caching(evict = {
      @CacheEvict(value = "paper", key = "#paperId"),
      @CacheEvict(value = "popularPapers", allEntries = true),
      @CacheEvict(value = "latestPapers", allEntries = true)
  })
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
    return convertToDTO(paper);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @Caching(evict = {
      @CacheEvict(value = "paper", key = "#paperId"),
      @CacheEvict(value = "popularPapers", allEntries = true),
      @CacheEvict(value = "latestPapers", allEntries = true)
  })
  public Boolean deletePaper(Long paperId) {
    // 删除作者关系
    authorPaperMapper.deleteByPaperId(paperId);
    // 删除引用关系
    paperRefMapper.deleteByPaperId(paperId);
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
  @Cacheable(value = "popularPapers", key = "#limit")
  public List<PaperDTO> getPopularPapers(Integer limit) {
    QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
    queryWrapper.orderByDesc("favorite_count");
    queryWrapper.last("LIMIT " + (limit != null ? limit : 10));

    List<Paper> papers = paperMapper.selectList(queryWrapper);
    return papers.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Cacheable(value = "latestPapers", key = "#limit")
  public List<PaperDTO> getLatestPapers(Integer limit) {
    QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
    queryWrapper.orderByDesc("updated");
    queryWrapper.last("LIMIT " + (limit != null ? limit : 10));

    List<Paper> papers = paperMapper.selectList(queryWrapper);
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
    Paper paper = getById(paperId);
    if (paper != null) {
      paper.setReadCount(paper.getReadCount() != null ? paper.getReadCount() + 1 : 1);
      updateById(paper);
    }
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
    try {
      List<Long> paperIds = request.getPaperIds();
      if (paperIds == null || paperIds.isEmpty()) {
        return "No papers selected for export";
      }

      List<Paper> papers = paperMapper.selectBatchIds(paperIds);
      if (papers.isEmpty()) {
        return "No papers found for export";
      }

      String format = StringUtils.hasText(request.getFormat()) ? 
          request.getFormat().toUpperCase() : "BIBTEX";
      
      switch (format) {
        case "BIBTEX":
          return generateBibTeXExport(papers);
        case "RIS":
          return generateRISExport(papers);
        default:
          return "Unsupported export format: " + format;
      }
      
    } catch (Exception e) {
      log.error("导出论文失败", e);
      return "Export failed: " + e.getMessage();
    }
  }

  @Override
  public List<CitationDTO> getPaperCitations(Long paperId) {
    try {
      List<PaperRef> citations = paperRefMapper.selectList(new QueryWrapper<PaperRef>().eq("paper_id", paperId));
      return citations.stream()
          .map(ref -> {
            CitationDTO citation = new CitationDTO();
            citation.setCitationId(ref.getId());
            citation.setCitedPaperId(ref.getPaperId());
            citation.setCitedPaperTitle(getPaperTitle(ref.getPaperId()));
            citation.setCitedPaperYear(String.valueOf(getPaperYear(ref.getPaperId())));
            citation.setCitedPaperAuthors(getPaperAuthors(ref.getPaperId()));
            citation.setCitedPaperJournal(getPaperJournal(ref.getPaperId()));
            return citation;
          })
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("获取论文引用信息失败", e);
      return new ArrayList<>();
    }
  }

  @Override
  public List<CitationDTO> getPapersCitedBy(Long paperId) {
    try {
      List<PaperRef> citingPapers = paperRefMapper.selectByCitedPaperId(paperId);
      return citingPapers.stream()
          .map(ref -> {
            CitationDTO citation = new CitationDTO();
            citation.setCitationId(ref.getId());
            citation.setCitedPaperId(ref.getPaperId());
            citation.setCitedPaperTitle(getPaperTitle(ref.getPaperId()));
            citation.setCitedPaperYear(String.valueOf(getPaperYear(ref.getPaperId())));
            citation.setCitedPaperAuthors(getPaperAuthors(ref.getPaperId()));
            citation.setCitedPaperJournal(getPaperJournal(ref.getPaperId()));
            return citation;
          })
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("获取被引用信息失败", e);
      return new ArrayList<>();
    }
  }

  // 辅助方法
  private String getPaperTitle(Long paperId) {
    Paper paper = paperMapper.selectById(paperId);
    return paper != null ? paper.getTitle() : "";
  }

  private Integer getPaperYear(Long paperId) {
    Paper paper = paperMapper.selectById(paperId);
    if (paper != null && paper.getUpdated() != null) {
      return paper.getUpdated().getYear();
    }
    return null;
  }

  private String getPaperAuthors(Long paperId) {
    List<AuthorPaper> authors = authorPaperMapper.selectByPaperId(paperId);
    if (authors == null || authors.isEmpty()) {
      return "";
    }
    return authors.stream()
          .map(author -> author.getAuthorLastName() + " " + author.getAuthorFirstName())
          .collect(Collectors.joining("; "));
  }

  private String getPaperJournal(Long paperId) {
    Paper paper = paperMapper.selectById(paperId);
    return paper != null ? paper.getJournalSource() : "";
  }

  private String generateBibTeXExport(List<Paper> papers) {
    StringBuilder bibTeX = new StringBuilder();
    
    for (int i = 0; i < papers.size(); i++) {
      Paper paper = papers.get(i);
      if (paper != null) {
        // 生成 BibTeX 条目
        String key = generateBibTeXKey(paper);
        bibTeX.append("  ").append(key).append(" {");
        
        // 添加标题
        if (StringUtils.hasText(paper.getTitle())) {
          bibTeX.append("\n    title = {\"").append(paper.getTitle()).append("\"},");
        }
        
        // 添加作者
        List<AuthorPaper> authors = authorPaperMapper.selectByPaperId(paper.getPaperId());
        if (authors != null && !authors.isEmpty()) {
          String authorStr = authors.stream()
              .map(author -> author.getAuthorLastName() + ", " + author.getAuthorFirstName())
              .collect(Collectors.joining(" and "));
          bibTeX.append("\n    author = {\"").append(authorStr).append("\"},");
        }
        
        // 添加期刊
        if (StringUtils.hasText(paper.getJournalSource())) {
          bibTeX.append("\n    journal = {\"").append(paper.getJournalSource()).append("\"},");
        }
        
        // 添加年份
        if (paper.getUpdated() != null) {
          bibTeX.append("\n    year = {\"").append(paper.getUpdated().getYear()).append("\"},");
        }
        
        // 移除最后的逗号并关闭条目
        if (bibTeX.charAt(bibTeX.length() - 1) == ',') {
          bibTeX.deleteCharAt(bibTeX.length() - 1);
        }
        bibTeX.append("\n  }");
        
        if (i < papers.size() - 1) {
          bibTeX.append(",\n");
        }
      }
    }
    
    bibTeX.append("\n}");
    return bibTeX.toString();
  }
  
  private String generateBibTeXKey(Paper paper) {
    List<AuthorPaper> authors = authorPaperMapper.selectByPaperId(paper.getPaperId());
    String authorName = "Unknown";
    if (authors != null && !authors.isEmpty()) {
      authorName = authors.get(0).getAuthorLastName();
    }
    
    String year = paper.getUpdated() != null ? String.valueOf(paper.getUpdated().getYear()) : "";
    String title = paper.getTitle() != null ? paper.getTitle().replaceAll("[^a-zA-Z0-9]", "") : "";
    String titlePrefix = title.length() > 8 ? title.substring(0, 8) : title;
    
    return authorName + year + titlePrefix;
  }
  
  private String generateRISExport(List<Paper> papers) {
    StringBuilder ris = new StringBuilder();
    
    for (Paper paper : papers) {
      if (paper != null) {
        ris.append("TY  - JOUR\n");
        
        if (StringUtils.hasText(paper.getTitle())) {
          ris.append("TI  - ").append(paper.getTitle()).append("\n");
        }
        
        List<AuthorPaper> authors = authorPaperMapper.selectByPaperId(paper.getPaperId());
        if (authors != null) {
          for (AuthorPaper author : authors) {
            String fullName = author.getAuthorFirstName() + " " + author.getAuthorLastName();
            ris.append("AU  - ").append(fullName).append("\n");
          }
        }
        
        if (StringUtils.hasText(paper.getJournalSource())) {
          ris.append("JO  - ").append(paper.getJournalSource()).append("\n");
        }
        
        if (paper.getUpdated() != null) {
          ris.append("PY  - ").append(paper.getUpdated().getYear()).append("\n");
        }
        
        ris.append("ER  - \n\n");
      }
    }
    
    return ris.toString();
  }
}
