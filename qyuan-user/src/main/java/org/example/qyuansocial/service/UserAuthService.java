package org.example.qyuanuser.service;

import org.example.qyuanuser.DTO.auth.*;
import org.example.qyuanuser.Result.*;

public interface UserAuthService {
    RegisterResult register(RegisterDTO registerDTO);
    LoginResult captchaLogin(CaptchaLoginDTO captchaLoginDTO);
    LoginResult passwordLogin(PasswordLoginDTO passwordLoginDTO);
    boolean sendCaptcha(SendCaptchaDTO sendCaptchaDTO);
    
}
