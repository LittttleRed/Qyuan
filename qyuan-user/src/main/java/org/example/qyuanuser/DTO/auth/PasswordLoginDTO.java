package org.example.qyuanuser.DTO.auth;

import lombok.Data;

@Data
public class PasswordLoginDTO {
    private String email;
    private String password;

    public boolean isFull() {
        return email != null && password != null;
    }
}
