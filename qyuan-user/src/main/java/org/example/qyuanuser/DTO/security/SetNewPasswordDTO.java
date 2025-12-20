package org.example.qyuanuser.DTO.security;

import lombok.Data;

@Data
public class SetNewPasswordDTO {
    private String password;
    private String captcha;

    public boolean isFull() {
        return password != null && captcha != null;
    }
}
