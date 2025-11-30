package org.example.qyuanreadnotes.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.qyuanreadnotes.Entity.HighLights;

import java.util.List;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/26 17:22
 */
@Mapper
public interface HighLightsMapper extends BaseMapper<HighLights> {
    @Select("SELECT * from highlights where user_id= #{user_id} and paper_id= #{paper_id}")
    List<HighLights> getByUserAndPaper(@Param("user_id")int userId, @Param("paper_id") Integer paperId);
}
