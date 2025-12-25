package org.example.qyuanuser.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@TableName("user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
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

    @TableField("avatar")
    private String avatar;
    /**
     * 加密密码
     *
     * @param rawPassword 明文密码
     */
    public void encodePassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.password = encoder.encode(rawPassword);
        System.out.println("加密后的密码：" + this.password);
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
