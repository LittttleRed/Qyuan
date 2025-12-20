package org.example.qyuanuser.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.example.qyuanuser.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.example.qyuanuser.service.UserSecurityService;
import org.example.qyuanuser.DTO.security.*;

import org.example.qyuanuser.entity.User;


@Service
public class UserSecurityServiceImpl extends ServiceImpl<UserMapper, User> implements UserSecurityService {
    
    @Resource
    private UserMapper userMapper;

    @Override
    public boolean setNewPassword(SetNewPasswordDTO setNewPasswordDTO) {
        return false;
    }
    
    @Override
    public boolean setNewEmail(SetNewEmailDTO setNewEmailDTO) {
        return false;
    }
}
