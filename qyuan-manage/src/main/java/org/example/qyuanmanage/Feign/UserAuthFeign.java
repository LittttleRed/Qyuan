package org.example.qyuanmanage.Feign;

import org.example.qyuancommon.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@FeignClient(name="qyuan-user",path = "/user/personalHomepage")
public interface UserAuthFeign {


    @GetMapping("/vip")
    public Result<Object> authRoot(@RequestHeader("USER-ID") Integer user_id);
}