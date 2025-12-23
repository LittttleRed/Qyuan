package org.example.qyuanuser.service;

import org.example.qyuanuser.DTO.security.*;
import org.example.qyuanuser.Result.CommonResult;

public interface UserSecurityService {
    CommonResult setNewPassword(SetNewPasswordDTO setNewPasswordDTO, int user_id);
    CommonResult setNewEmail(SetNewEmailDTO setNewEmailDTO, int user_id);
}
