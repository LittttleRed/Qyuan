package org.example.qyuanuser.Result;

import lombok.Data;

@Data
public class RegisterResult {
    private boolean success;
    //失败传String
    private String message;
    //成功传id
    private Integer id;
}