package org.example.qyuanuser.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.example.qyuanuser.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.example.qyuanuser.service.UserSecurityService;
import org.example.qyuanuser.DTO.security.*;
import org.example.qyuanuser.Result.CommonResult;
import org.example.qyuanuser.entity.User;


@Service
public class UserSecurityServiceImpl extends ServiceImpl<UserMapper, User> implements UserSecurityService {
    
    @Resource
    private UserMapper userMapper;

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
        //TODO：验证captcha

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

        //TODO：验证captcha

        //获取user
        User user = userMapper.selectById(user_id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setEmail(setNewEmailDTO.getEmail());
        userMapper.updateById(user);
        result.setSuccess(true);
        return result;
    }
}
