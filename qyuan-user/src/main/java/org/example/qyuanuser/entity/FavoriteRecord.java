package org.example.qyuanuser.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName("favorite_record")
public class FavoriteRecord {
    
    @TableId(type = IdType.AUTO)
    private Integer recordId;

    @TableField("folder_id")
    private Integer folderId;

    @TableField("paper_id")
    private Integer paperId;

    @TableField("paper_title")
    private String paperTitle;

    @TableField("user_id")
    private Integer userId;
}

