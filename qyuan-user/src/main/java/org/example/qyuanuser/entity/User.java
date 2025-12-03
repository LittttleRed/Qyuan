package org.example.qyuanuser.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@TableName("user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Integer userId;

    @TableField("username")
    private String username;

    @TableField("email")
    private String email;

    @TableField("password")
    private String password;

    @TableField("institution")
    private String institution;

    @TableField("first_name")
    private String firstName;

    @TableField("last_name")
    private String lastName;

    @TableField("research_direction")
    private String researchDirection;

    @TableField("orcid_code")
    private String orcidCode;

    @TableField("permisson_level")
    private Integer permissonLevel;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("use_times")
    private Integer useTimes;
}
