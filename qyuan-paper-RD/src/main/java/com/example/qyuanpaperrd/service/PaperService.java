package com.example.qyuanpaperrd.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.dto.CitationDTO;
import com.example.qyuanpaperrd.dto.PaperAddRequest;
import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.PaperExportRequest;
import com.example.qyuanpaperrd.dto.PaperSearchRequest;
import com.example.qyuanpaperrd.entity.Paper;

/**
 * 论文业务逻辑层接口
 */
public interface PaperService extends IService<Paper> {

  /**
   * 搜索论文
   */
  PageResult<PaperDTO> searchPapers(PaperSearchRequest request);

  /**
   * 根据ID获取论文详情
   */
  PaperDTO getPaperById(Long paperId);

  /**
   * 根据DOI获取论文
   */
  PaperDTO getPaperByDoi(String doi);

  /**
   * 新增论文
   */
  PaperDTO addPaper(PaperAddRequest request);

  /**
   * 更新论文信息
   */
  PaperDTO updatePaper(Long paperId, PaperAddRequest request);

  /**
   * 删除论文
   */
  Boolean deletePaper(Long paperId);

  /**
   * 批量导入论文
   */
  int batchImportPapers(List<PaperAddRequest> papers);

  /**
   * 获取热门论文
   */
  List<PaperDTO> getPopularPapers(Integer limit);

  /**
   * 获取最新论文
   */
  List<PaperDTO> getLatestPapers(Integer limit);

  /**
   * 根据分类获取论文
   */
  PageResult<PaperDTO> getPapersByCategory(Long categoryId, Integer page, Integer size);

  /**
   * 下载论文PDF
   */
  String downloadPaperPdf(Long paperId);

  /**
   * 增加下载次数
   */
  void incrementDownloadCount(Long paperId);

  /**
   * 检查论文是否存在
   */
  Boolean existsById(Long paperId);

  /**
   * 检查DOI是否存在
   */
  Boolean existsByDoi(String doi);

  /**
   * 导出论文信息
   */
  String exportPapers(PaperExportRequest request);

  /**
   * 获取论文引用信息
   */
  List<CitationDTO> getPaperCitations(Long paperId);

  /**
   * 获取引用该论文的其他论文
   */
  List<CitationDTO> getPapersCitedBy(Long paperId);
}
