package org.example.qyuanuser.service;

import org.example.qyuanuser.DTO.security.*;
import org.example.qyuanuser.Result.CommonResult;

import java.time.LocalDateTime;

public interface UserSecurityService {
    CommonResult setNewPassword(SetNewPasswordDTO setNewPasswordDTO, int user_id);
    CommonResult setNewEmail(SetNewEmailDTO setNewEmailDTO, int user_id);

    void payForVIP(Integer userId, LocalDateTime expireTime, String orderKey);
}
