package com.example.qyuanpaperrd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qyuanpaperrd.entity.UserPaper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户论文关系数据访问层
 */
@Mapper
public interface UserPaperMapper extends BaseMapper<UserPaper> {

    /**
     * 根据用户ID查询论文关系
     */
    List<UserPaper> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据论文ID查询用户关系
     */
    List<UserPaper> selectByPaperId(@Param("paperId") Long paperId);

    /**
     * 查询用户对特定论文的关系
     */
    UserPaper selectByUserIdAndPaperId(@Param("userId") Long userId, @Param("paperId") Long paperId);

    /**
     * 统计用户的论文数量
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计论文的用户数量
     */
    Long countByPaperId(@Param("paperId") Long paperId);

    /**
     * 批量插入用户论文关系
     */
    int insertBatch(@Param("userPapers") List<UserPaper> userPapers);
}