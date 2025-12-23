package org.example.qyuanuser.DTO.security;

import lombok.Data;

@Data
public class SetNewEmailDTO {
    private String email;
    private String captcha;

    public boolean isFull() {
        return email != null && captcha != null;
    }

    public boolean isEmailValid(){
        return email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }
    
}
