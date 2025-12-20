package com.example.qyuanpaperrd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qyuanpaperrd.entity.PatentClaim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 专利认领数据访问层
 */
@Mapper
public interface PatentClaimMapper extends BaseMapper<PatentClaim> {

    /**
     * 根据用户ID和专利申请号查询认领记录
     * @param userId 用户ID
     * @param patentNumber 专利申请号
     * @return 认领记录
     */
    @Select("SELECT * FROM patent_claim WHERE user_id = #{userId} AND patent_number = #{patentNumber}")
    PatentClaim findByUserIdAndPatentNumber(@Param("userId") Long userId, @Param("patentNumber") String patentNumber);

    /**
     * 根据用户ID获取认领的专利列表
     * @param userId 用户ID
     * @return 专利申请号列表
     */
    @Select("SELECT DISTINCT patent_number FROM patent_claim WHERE user_id = #{userId} AND status = 3")
    List<String> findClaimedPatentNumbersByUserId(@Param("userId") Long userId);

    /**
     * 根据专利申请号获取认领记录
     * @param patentNumber 专利申请号
     * @return 认领记录列表
     */
    @Select("SELECT * FROM patent_claim WHERE patent_number = #{patentNumber} ORDER BY created_at DESC")
    List<PatentClaim> findByPatentNumber(@Param("patentNumber") String patentNumber);

    /**
     * 根据用户ID获取所有认领记录
     * @param userId 用户ID
     * @return 认领记录列表
     */
    @Select("SELECT * FROM patent_claim WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<PatentClaim> findByUserId(@Param("userId") Long userId);
}