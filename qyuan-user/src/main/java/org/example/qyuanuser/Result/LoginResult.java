package org.example.qyuanuser.Result;

import lombok.Data;

@Data
public class LoginResult {
    private boolean success;
    //失败传message
    private String message;
    //成功传token
    private String token;
    
}
