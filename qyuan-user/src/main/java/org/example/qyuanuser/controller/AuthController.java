package org.example.qyuanuser.controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.qyuancommon.Result;
import org.example.qyuanuser.DTO.auth.*;
import org.example.qyuanuser.Result.*;
import org.example.qyuanuser.service.UserAuthService;
import jakarta.annotation.Resource;

@RestController
@RequestMapping("/user/auth")
public class AuthController {
    @Resource
    private UserAuthService userAuthService;

    @PostMapping("/register")
    public Result<Object> register(@RequestBody RegisterDTO registerDTO) {
        try {
            RegisterResult registerResult = userAuthService.register(registerDTO);
            if (registerResult.isSuccess()) {
                return Result.ok(registerResult.getId());
            } else {
                return Result.fail(registerResult.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/captchaLogin")
    public Result<Object> captchaLogin(@RequestBody CaptchaLoginDTO captchaLoginDTO) {
       try {
         LoginResult loginResult = userAuthService.captchaLogin(captchaLoginDTO);
         if (loginResult.isSuccess()) {
             return Result.ok(loginResult.getToken());
         } else {
             return Result.fail(loginResult.getMessage());
         }
       } catch (Exception e) {
        return Result.fail(e.getMessage());
       }
    }

    @PostMapping("/passwordLogin")
    public Result<Object> passwordLogin(@RequestBody PasswordLoginDTO passwordLoginDTO) {
        try{
            LoginResult loginResult = userAuthService.passwordLogin(passwordLoginDTO);
            if (loginResult.isSuccess()) {
                return Result.ok(loginResult.getToken());
            } else {
                return Result.fail(loginResult.getMessage());
            }
        }
        catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/captcha")
    public Result<Object> sendCaptcha(@RequestBody SendCaptchaDTO sendCaptchaDTO) {
        try {
            CommonResult result = userAuthService.sendCaptcha(sendCaptchaDTO);
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
