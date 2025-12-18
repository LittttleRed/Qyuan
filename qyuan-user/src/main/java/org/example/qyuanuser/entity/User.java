package org.example.qyuanuser.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    @TableField("permission_level")
    private Integer permissionLevel;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("use_times")
    private Integer useTimes;

    /**
     * 加密密码
     *
     * @param rawPassword 明文密码
     */
    public void setPassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.password = encoder.encode(rawPassword);
    }

    /**
     * 验证明文密码是否与加密密码匹配
     *
     * @param rawPassword 明文密码
     * @return 是否匹配
     */
    public boolean verifyPassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, this.password);
    }
}
