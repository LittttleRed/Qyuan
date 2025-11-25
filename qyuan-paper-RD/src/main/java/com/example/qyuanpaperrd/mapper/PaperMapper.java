package com.example.qyuanpaperrd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.qyuanpaperrd.entity.Paper;
import com.example.qyuanpaperrd.dto.PaperSearchRequest;
import com.example.qyuanpaperrd.dto.CitationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 论文数据访问层
 */
@Mapper
public interface PaperMapper extends BaseMapper<Paper> {

    /**
     * 根据DOI查询论文
     */
    Paper selectByDoi(@Param("doi") String doi);

    /**
     * 搜索论文（支持多条件）
     */
    IPage<Paper> searchPapers(Page<Paper> page, @Param("request") PaperSearchRequest request);

    /**
     * 根据作者姓名搜索论文
     */
    List<Paper> selectByAuthorName(@Param("authorName") String authorName);

    /**
     * 根据期刊名称搜索论文
     */
    List<Paper> selectByJournalName(@Param("journalName") String journalName);

    /**
     * 获取热门论文（按下载次数排序）
     */
    List<Paper> selectPopularPapers(@Param("limit") Integer limit);

    /**
     * 获取最新论文
     */
    List<Paper> selectLatestPapers(@Param("limit") Integer limit);

    /**
     * 获取热门论文（别名，为了兼容测试）
     */
    List<Paper> getPopularPapers(@Param("limit") Integer limit);

    /**
     * 获取最新论文（别名，为了兼容测试）
     */
    List<Paper> getLatestPapers(@Param("limit") Integer limit);

    
    /**
     * 获取论文引用信息
     */
    List<CitationDTO> selectPapersWithCitations(@Param("paperIds") List<Long> paperIds);

    /**
     * 根据论文ID获取作者信息
     */
    List<Paper> selectByPaperIdAndRank(@Param("paperId") Long paperId, @Param("rank") Integer rank);

    /**
     * 根据论文ID获取引用该论文的其他文献
     */
    List<CitationDTO> selectPapersCitedBy(@Param("paperId") Long paperId);

    /**
     * 获取引用该论文的其他论文
     */
    List<Paper> selectPapersCitingBy(@Param("paperId") Long paperId);

    /**
     * 根据分类ID获取论文
     */
    IPage<Paper> selectByCategoryId(Page<Paper> page, @Param("categoryId") Long categoryId);

    /**
     * 批量插入论文
     */
    int insertBatch(@Param("papers") List<Paper> papers);
}