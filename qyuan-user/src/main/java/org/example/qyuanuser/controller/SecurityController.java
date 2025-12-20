package org.example.qyuanuser.controller;

import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.example.qyuanuser.service.UserSecurityService;
import org.example.qyuancommon.Result;
import org.example.qyuanuser.DTO.security.*;
@RestController
@RequestMapping("/user/security")
public class SecurityController {
    @Resource
    private UserSecurityService userSecurityService;

    @PatchMapping("/email")
    public Result<Object> updateEmail(@RequestBody SetNewEmailDTO setNewEmailDTO) {
        try {
            if(userSecurityService.setNewEmail(setNewEmailDTO)){
                return Result.ok();
            } else {
                return Result.fail();
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PatchMapping("/password")
    public Result<Object> updatePassword(@RequestBody SetNewPasswordDTO setNewPasswordDTO) {
        try {
            if(userSecurityService.setNewPassword(setNewPasswordDTO)){
                return Result.ok();
            } else {
                return Result.fail();
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
