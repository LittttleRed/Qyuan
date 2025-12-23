package org.example.qyuanuser.service;

import org.example.qyuanuser.DTO.personalHomepage.*;
import org.example.qyuanuser.Result.*;

public interface UserPersonalHomepageService {
    

    CommonResult updatePersonalInfo(UpdatePersonalInfoDTO updatePersonalInfoDTO, int user_id);

    PersonalInfoResult getPersonalInfo(int user_id);

    VIPStateResult getVipState(int user_id);
}
