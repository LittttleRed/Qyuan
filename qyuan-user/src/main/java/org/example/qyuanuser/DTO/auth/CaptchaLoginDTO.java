package org.example.qyuanuser.DTO.auth;

import lombok.Data;

@Data
public class CaptchaLoginDTO {
    private String email;
    private String captcha;

    public boolean isFull() {
        return email != null && captcha != null;
    }
}
