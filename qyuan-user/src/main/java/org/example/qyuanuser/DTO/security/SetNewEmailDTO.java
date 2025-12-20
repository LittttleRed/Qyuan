package org.example.qyuanuser.DTO.security;

import lombok.Data;

@Data
public class SetNewEmailDTO {
    private String email;
    private String captcha;

    public boolean isFull() {
        return email != null && captcha != null;
    }
}
