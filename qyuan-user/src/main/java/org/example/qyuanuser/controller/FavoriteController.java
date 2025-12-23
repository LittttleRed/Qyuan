package org.example.qyuanuser.controller;

import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import org.example.qyuancommon.Result;
import org.example.qyuanuser.DTO.favorite.FolderDTO;
import org.example.qyuanuser.DTO.favorite.PaperDTO;
import org.example.qyuanuser.Result.CommonResult;
import org.example.qyuanuser.service.UserFavoriteService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.example.qyuanuser.Result.FolderSpecInfoResult;
import org.example.qyuanuser.Result.RecordSepcInfoResult;

@RestController 
@RequestMapping("/user/favorite")
public class FavoriteController {
    @Resource
    private UserFavoriteService favoriteService;

    @PostMapping("/papers")
    public Result<Object> addPaper(@RequestBody PaperDTO paperDTO, @RequestHeader ("USER-ID") int user_id) {
        try {
            CommonResult result = favoriteService.addPaper(paperDTO, user_id);
            if (result.isSuccess()){
                return Result.ok();
            }
            else{
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @DeleteMapping("/papers")
    public Result<Object> deletePaper(@RequestBody PaperDTO paperDTO, @RequestHeader ("USER-ID") int user_id) {
        try {
            CommonResult result = favoriteService.deletePaper(paperDTO, user_id);
            if (result.isSuccess()){
                return Result.ok();
            }
            else{
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/folders")
    public Result<Object> addFolder(@RequestBody FolderDTO folderDTO, @RequestHeader ("USER-ID") int user_id) {
        try {
            CommonResult result = favoriteService.addFolder(folderDTO, user_id);
            if (result.isSuccess()){
                return Result.ok();
            }
            else{
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @DeleteMapping("/folders")
    public Result<Object> deleteFolder(@RequestBody Integer folderId) {
        try {
            CommonResult result = favoriteService.deleteFolder(folderId);
            if (result.isSuccess()){
                return Result.ok();
            }
            else{
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/folders")
    public Result<Object> getFolders(@RequestHeader ("USER-ID") int user_id) {
        try {
            FolderSpecInfoResult result = favoriteService.getFolderSpecInfo(user_id);
            if (result.isSuccess()){
                return Result.ok(result.getFolderSpecInfoes());
            }
            else{
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/folders/records")
    public Result<Object> getFolderRecords(@RequestBody Integer folder_id, @RequestHeader ("USER-ID") int user_id) {
        try {
            RecordSepcInfoResult result = favoriteService.getRecordSpecInfo(user_id, user_id);
            if (result.isSuccess()){
                return Result.ok(result.getRecordSepcInfoes());
            }
            else{
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    @PatchMapping("/folders")
    public Result<Object> renameFolder(@RequestBody Integer folderId, String folderName, @RequestHeader ("USER-ID") int user_id) {
        try {
            CommonResult result = favoriteService.renameFolder(folderId, folderName, user_id);
            if (result.isSuccess()){
                return Result.ok();
            }
            else{
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
        }


    @PatchMapping("/folders/state")
    public Result<Object> changeFolderState(@RequestBody Integer folderId, Integer isPublic, @RequestHeader ("USER-ID") int user_id) {
        try {
            CommonResult result = favoriteService.changeFolderState(folderId, isPublic, user_id);
            if (result.isSuccess()){
                return Result.ok();
            }
            else{
                return Result.fail(result.getMessage());
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
        }
    }
