package org.example.qyuanuser.DTO.auth;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String password_repeat;
    private String email;
    private String captcha;

    public boolean isFull(){
        return username != null && password != null && password_repeat != null && email != null && captcha != null;
    }

    public boolean isPasswordSame(){
        return password.equals(password_repeat);
    }

    public boolean isEmailValid(){
        return email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }

    public boolean isUsernameValid(){
        return username.matches("^[a-zA-Z0-9_-]{4,16}$");
    }

    public boolean isPasswordValid(){
        return password.matches("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$");
    }
}
