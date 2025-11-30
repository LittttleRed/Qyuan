package org.example.qyuanreadnotes.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.qyuanreadnotes.Entity.Note;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    @Select("SELECT * from note where user_id= #{user_id} and paper_id= #{paper_id}")
    List<Note> getByUserAndPaper(int userId, Integer paperId);
}
