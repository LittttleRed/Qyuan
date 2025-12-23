package org.example.qyuanuser.controller;

import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.example.qyuanuser.service.UserSecurityService;
import org.example.qyuancommon.Result;
import org.example.qyuanuser.DTO.security.*;
import org.example.qyuanuser.Result.CommonResult;
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
}
