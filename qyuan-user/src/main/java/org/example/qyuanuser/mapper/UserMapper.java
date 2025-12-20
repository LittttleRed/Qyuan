package org.example.qyuanuser.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.qyuanuser.entity.User;
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT * FROM user WHERE email = #{email}")
    User getUserByEmail(String email);
}
