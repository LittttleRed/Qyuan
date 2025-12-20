package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.dto.ClaimRequest;
import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.service.UserInteractionService;
import com.example.qyuanpaperrd.util.HeaderUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 用户交互控制器 - 用户个人功能
 */
@Slf4j
@RestController
@RequestMapping("/paper/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户交互", description = "用户认领功能")
public class UserInteractionController {

    private final UserInteractionService userInteractionService;

    @PostMapping("/claims")
    @Operation(summary = "认领论文", description = "用户认领自己的论文")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "认领申请已提交"),
        @ApiResponse(responseCode = "500", description = "认领失败")
    })
    public ResponseEntity<Result<String>> claimPaper(
            @Parameter(description = "认领请求", required = true)
            @Valid @RequestBody ClaimRequest claimRequest,

            HttpServletRequest request) {
        try {
            Long userId = HeaderUtil.getUserId(request);
            userInteractionService.claimPaper(userId, claimRequest);
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "获取认领失败")
    })
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
}
