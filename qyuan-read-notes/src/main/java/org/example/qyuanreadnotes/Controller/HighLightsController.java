package org.example.qyuanreadnotes.Controller;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.example.qyuancommon.Result;
import org.example.qyuanreadnotes.Service.HighLightsService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/26 17:21
 */
@RestController
@RequestMapping("/read_notes/light")
public class HighLightsController {
    @Resource
    HighLightsService highLightsService;
    @RequestMapping("/{paper_id}")
    public Result<Object> getHighLights(@RequestHeader("USER-ID") int user_id,
                                        @PathVariable("paper_id") Integer paper_id){
        try{
            return Result.ok(highLightsService.getHighLights(user_id,paper_id));
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }
    @RequestMapping("/createHighlight")
    public Result<Object> createHighlight(@RequestHeader("USER-ID") int user_id,
                                          @RequestBody JSONObject body){
        try{
            Integer paperId= body.getInteger("paperId");
            int pageIndex=body.getInteger("pageIndex");
            String highlightedText=body.getString("highlightedText");
            String positionData=body.getString("positionData");
            String color=body.getString("color");
            BigDecimal opacity=body.getBigDecimal("opacity");
            return Result.ok(highLightsService.createHighlight(user_id,paperId,pageIndex,highlightedText,positionData,color,opacity));
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }
}
