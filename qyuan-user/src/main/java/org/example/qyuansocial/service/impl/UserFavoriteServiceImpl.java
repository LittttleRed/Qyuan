package org.example.qyuanuser.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.example.qyuanuser.service.UserFavoriteService;
import org.example.qyuanuser.mapper.FavoriteRecordMapper;
import org.example.qyuanuser.mapper.FavoriteFolderMapper;
import org.example.qyuanuser.entity.FavoriteRecord;

@Service
public class UserFavoriteServiceImpl extends ServiceImpl<FavoriteRecordMapper, FavoriteRecord> implements UserFavoriteService {

    @Resource
    private FavoriteRecordMapper favoriteRecordMapper;
    
    @Resource
    private FavoriteFolderMapper favoriteFolderMapper;
    
}