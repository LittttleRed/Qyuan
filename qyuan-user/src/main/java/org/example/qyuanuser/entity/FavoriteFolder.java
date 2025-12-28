package org.example.qyuanuser.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName("favorite_folder")
public class FavoriteFolder {
    
    @TableId(type = IdType.AUTO)
    private Integer folderId;

    @TableField("user_id")
    private Integer userId;

    @TableField("folder_name")
    private String folderName;

    @TableField("is_public")
    private Integer isPublic;

    @TableField("paper_count")
    private Integer paperCount;

}

