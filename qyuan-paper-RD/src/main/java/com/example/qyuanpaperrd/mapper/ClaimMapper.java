package com.example.qyuanpaperrd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.qyuanpaperrd.entity.Claim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 认领信息数据访问层
 */
@Mapper
public interface ClaimMapper extends BaseMapper<Claim> {

    /**
     * 根据用户ID查询认领记录
     */
    IPage<Claim> selectByUserId(Page<Claim> page, @Param("userId") Long userId);

    /**
     * 根据论文ID查询认领记录
     */
    List<Claim> selectByPaperId(@Param("paperId") Long paperId);

    /**
     * 根据状态查询认领记录
     */
    IPage<Claim> selectByStatus(Page<Claim> page, @Param("status") Integer status);

    /**
     * 查询用户对特定论文的认领记录
     */
    Claim selectByUserIdAndPaperId(@Param("userId") Long userId, @Param("paperId") Long paperId);

    /**
     * 统计用户认领数量
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计各状态的认领数量
     */
    List<java.util.Map<String, Object>> countByStatus();

    /**
     * 更新认领状态
     */
    int updateStatus(@Param("claimId") Long claimId, @Param("status") Integer status);
}