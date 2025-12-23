package org.example.qyuanuser.controller;

import org.example.qyuanuser.service.UserPersonalHomepageService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.qyuanuser.Result.VIPStateResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.example.qyuancommon.Result;
import org.example.qyuanuser.DTO.personalHomepage.UpdatePersonalInfoDTO;
import org.example.qyuanuser.Result.CommonResult;
import org.springframework.web.bind.annotation.RequestHeader;
import org.example.qyuanuser.Result.PersonalInfoResult;

@RestController
@RequestMapping("/user/personalHomepage")
public class PersonalHomepageController {
    @Resource
    private UserPersonalHomepageService userPersonalHomepageService;

    @PatchMapping("/info")
    public Result<Object> updateInfo(@RequestBody UpdatePersonalInfoDTO updatePersonalInfoDTO, @RequestHeader ("USER-ID") int user_id){
        try {
            CommonResult result = userPersonalHomepageService.updatePersonalInfo(updatePersonalInfoDTO, user_id);
            if (result.isSuccess()) {
                return Result.ok();
            } else {
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
        
    }

    @GetMapping("/info")
    public Result<Object> getInfo(@RequestHeader ("USER-ID") int user_id) {
        try {
            PersonalInfoResult result = userPersonalHomepageService.getPersonalInfo(user_id);
            if (result.isSuccess()) {
                return Result.ok(result.getPersonalInfo());
            } else {
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    

    @GetMapping("/vip")
    public Result<Object> getVipState(@RequestHeader ("USER-ID") int user_id) {
        try {
            VIPStateResult result = userPersonalHomepageService.getVipState(user_id);
            if (result.isSuccess()) {
                return Result.ok(result.getVipState());
            } else {
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
