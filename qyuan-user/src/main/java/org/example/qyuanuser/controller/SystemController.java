package org.example.qyuanuser.controller;

import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.example.qyuanuser.Result.CommonResult;
import org.example.qyuanuser.service.UserSystemService;
import org.example.qyuancommon.Result;
@RestController
@RequestMapping("/user/system")
public class SystemController {
    @Resource
    private UserSystemService systemService;

    @PostMapping("/theme")
    public Result<Object> updateTheme(@RequestBody String theme){
        CommonResult result = systemService.updateTheme(theme);
        try{
            if(result.isSuccess()){
                return Result.ok();
            }
            else{
                return Result.fail(result.getMessage());
            }
        }
        catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }
}
