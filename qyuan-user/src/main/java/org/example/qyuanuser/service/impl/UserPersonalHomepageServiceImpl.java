package org.example.qyuanuser.service.impl;

import com.alibaba.fastjson2.JSONObject;
import org.example.qyuancommon.Result;
import org.example.qyuanuser.service.MinioService;
import org.example.qyuanuser.service.UserPersonalHomepageService;
import org.example.qyuanuser.mapper.UserMapper;
import org.example.qyuanuser.entity.User;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.qyuanuser.Result.PersonalInfoResult;
import jakarta.annotation.Resource;
import org.example.qyuanuser.DTO.personalHomepage.UpdatePersonalInfoDTO;
import org.example.qyuanuser.Result.CommonResult;
import org.springframework.stereotype.Service;
import org.example.qyuanuser.Result.VIPStateResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserPersonalHomepageServiceImpl extends ServiceImpl<UserMapper, User> implements UserPersonalHomepageService {
    
    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private MinioService minioService;
    private static final String USER_INFO_CACHE_PREFIX = "user:info:";

    private static final int CACHE_EXPIRE_TIME = 3600; // 1小时过期时间

    @Override
    public CommonResult updatePersonalInfo(UpdatePersonalInfoDTO updatePersonalInfoDTO, int user_id) {
        CommonResult result = new CommonResult();
        User user = userMapper.selectById(user_id);
        if (user == null){
            throw new RuntimeException("用户不存在");
        }
        //对每个UpdatePersonalInfoDTO的属性进行判断，如果不为空则更新user到它
        if (updatePersonalInfoDTO.getUsername() != null) {
            user.setUsername(updatePersonalInfoDTO.getUsername());
        }
        if (updatePersonalInfoDTO.getFirst_name() != null) {
            user.setFirstName(updatePersonalInfoDTO.getFirst_name());
        }
        if (updatePersonalInfoDTO.getLast_name() != null) {
            user.setLastName(updatePersonalInfoDTO.getLast_name());
        }
        if (updatePersonalInfoDTO.getInstitution() != null) {
            user.setInstitution(updatePersonalInfoDTO.getInstitution());
        }
        if (updatePersonalInfoDTO.getResearch_direction() != null) {
            user.setResearchDirection(updatePersonalInfoDTO.getResearch_direction());
        }
        if(updatePersonalInfoDTO.getOrcid() != null){
            user.setOrcidCode(updatePersonalInfoDTO.getOrcid());
        }

        int updateResult = userMapper.updateById(user);
        if (updateResult > 0) {
            // 更新成功后，删除Redis中的缓存，以便下次获取时重新从数据库加载
            String userInfoKey = USER_INFO_CACHE_PREFIX + user_id;
            redisTemplate.delete(userInfoKey);
        }
        result.setSuccess(true);
        return result;
    }

    @Override
    public PersonalInfoResult getPersonalInfo(int user_id) {
        String cacheKey = USER_INFO_CACHE_PREFIX + user_id;
        
        // 先从Redis缓存中获取用户信息
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);

        if (cachedUser != null) {
            PersonalInfoResult result = new PersonalInfoResult();
            result.setData(cachedUser.getUserId(),cachedUser.getUsername(), cachedUser.getEmail(), cachedUser.getFirstName(),
                cachedUser.getLastName(), cachedUser.getInstitution(), 
                cachedUser.getResearchDirection(), cachedUser.getOrcidCode(), cachedUser.getPermissionLevel(), String.valueOf(cachedUser.getExpireTime()),cachedUser.getUseTimes(),
                    cachedUser.getAvatar());
            result.setSuccess(true);
            return result;
        }
        
        // 缓存中没有，从数据库查询
        User user = userMapper.selectById(user_id);
        if (user == null){
            throw new RuntimeException("用户不存在");
        }
        
        // 将查询结果存入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        
        PersonalInfoResult result = new PersonalInfoResult();
        result.setData(user.getUserId(),user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(),
            user.getInstitution(), user.getResearchDirection(), user.getOrcidCode(), user.getPermissionLevel(), String.valueOf(user.getExpireTime()), user.getUseTimes(),
                user.getAvatar());
        result.setSuccess(true);
        return result;
    }

    @Override
    public VIPStateResult getVipState(int user_id) {
        String cacheKey = USER_INFO_CACHE_PREFIX + user_id;
        
        // 先从Redis缓存中获取VIP状态
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            VIPStateResult result = new VIPStateResult();
            result.setData(cachedUser.getPermissionLevel().intValue(),
                    String.valueOf(cachedUser.getExpireTime()), cachedUser.getUseTimes().intValue());
            result.setSuccess(true);
            return result;
        }
        
        // 缓存中没有，从数据库查询
        User user = userMapper.selectById(user_id);
        if (user == null){
            throw new RuntimeException("用户不存在");
        }
        
        // 将查询结果存入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        
        VIPStateResult result = new VIPStateResult();
        result.setData(user.getPermissionLevel().intValue(), String.valueOf(user.getExpireTime()), user.getUseTimes().intValue());
        result.setSuccess(true);
        return result;
    }

    @Override
    public List<JSONObject> batchGetPersonalInfo(List<Integer> userIds) {
        List<JSONObject> results = new ArrayList<>();
        //批量获取用户信息
        listByIds(userIds).forEach(user -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("avatar", user.getAvatar());
            jsonObject.put("username", user.getUsername());
            jsonObject.put("user_id", user.getUserId());
            results.add(jsonObject);
        });
        return results;
    }

    @Override
    public Result<Object> updateAvatar(MultipartFile avatar, int userId) {
            String picture= null;
            if(avatar != null){
                try{
                    String folder="avatar";
                    String objectKey = minioService.uploadFile(avatar, folder);
                    picture = minioService.getPublicUrl(objectKey);
                    User user = userMapper.selectById(userId);
                    user.setAvatar(picture);
                    userMapper.updateById(user);
                    redisTemplate.delete(USER_INFO_CACHE_PREFIX + userId);
                    return Result.ok("上传成功", picture);
                } catch (Exception e){
                    log.error("上传头像失败", e);
                   throw new RuntimeException("上传头像失败");
                }
            }
            return Result.fail("上传失败");
    }
}
