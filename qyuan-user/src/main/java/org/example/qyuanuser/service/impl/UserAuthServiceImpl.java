package org.example.qyuanuser.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.example.qyuanuser.service.UserAuthService;
import org.example.qyuanuser.util.JWT;
import org.example.qyuanuser.mapper.UserMapper;
import org.example.qyuanuser.entity.User;
import org.example.qyuanuser.DTO.auth.*;
import org.example.qyuanuser.Result.LoginResult;
import org.example.qyuanuser.Result.RegisterResult;
@Service
public class UserAuthServiceImpl extends ServiceImpl<UserMapper, User> implements UserAuthService {
    @Resource
    private UserMapper userMapper;

    @Override
    public RegisterResult register(RegisterDTO registerDTO) {
        RegisterResult registerResult = new RegisterResult();
        //参数校验
        if(!registerDTO.isFull()){
            registerResult.setMessage("缺少参数");
            registerResult.setSuccess(false);
            return registerResult;
        }
        if(!registerDTO.isPasswordSame()){
            registerResult.setMessage("密码不一致");
            registerResult.setSuccess(false);
            return registerResult;
        }
        if(!registerDTO.isEmailValid()){
            registerResult.setMessage("邮箱格式错误");
            registerResult.setSuccess(false);
            return registerResult;
        }
        if(!registerDTO.isUsernameValid()){
            registerResult.setMessage("用户名格式错误,应满足：4-16位,可含有字母、数字、下划线、连字符");
            registerResult.setSuccess(false);
            return registerResult;
        }
        if(!registerDTO.isPasswordValid()){
            registerResult.setMessage("密码格式错误,应满足：8-20位，包含字母和数字");
            registerResult.setSuccess(false);
            return registerResult;
        }

        //业务校验
        if (userMapper.getUserByEmail(registerDTO.getEmail()) != null) {
            registerResult.setMessage("邮箱已注册");
            registerResult.setSuccess(false);
            return registerResult;
        }

        //插入数据库
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setUsername(registerDTO.getUsername());
        // 使用 User 实体类中的 setPassword 方法进行密码加密
        user.setPassword(registerDTO.getPassword());
        userMapper.insert(user);

        registerResult.setSuccess(true);
        registerResult.setId(user.getUserId());
        return registerResult;
        }

    @Override
    public LoginResult captchaLogin(CaptchaLoginDTO captchaLoginDTO) {
        LoginResult loginResult = new LoginResult();
        //参数校验
        if(!captchaLoginDTO.isFull()){
            loginResult.setMessage("缺少参数");
            loginResult.setSuccess(false);
            return loginResult;
        }
        //验证码数据库里没有实现，先不做验证码校验

        //获取user
        User user = userMapper.getUserByEmail(captchaLoginDTO.getEmail());
        if (user == null) {
            loginResult.setMessage("用户不存在");
            loginResult.setSuccess(false);
            return loginResult;
        }

        String token = JWT.generateJWT(user.getUserId(), user.getEmail());
        
        loginResult.setSuccess(true);
        loginResult.setToken(token);
        return loginResult;
    }

    @Override
    public LoginResult passwordLogin(PasswordLoginDTO passwordLoginDTO) {
        LoginResult loginResult = new LoginResult();
        //参数校验
        if(!passwordLoginDTO.isFull()){
            loginResult.setMessage("缺少参数");
            loginResult.setSuccess(false);
            return loginResult;
        }

        //获取user
        User user = userMapper.getUserByEmail(passwordLoginDTO.getEmail());
        if (user == null) {
            loginResult.setMessage("用户不存在");
            loginResult.setSuccess(false);
            return loginResult;
        }

        //校验密码 使用 User 实体类中的 verifyPassword 方法进行密码验证
        if (!user.verifyPassword(passwordLoginDTO.getPassword())) {
            loginResult.setMessage("密码错误");
            loginResult.setSuccess(false);
            return loginResult;
        }

        String token = JWT.generateJWT(user.getUserId(), user.getEmail());
        
        loginResult.setSuccess(true);
        loginResult.setToken(token);
        return loginResult;
    }

    @Override
    public boolean sendCaptcha(SendCaptchaDTO sendCaptchaDTO) {
        return true;
    }
}