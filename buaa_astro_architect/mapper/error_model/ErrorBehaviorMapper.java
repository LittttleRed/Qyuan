package org.buaa.buaa_astro_architect.mapper.error_model;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.buaa.buaa_astro_architect.entity.error_model.ErrorBehavior;

@Mapper
public interface ErrorBehaviorMapper extends BaseMapper<ErrorBehavior> {
    @Select("select composite_error_behavior " +
            "from error_behavior " +
            "where view_id = #{viewId} and view_type = #{viewType} ")
    String getCompositeErrorBehavior(@Param("viewId") String viewId, @Param("viewType") String viewType);

    @Select("select id " +
            "from error_behavior " +
            "where view_id = #{viewId} and view_type = #{viewType} ")
    Integer getIdByView(@Param("viewId") String viewId, @Param("viewType") String viewType);

    @Update("update error_behavior " +
            "set composite_error_behavior = #{compositeErrorBehavior} " +
            "where id = #{id} ")
    void setCompositeErrorBehavior(@Param("id") int id, @Param("compositeErrorBehavior") String compositeErrorBehavior);
}
