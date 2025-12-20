package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.ClaimRequest;
import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.service.UserInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 用户交互控制器 - 用户个人功能
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户交互", description = "用户收藏、认领、历史记录等个人功能")
public class UserInteractionController {

    private final UserInteractionService userInteractionService;

    @PostMapping("/{userId}/favorites/{paperId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "收藏论文", description = "用户收藏指定论文")
    public ResponseEntity<Result<String>> favoritePaper(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId) {
        try {
            userInteractionService.favoritePaper(userId, paperId);
            return ResponseEntity.ok(Result.success("收藏成功", "success"));
        } catch (Exception e) {
            log.error("收藏论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("收藏失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}/favorites/{paperId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "取消收藏", description = "用户取消收藏论文")
    public ResponseEntity<Result<String>> unfavoritePaper(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId) {
        try {
            userInteractionService.unfavoritePaper(userId, paperId);
            return ResponseEntity.ok(Result.success("取消收藏成功", "success"));
        } catch (Exception e) {
            log.error("取消收藏失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("取消收藏失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{userId}/favorites")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "获取收藏列表", description = "获取用户的收藏论文列表")
    public ResponseEntity<Result<PageResult<PaperDTO>>> getUserFavorites(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        try {
            PageResult<PaperDTO> result = userInteractionService.getUserFavorites(userId, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("获取收藏列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取收藏失败：" + e.getMessage()));
        }
    }

    @PostMapping("/{userId}/claims")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "认领论文", description = "用户认领自己的论文")
    public ResponseEntity<Result<String>> claimPaper(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            
            @Parameter(description = "认领请求", required = true)
            @Valid @RequestBody ClaimRequest request) {
        try {
            userInteractionService.claimPaper(userId, request);
            return ResponseEntity.ok(Result.success("认领申请已提交", "success"));
        } catch (Exception e) {
            log.error("认领论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("认领失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{userId}/claims")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "获取认领记录", description = "获取用户的论文认领记录")
    public ResponseEntity<Result<PageResult<Object>>> getUserClaims(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            
            @Parameter(description = "认领状态")
            @RequestParam(required = false) Integer status,
            
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        try {
            PageResult<Object> result = userInteractionService.getUserClaims(userId, status, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("获取认领记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取认领失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{userId}/history")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "获取浏览历史", description = "获取用户的论文浏览历史")
    public ResponseEntity<Result<PageResult<PaperDTO>>> getUserHistory(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        try {
            PageResult<PaperDTO> result = userInteractionService.getUserHistory(userId, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("获取浏览历史失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取历史失败：" + e.getMessage()));
        }
    }

    @PostMapping("/{userId}/record-view/{paperId}")
    @Operation(summary = "记录浏览", description = "记录用户浏览论文的行为")
    public ResponseEntity<Result<String>> recordView(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId) {
        try {
            userInteractionService.recordView(userId, paperId);
            return ResponseEntity.ok(Result.success("浏览记录已保存", "success"));
        } catch (Exception e) {
            log.error("记录浏览失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("记录失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{userId}/profile")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "获取用户档案", description = "获取用户的学术档案信息")
    public ResponseEntity<Result<Map<String, Object>>> getUserProfile(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        try {
            Map<String, Object> profile = userInteractionService.getUserProfile(userId);
            return ResponseEntity.ok(Result.success(profile));
        } catch (Exception e) {
            log.error("获取用户档案失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取档案失败：" + e.getMessage()));
        }
    }
}
