package org.example.qyuanreadnotes.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.qyuanreadnotes.Entity.Note;
import org.example.qyuanreadnotes.Mapper.NoteMapper;
import org.example.qyuanreadnotes.Service.NoteService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/26 17:43
 */
@Service
public class NoteServiceImpl extends ServiceImpl< NoteMapper, Note> implements NoteService {
    @Resource
    NoteMapper noteMapper;

    @Override
    public List<Note> getNote(int userId, Integer paperId) {
         List<Note> notes=noteMapper.getByUserAndPaper(userId,paperId);
         return notes;
    }

    @Override
    public Note addNotes(int userId, Integer paperId) {
             Note note=new Note();
             note.setUserId(userId);
             note.setPaperId(paperId);
             note.setNoteTitle("示例笔记");
             note.setNoteContent("这是笔记内容,可以记录阅读心得、引用、或备注。");
             note.setCreateAt(LocalDateTime.now());
             this.save(note);
             return note;
    }

    @Override
    public Note updateNotes(Integer noteId, String title, String content) {
             Note note = this.getById(noteId);

             if(note==null){
                 throw new RuntimeException("笔记不存在");
             }
             if(title!=null){
                 note.setNoteTitle(title);
             }
             if(content!=null){
                 note.setNoteContent(content);
             }
             this.updateById(note);
             return note;
    }

    @Override
    public void deleteNotes(Integer noteId) {
         this.removeById(noteId);
    }
}
