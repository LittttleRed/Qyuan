package org.example.qyuanuser.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.qyuanuser.entity.User;
import org.example.qyuanuser.mapper.UserMapper;
import org.example.qyuanuser.service.UserSystemService;
import org.springframework.stereotype.Service;

@Service
public class UserSystemServiceImpl extends ServiceImpl<UserMapper, User> implements UserSystemService {
    
}
