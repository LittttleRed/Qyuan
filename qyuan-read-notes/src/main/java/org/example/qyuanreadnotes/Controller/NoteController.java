package org.example.qyuanreadnotes.Controller;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.example.qyuancommon.Result;
import org.example.qyuanreadnotes.Service.NoteService;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/26 17:42
 */
@RestController
@RequestMapping("/read_notes/note")
public class NoteController {
    @Resource
    NoteService noteService;
    @RequestMapping("/{paper_id}")
    public Result<Object> getNote(@RequestHeader("USER-ID") int user_id,
                                  @PathVariable("paper_id") Integer paper_id){
        try {
              return Result.ok(noteService.getNote(user_id,paper_id));
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }
    @RequestMapping("/addNotes")
    public Result<Object> addNotes(@RequestHeader("USER-ID") int user_id,
                                   @RequestBody JSONObject body){
        try{
            Integer paperId= body.getInteger("paperId");
            return Result.ok(noteService.addNotes(user_id,paperId));
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }
    @RequestMapping("/updateNotes")
    public Result<Object> updateNotes(@RequestBody JSONObject body){
        try{
            Integer noteId= body.getInteger("id");
            String title=body.getString("noteTitle");
            String content=body.getString("noteContent");
            return Result.ok(noteService.updateNotes(noteId,title,content));
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }
     @RequestMapping("/deleteNotes")
    public Result<Object> deleteNotes(@RequestBody JSONObject body){
        try{
            Integer noteId= body.getInteger("id");
            noteService.deleteNotes(noteId);
            return Result.ok();
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }
}
