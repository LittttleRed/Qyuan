package org.example.qyuanuser.DTO.auth;

import lombok.Data;

@Data
public class SendCaptchaDTO {
    private String email;
    private String scene;

    public boolean isFull() {
        return email != null && scene != null;
    }

    public boolean isEmailValid() {
        return email != null && email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }
}
