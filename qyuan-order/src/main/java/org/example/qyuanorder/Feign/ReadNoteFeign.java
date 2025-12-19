package org.example.qyuanorder.Feign;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/30 13:04
 */
@FeignClient(name="qyuan-read-notes",path = "/read_notes/record")
public interface ReadNoteFeign {
    @GetMapping("/getReadRecord/{paperId}")
    public JSONObject getReadRecord(@RequestHeader("USER-ID") int user_id,
                                     @PathVariable("paperId") Integer paperId);
}
