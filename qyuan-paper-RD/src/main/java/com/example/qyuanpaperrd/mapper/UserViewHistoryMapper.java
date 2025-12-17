package com.example.qyuanpaperrd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.qyuanpaperrd.entity.UserViewHistory;
import com.example.qyuanpaperrd.entity.Paper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户浏览历史数据访问层
 */
@Mapper
public interface UserViewHistoryMapper extends BaseMapper<UserViewHistory> {

    /**
     * 根据用户ID查询浏览历史（分页，包含论文信息）
     */
    IPage<Paper> selectPapersByUserId(Page<Paper> page, @Param("userId") Long userId);

    /**
     * 根据用户ID查询浏览历史记录
     */
    List<UserViewHistory> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和论文ID查询浏览记录
     */
    UserViewHistory selectByUserIdAndPaperId(@Param("userId") Long userId, @Param("paperId") Long paperId);

    /**
     * 统计用户浏览历史数量
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 删除用户的历史记录
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据时间范围删除过期记录
     */
    int deleteByTimeRange(@Param("beforeDate") java.time.LocalDateTime beforeDate, @Param("limit") Integer limit);

    /**
     * 获取最近浏览的论文
     */
    List<Paper> selectRecentPapersByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 批量插入浏览记录
     */
    int insertBatch(@Param("histories") List<UserViewHistory> histories);
}