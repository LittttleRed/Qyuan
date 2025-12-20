package org.example.qyuanuser.controller;

import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.example.qyuanuser.service.UserSystemService;

@RestController
@RequestMapping("/user/system")
public class SystemController {
    @Resource
    private UserSystemService systemService;

    @PostMapping("/theme")
    public void updateTheme(String theme){
        
    }
}
