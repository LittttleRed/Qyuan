package org.buaa.buaa_astro_architect.mapper.error_model;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.buaa.buaa_astro_architect.entity.error_model.ErrorProperty;

import java.util.List;

@Mapper
public interface ErrorPropertyMapper extends BaseMapper<ErrorProperty> {

    @Select("select count(*) > 0 " +
            "from error_property " +
            "where view_id = (select view_id from error_property where id = #{id}) " +
            "and view_type = (select view_type from error_property where id = #{id}) " +
            "and id <> #{id} " +
            "and name = #{name} ")
    boolean newNameExists(@Param("id") int id, @Param("name") String name);

    @Select("select * from error_property where id = #{errorPropertyId}")
    ErrorProperty getById(int errorPropertyId);
    @Select("select * from error_property where port_item_id = #{portItemId} and name= #{name}")
    List<ErrorProperty> getAllByPortItemIdAndName(@Param("portItemId") String portItemId, @Param("name") String name);
}
