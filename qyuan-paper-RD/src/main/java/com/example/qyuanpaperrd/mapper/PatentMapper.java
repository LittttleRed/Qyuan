package com.example.qyuanpaperrd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qyuanpaperrd.entity.Patent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 专利数据访问层
 */
@Mapper
public interface PatentMapper extends BaseMapper<Patent> {

    /**
     * 根据关键词搜索专利
     * @param keyword 关键词
     * @return 专利列表
     */
    @Select("SELECT * FROM patent WHERE " +
            "patent_name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "inventor LIKE CONCAT('%', #{keyword}, '%') OR " +
            "assignee LIKE CONCAT('%', #{keyword}, '%') OR " +
            "`abstract` LIKE CONCAT('%', #{keyword}, '%')")
    List<Patent> searchPatents(@Param("keyword") String keyword);

    /**
     * 根据发明人搜索专利
     * @param inventor 发明人
     * @return 专利列表
     */
    @Select("SELECT * FROM patent WHERE inventor LIKE CONCAT('%', #{inventor}, '%')")
    List<Patent> searchByInventor(@Param("inventor") String inventor);

    /**
     * 根据专利权人搜索专利
     * @param assignee 专利权人
     * @return 专利列表
     */
    @Select("SELECT * FROM patent WHERE assignee LIKE CONCAT('%', #{assignee}, '%')")
    List<Patent> searchByAssignee(@Param("assignee") String assignee);

    /**
     * 根据国家搜索专利
     * @param country 国家
     * @return 专利列表
     */
    @Select("SELECT * FROM patent WHERE country = #{country}")
    List<Patent> searchByCountry(@Param("country") String country);

    /**
     * 获取热门专利（按引用数排序）
     * @param limit 限制数量
     * @return 专利列表
     */
    @Select("SELECT * FROM patent ORDER BY citation_count DESC LIMIT #{limit}")
    List<Patent> getHotPatents(@Param("limit") Integer limit);

    /**
     * 获取最新专利（按申请日期排序）
     * @param limit 限制数量
     * @return 专利列表
     */
    @Select("SELECT * FROM patent ORDER BY application_date DESC LIMIT #{limit}")
    List<Patent> getLatestPatents(@Param("limit") Integer limit);
}