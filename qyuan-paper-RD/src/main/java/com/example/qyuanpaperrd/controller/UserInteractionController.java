package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.dto.ClaimRequest;
import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.entity.Claim;
import com.example.qyuanpaperrd.service.UserInteractionService;
import com.example.qyuanpaperrd.service.ClaimService;
import com.example.qyuanpaperrd.common.PageResult;
import com.alibaba.fastjson2.JSONObject;
import com.example.qyuanpaperrd.service.UserPaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;

/**
 * 用户交互控制器 - 处理用户个人相关的功能
 */
@Slf4j
@RestController
@RequestMapping("/paper/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户交互", description = "处理用户个人相关的功能接口")
public class UserInteractionController {

    private final UserInteractionService userInteractionService;

    private final ClaimService claimService;

    private final UserPaperService userPaperService;

    @PostMapping("/claims")
    @Operation(summary = "认领论文", description = "用户主动认领某篇论文")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "认领申请已提交"),
        @ApiResponse(responseCode = "500", description = "认领失败")
    })
    public ResponseEntity<Result<String>> claimPaper(
            @Parameter(description = "论文ID", required = true)
            @RequestParam Long paperId,

            @Parameter(description = "认领信息图片文件")
            @RequestParam(required = false) MultipartFile claimPicture,

            @RequestHeader("USER-ID") Long userId,
            @RequestParam String paper_title) {
        try {
            ClaimRequest claimRequest = new ClaimRequest();
            claimRequest.setPaperId(paperId);
            claimRequest.setClaimPicture(claimPicture);
            
            claimService.submitClaim(userId, claimRequest, paper_title);
            return ResponseEntity.ok(Result.success("认领申请已提交", "success"));
        } catch (Exception e) {
            log.error("认领论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("认领失败：" + e.getMessage()));
        }
    }

    @GetMapping("/getClaims")
    @Operation(summary = "获取认领记录", description = "获取用户的论文认领记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "获取认领失败")
    })
    public ResponseEntity<Result<PageResult<Claim>>> getUserClaims(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("USER-ID") Long userId,

            @Parameter(description = "认领状态")
            @RequestParam(required = false) Integer status,

            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        try {
            PageResult<Claim> result = claimService.getUserClaims(userId, status, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("获取认领记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取认领失败：" + e.getMessage()));
        }
    }
    @GetMapping("/getAllClaim")
    @Operation(summary = "管理员获取所有认领记录", description = "管理员获取所有认领记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "获取认领失败")
    })
    public ResponseEntity<Result<PageResult<Claim>>> getAllClaims(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") @Min(1) Integer size,

            @Parameter(description = "认领状态")
            @RequestParam(required = false) Integer status
    ) {
        try {
            PageResult<Claim> result = claimService.getAllClaims(page, size, status);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("获取认领记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取认领失败：" + e.getMessage()));
        }
    }

    @PostMapping("/genClaims")
    @Operation(summary = "生成认领记录", description = "生成认领记录")
    public ResponseEntity<Result<ArrayList<Claim>>> genClaims(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("USER-ID") Long userId,

            @RequestBody JSONObject data
            ) {
        try {
            String last_name = data.getString("last_name");
            String first_name = data.getString("first_name");
            String orcid = data.getString("orcid");
            ArrayList<Claim> claims =claimService.genClaims(userId, last_name, first_name, orcid);
            return ResponseEntity.ok(Result.success(claims));
        } catch (Exception e) {
            log.error("生成认领记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(e.getMessage()));
        }
    }
    /*
        由于已经根据orcid和fullname生成了认领记录,所以只要用户更新,就传为3
        如果是管理员更新,就传为3或者2
     */
    @PostMapping("/updateClaim/{claim_id}")
    @Operation(summary = "更新认领记录", description = "更新认领记录")
    public ResponseEntity<Result<String>> updateClaim(
            @PathVariable("claim_id") Long claim_id,
            @RequestBody JSONObject data
            ) {
        try {
            Integer status = data.getInteger("status");
            Long user_id = data.getLong("user_id");
            Long paper_id = data.getLong("paper_id");
            claimService.updateClaim(claim_id, status, user_id, paper_id);
            return ResponseEntity.ok(Result.success("更新成功"));
        } catch (Exception e) {
            log.error("更新认领记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(e.getMessage()));
        }
    }

    @GetMapping("/papers")
    @Operation(summary = "获取我的论文", description = "获取用户相关的论文列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "获取失败")
    })
    public ResponseEntity<Result<PageResult<com.example.qyuanpaperrd.dto.PaperDTO>>> getUserPapers(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("USER-ID") Long userId,

            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        try {
            PageResult<PaperDTO> result = userPaperService.getUserPapers(userId, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("获取用户论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取用户论文失败：" + e.getMessage()));
        }
    }

    @GetMapping("/getUsers")
    @Operation(summary = "获取论文所属用户", description = "获取论文所属用户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "获取失败")
    })
    public ResponseEntity<Result<Object>> getUsers(
            @Parameter(description = "论文ID", required = true)
            @RequestParam Long paperId
            ) {
        try {
            Object result = userPaperService.getUsers(paperId);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("获取用户论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取用户论文失败：" + e.getMessage()));
        }
    }


}