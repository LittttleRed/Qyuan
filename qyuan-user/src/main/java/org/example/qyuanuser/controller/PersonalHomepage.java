package org.example.qyuanuser.controller;

import org.example.qyuanuser.service.UserPersonalHomepageService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/user/personalHomepage")
public class PersonalHomepage {
    @Resource
    private UserPersonalHomepageService userPersonalHomepageService;

    @PatchMapping("/info")
    public void updateInfo(String username, String email, String institution, String firstName, String lastName, String researchDirection, String orcidCode){

    }

    @GetMapping("/info")
    public void getInfo(@RequestParam String param) {

    }
    

    @GetMapping("vip")
    public void getVipState(@RequestParam String param) {

    }
}
