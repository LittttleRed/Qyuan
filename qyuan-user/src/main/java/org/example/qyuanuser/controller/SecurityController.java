package org.example.qyuanuser.controller;

import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import org.example.qyuanuser.service.UserSecurityService;
import org.example.qyuancommon.Result;
import org.example.qyuanuser.DTO.security.*;
import org.example.qyuanuser.Result.CommonResult;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/user/security")
public class SecurityController {
    @Resource
    private UserSecurityService userSecurityService;

    @PatchMapping("/email")
    public Result<Object> updateEmail(@RequestBody SetNewEmailDTO setNewEmailDTO, @RequestHeader ("USER-ID") int user_id) { 
        try {
            CommonResult result = userSecurityService.setNewEmail(setNewEmailDTO, user_id);
            if(result.isSuccess()){
                return Result.ok();
            } else {
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PatchMapping("/password")
    public Result<Object> updatePassword(@RequestBody SetNewPasswordDTO setNewPasswordDTO, @RequestHeader ("USER-ID") int user_id) {
        try {
            CommonResult result = userSecurityService.setNewPassword(setNewPasswordDTO, user_id);
            if(result.isSuccess()){
                return Result.ok();
            } else {
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/expire_time/{order_key}")
    public void payForVIP(@RequestHeader("USER-ID") Integer user_id,
                          @RequestParam("expire_time") LocalDateTime expire_time,
                          @PathVariable("order_key") String order_key){
        if(!order_key.equals("123456789123456789123456789")){
            throw new RuntimeException("Invalid order key");
        }
        userSecurityService.payForVIP(user_id, expire_time, order_key);
    }
}
