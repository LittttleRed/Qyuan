package org.example.qyuanuser.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.example.qyuanuser.entity.FavoriteFolder;

import java.util.List;

@Mapper
public interface FavoriteFolderMapper extends BaseMapper<FavoriteFolder> {

    @Select("SELECT * FROM favorite_folder WHERE user_id = #{userId}")
    List<FavoriteFolder> selectByUserId(Integer userId);
}
