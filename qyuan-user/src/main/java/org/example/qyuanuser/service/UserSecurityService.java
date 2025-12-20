package org.example.qyuanuser.service;

import org.example.qyuanuser.DTO.security.*;

public interface UserSecurityService {
    boolean setNewPassword(SetNewPasswordDTO setNewPasswordDTO);
    boolean setNewEmail(SetNewEmailDTO setNewEmailDTO);
}
