package org.example.qyuanreadnotes.Controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.qyuancommon.Result;
import org.example.qyuanreadnotes.Entity.ReadRecord;
import org.example.qyuanreadnotes.Mapper.ReadRecordMapper;
import org.example.qyuanreadnotes.Service.ReadRecordService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/19 21:11
 */
@RestController
@RequestMapping("/read_notes/record")
public class ReadRecordController {

    @Resource
    ReadRecordService readRecordService;

    @PostMapping("/createReadRecord")
    public Result<Object> createReadRecord(@RequestHeader("USER-ID") int user_id,
                                           @RequestBody JSONObject body){
        try{
            Integer paperId= body.getInteger("paperId");
            String paperTitle=body.getString("paperTitle");
            String firstAuthor=body.getString("firstAuthor");
            String abstractText=body.getString("abstract");
            ReadRecord readRecord=readRecordService.createReadRecord(user_id,paperId,paperTitle,firstAuthor,abstractText);
            return Result.ok(readRecord);
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/getReadRecord/{paperId}")
    public Result<Object> getReadRecord(@RequestHeader("USER-ID") int user_id,
                                        @PathVariable("paperId") Integer paperId){
        try{
            ReadRecord readRecord=readRecordService.getReadRecord(user_id,paperId);
            return Result.ok(readRecord);
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/updateReadRecord/{recordId}")
    public Result<Object> updateReadRecord(@PathVariable("recordId") Integer recordId,
                                          @RequestBody JSONObject body){
        try{
            Integer pageNumber=body.getInteger("pageNumber");
            BigDecimal scrollPosition=body.getBigDecimal("scrollPosition");
            ReadRecord readRecord=readRecordService.updateReadRecord(recordId,pageNumber,scrollPosition);
            return Result.ok(readRecord);
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/getMyReadRecordByPage")
    public Result<Object> getMyReadRecordByPage(@RequestHeader("USER-ID") int user_id,
                                                  @RequestParam("page_num") int pageNum,
                                                  @RequestParam("page_size") int pageSize){
        try{
            Page<ReadRecord> readRecordPage=readRecordService.getMyReadRecordByPage(user_id,pageNum,pageSize);
            return Result.ok(readRecordPage);
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }


}
