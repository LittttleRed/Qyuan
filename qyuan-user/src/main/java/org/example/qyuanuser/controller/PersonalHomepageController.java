package org.example.qyuanuser.controller;

import com.alibaba.fastjson2.JSONObject;
import org.example.qyuanuser.service.UserPersonalHomepageService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    
    @PostMapping("/batch-info")
    public Result<Object> batchGetInfo(@RequestBody List<Integer> userIds) {
        try {
            List<JSONObject> results = userPersonalHomepageService.batchGetPersonalInfo(userIds);
            
            // 提取所有成功的用户信息
            
            return Result.ok(results);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    @PostMapping("/updateAvatar")
    public Result<Object> updateAvatar(@RequestBody MultipartFile avatar, @RequestHeader ("USER-ID") int user_id) {
        try {
            Result<Object> result = userPersonalHomepageService.updateAvatar(avatar, user_id);
            return result;
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
