package org.example.qyuanorder.Feign;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/30 13:27
 */
@FeignClient(name="qyuan-user",path = "/user/security")
public interface UserSecurityFeign {


    @PostMapping("/expire_time/{order_key}")
    public void payForVIP(@RequestHeader("USER-ID") Integer user_id,
                                 @RequestParam("expire_time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  LocalDateTime expire_time,
                                 @PathVariable("order_key") String order_key);
}
