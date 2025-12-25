package org.example.qyuanuser.service;

import com.alibaba.fastjson2.JSONObject;
import org.example.qyuancommon.Result;
import org.example.qyuanuser.DTO.personalHomepage.*;
import org.example.qyuanuser.Result.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserPersonalHomepageService {
    

    CommonResult updatePersonalInfo(UpdatePersonalInfoDTO updatePersonalInfoDTO, int user_id);

    PersonalInfoResult getPersonalInfo(int user_id);

    VIPStateResult getVipState(int user_id);
    
    List<JSONObject> batchGetPersonalInfo(List<Integer> userIds);

    Result< Object> updateAvatar(MultipartFile avatar, int userId);
}
