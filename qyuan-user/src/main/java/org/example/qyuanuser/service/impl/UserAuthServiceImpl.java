package org.example.qyuanuser.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.qyuanuser.Result.CommonResult;
import jakarta.annotation.Resource;
import org.example.qyuanuser.util.EmailApi;
import org.springframework.data.redis.core.RedisTemplate;
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
    private RedisTemplate<String,String> redisTemplate;
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private EmailApi emailApi;

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

        // 验证注册验证码
        String captchaKey = "captcha:register:" + registerDTO.getEmail();
        String storedCaptcha = redisTemplate.opsForValue().get(captchaKey);
        if (storedCaptcha == null || !storedCaptcha.equals(registerDTO.getCaptcha())) {
            registerResult.setMessage("注册验证码错误或已过期");
            registerResult.setSuccess(false);
            return registerResult;
        }

        //业务校验
        if (userMapper.getUserByEmail(registerDTO.getEmail()) != null) {
            registerResult.setMessage("邮箱已注册");
            registerResult.setSuccess(false);
            return registerResult;
        }

        // 删除已使用的注册验证码
        redisTemplate.delete(captchaKey);


        //插入数据库
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setUsername(registerDTO.getUsername());
        // 使用 User 实体类中的 setPassword 方法进行密码加密
        user.encodePassword(registerDTO.getPassword());
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

        // 验证登录验证码
        String captchaKey = "captcha:login:" + captchaLoginDTO.getEmail();
        String storedCaptcha = redisTemplate.opsForValue().get(captchaKey);
        if (storedCaptcha == null || !storedCaptcha.equals(captchaLoginDTO.getCaptcha())) {
            loginResult.setMessage("验证码错误或已过期");
            loginResult.setSuccess(false);
            return loginResult;
        }

        // 获取user
        User user = userMapper.getUserByEmail(captchaLoginDTO.getEmail());
        if (user == null) {
            loginResult.setMessage("用户不存在");
            loginResult.setSuccess(false);
            return loginResult;
        }

        // 验证码正确，删除已使用的验证码
        redisTemplate.delete(captchaKey);

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

        System.out.println(passwordLoginDTO.getPassword());
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
    public CommonResult sendCaptcha(SendCaptchaDTO sendCaptchaDTO) {
        //参数校验
        CommonResult result = new CommonResult();
        if(!sendCaptchaDTO.isFull()){
            result.setMessage("缺少参数");
            result.setSuccess(false);
            return result;
        }
        if(!sendCaptchaDTO.isEmailValid()){
            result.setMessage("邮箱格式错误");
            result.setSuccess(false);
            return result;
        }

        // 根据场景生成不同的验证码key
        String scene = sendCaptchaDTO.getScene();
        String captchaKey;
        if ("register".equals(scene)) {
            captchaKey = "captcha:register:" + sendCaptchaDTO.getEmail();
        } else if ("login".equals(scene)) {
            captchaKey = "captcha:login:" + sendCaptchaDTO.getEmail();
        } else if ("update_email".equals(scene)) {
            captchaKey = "captcha:update_email:" + sendCaptchaDTO.getEmail();
        } else if ("update_password".equals(scene)) {
            captchaKey = "captcha:update_password:" + sendCaptchaDTO.getEmail();
        } else {
            result.setMessage("无效的验证码使用场景");
            result.setSuccess(false);
            return result;
        }

        //发送验证码
        String captcha = String.valueOf((int)(Math.random() * 1000000));
        if (!emailApi.sendGeneralEmail("验证码", "验证码：" + captcha, sendCaptchaDTO.getEmail())){
            result.setMessage("发送验证码失败");
            result.setSuccess(false);
            return result;
        }

        //保存验证码到Redis，区分使用场景
        redisTemplate.opsForValue().set(captchaKey, captcha, 5, java.util.concurrent.TimeUnit.MINUTES); // 验证码有效期5分钟

        result.setSuccess(true);
        return result;
    }
}