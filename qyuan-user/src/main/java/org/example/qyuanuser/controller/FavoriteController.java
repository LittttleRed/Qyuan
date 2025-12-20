package org.example.qyuanuser.controller;

import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import org.example.qyuanuser.service.UserFavoriteService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController 
@RequestMapping("/user/favorite")
public class FavoriteController {
    @Resource
    private UserFavoriteService favoriteService;

    @PostMapping("/papers")
    public void addPaper(Integer folderId, Integer paperId, String paperTitle) {

    }

    @DeleteMapping("/papers")
    public void deletePaper(Integer folderId, Integer paperId) {

    }

    @PostMapping("/folders")
    public void addFolder(String folderName) {

    }

    @DeleteMapping("/folders")
    public void deleteFolder(Integer folderId) {

    }

    @GetMapping("/folders")
    public String getFolders(@RequestParam String param) {
        return new String();
    }

    @GetMapping("/folders/records")
    public String getFolderRecords(@RequestParam String param) {
        return new String();
    }
    
    @PatchMapping("/folders")
    public void renameFolder(Integer folderId, String folderName) {

    }

    @PatchMapping("/folders/state")
    public void changeFolderState(Integer folderId, Integer isPublic) {

    }
}
