package org.example.qyuanuser.DTO.security;

import lombok.Data;

@Data
public class SetNewPasswordDTO {
    private String password;
    private String new_password_repeat;
    private String captcha;

    public boolean isFull() {
        return password != null && captcha != null && new_password_repeat != null;
    }

    public boolean isPasswordSame() {
        return password.equals(new_password_repeat);
    }

    public boolean isPasswordValid(){
        return password.matches("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$");
    }
}
