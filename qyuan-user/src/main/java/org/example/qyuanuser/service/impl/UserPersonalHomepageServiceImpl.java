package org.example.qyuanuser.service.impl;

import org.example.qyuanuser.service.UserPersonalHomepageService;
import org.example.qyuanuser.mapper.UserMapper;
import org.example.qyuanuser.entity.User;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserPersonalHomepageServiceImpl extends ServiceImpl<UserMapper, User> implements UserPersonalHomepageService {
    
}
