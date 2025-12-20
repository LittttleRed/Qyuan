package com.example.qyuanpaperrd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qyuanpaperrd.entity.AuthorPaper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作者论文关系数据访问层
 */
@Mapper
public interface AuthorPaperMapper extends BaseMapper<AuthorPaper> {

    /**
     * 根据论文ID获取作者列表
     */
    List<AuthorPaper> selectByPaperId(@Param("paperId") Long paperId);

    /**
     * 根据作者姓名查询论文关系
     */
    List<AuthorPaper> selectByAuthorName(@Param("authorName") String authorName);

    /**
     * 根据ORCID查询论文关系
     */
    List<AuthorPaper> selectByOrcid(@Param("orcid") String orcid);

    /**
     * 根据论文ID和作者次序查询
     */
    AuthorPaper selectByPaperIdAndRank(@Param("paperId") Long paperId, @Param("rank") Integer rank);

    /**
     * 批量插入作者论文关系
     */
    int insertBatch(@Param("authorPapers") List<AuthorPaper> authorPapers);

    /**
     * 根据论文ID删除作者关系
     */
    int deleteByPaperId(@Param("paperId") Long paperId);

    /**
     * 获取论文作者信息（为了兼容测试）
     */
    List<AuthorPaper> getAuthorsByPaperId(@Param("paperId") Long paperId);
}