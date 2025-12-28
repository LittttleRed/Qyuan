package org.example.qyuanuser.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.example.qyuanuser.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.example.qyuanuser.service.UserSecurityService;
import org.example.qyuanuser.DTO.security.*;
import org.example.qyuanuser.Result.CommonResult;
import org.example.qyuanuser.entity.User;
import org.example.qyuanuser.util.EmailUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;


@Service
public class    UserSecurityServiceImpl extends ServiceImpl<UserMapper, User> implements UserSecurityService {
    
    @Resource
    private UserMapper userMapper;

    @Resource
    @Qualifier("redisTemplateForCaptcha")
    private RedisTemplate<String, String> redisTemplate;
    private static final String USER_INFO_CACHE_PREFIX = "user:info:";

    private static final int CACHE_EXPIRE_TIME = 3600; // 1小时过期时间
    @Override
    public CommonResult setNewPassword(SetNewPasswordDTO setNewPasswordDTO, int user_id) {
        CommonResult result = new CommonResult();
        //参数校验
        if(!setNewPasswordDTO.isFull()){
            result.setMessage("缺少参数");
            result.setSuccess(false);
            return result;
        }
        if (!setNewPasswordDTO.isPasswordSame()){
            result.setMessage("两次输入的密码不一致");
            result.setSuccess(false);
            return result;
        }

        if (!setNewPasswordDTO.isPasswordValid()){
            result.setMessage("密码格式错误,应满足：8-20位，包含字母和数字");
            result.setSuccess(false);
            return result;
        }
        
        //获取user
        User user = userMapper.selectById(user_id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证更新密码验证码
        String captchaKey = "captcha:update_password:" + user.getEmail();
        String storedCaptcha = redisTemplate.opsForValue().get(captchaKey);
        if (storedCaptcha == null || !storedCaptcha.equals(setNewPasswordDTO.getCaptcha())) {
            result.setMessage("密码更新验证码错误或已过期");
            result.setSuccess(false);
            return result;
        }
        String userInfoKey = USER_INFO_CACHE_PREFIX + user_id;
        redisTemplate.delete(userInfoKey);
        // 验证码正确，删除已使用的验证码
        redisTemplate.delete(captchaKey);

        //修改密码
        user.setPassword(setNewPasswordDTO.getPassword());
        userMapper.updateById(user);
        result.setSuccess(true);
        return result;
    }
    
    @Override
    public CommonResult setNewEmail(SetNewEmailDTO setNewEmailDTO, int user_id) {
        CommonResult result = new CommonResult();
        //参数校验
        if(!setNewEmailDTO.isFull()){
            result.setMessage("缺少参数");
            result.setSuccess(false);
            return result;
        }
        if (!setNewEmailDTO.isEmailValid()){
            result.setMessage("邮箱格式错误");
            result.setSuccess(false);
            return result;
        }
        if (userMapper.getUserByEmail(setNewEmailDTO.getEmail()) != null){
            result.setMessage("邮箱已注册");
            result.setSuccess(false);
            return result;
        }

        //获取user
        User user = userMapper.selectById(user_id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证更新邮箱验证码
        String captchaKey = "captcha:update_email:" + setNewEmailDTO.getEmail();
        String storedCaptcha = redisTemplate.opsForValue().get(captchaKey);
        if (storedCaptcha == null || !storedCaptcha.equals(setNewEmailDTO.getCaptcha())) {
            result.setMessage("邮箱更新验证码错误或已过期");
            result.setSuccess(false);
            return result;
        }
        String userInfoKey = USER_INFO_CACHE_PREFIX + user_id;
        redisTemplate.delete(userInfoKey);
        // 验证码正确，删除已使用的验证码
        redisTemplate.delete(captchaKey);
        
        user.setEmail(setNewEmailDTO.getEmail());
        
        // 检查新邮箱是否为机构邮箱，如果是则设置权限级别为1
        if (EmailUtils.isInstitutionalEmail(setNewEmailDTO.getEmail()) && user.getPermissionLevel()==0) {
            user.setPermissionLevel(1); // 机构用户权限级别设为1
            user.setUseTimes(user.getUseTimes()+5);
        }
        
        userMapper.updateById(user);
        result.setSuccess(true);
        return result;
    }

    @Override
    public void payForVIP(Integer userId, LocalDateTime expireTime, String orderKey) {
         User user = userMapper.selectById(userId);
         user.setExpireTime(expireTime);
         // VIP用户权限级别为2，高于机构用户（级别1）和普通用户（级别0）
         user.setPermissionLevel(2);
         redisTemplate.delete(USER_INFO_CACHE_PREFIX + userId);
         userMapper.updateById(user);
    }

    @Override
    public CommonResult decreaseUseTimes(int user_id) {
        CommonResult result = new CommonResult();
        User user = userMapper.selectById(user_id);
        if (user == null) {
            result.setMessage("用户不存在");
            result.setSuccess(false);
            return result;
        }

        // 检查useTimes是否大于0，避免负数
        if (user.getUseTimes() <= 0) {
            result.setMessage("使用次数已为0，无法继续减少");
            result.setSuccess(false);
            return result;
        }

        // 使用MyBatis-Plus的update方法，只更新use_times字段
        user.setUseTimes(user.getUseTimes() - 1);
        userMapper.updateById(user);

        // 清除缓存以确保下次获取时是最新的数据
        String userInfoKey = USER_INFO_CACHE_PREFIX + user_id;
        redisTemplate.delete(userInfoKey);

        result.setSuccess(true);
        result.setMessage("使用次数减少成功");
        return result;
    }
}