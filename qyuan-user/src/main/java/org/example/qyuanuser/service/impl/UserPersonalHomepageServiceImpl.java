package org.example.qyuanuser.service.impl;

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

@Service
public class UserPersonalHomepageServiceImpl extends ServiceImpl<UserMapper, User> implements UserPersonalHomepageService {
    
    @Resource
    private UserMapper userMapper;

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

        userMapper.updateById(user);
        result.setSuccess(true);
        return result;
    }

    @Override
    public PersonalInfoResult getPersonalInfo(int user_id) {
        User user = userMapper.selectById(user_id);
        if (user == null){
            throw new RuntimeException("用户不存在");
        }
        PersonalInfoResult result = new PersonalInfoResult();
        result.setData(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getInstitution(), user.getResearchDirection(), user.getOrcidCode());
        result.setSuccess(true);
        return result;
    }

    @Override
    public VIPStateResult getVipState(int user_id) {
        User user = userMapper.selectById(user_id);
        if (user == null){
            throw new RuntimeException("用户不存在");
        }
        VIPStateResult result = new VIPStateResult();
        result.setData(user.getPermissionLevel().intValue(), user.getExpireTime().toString(), user.getUseTimes().intValue());
        result.setSuccess(true);
        return result;
    }
}
