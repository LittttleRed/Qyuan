package org.example.qyuanreadnotes.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.qyuanreadnotes.Entity.ReadRecord;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/26 16:29
 */

@Mapper
public interface ReadRecordMapper extends BaseMapper<ReadRecord> {

    @Select("SELECT * from read_record where user_id=#{user_id} and paper_id=#{paper_id}")
    ReadRecord getByUserAndPaper(@Param("user_id")int userId,@Param("paper_id") Integer paperId);
}
