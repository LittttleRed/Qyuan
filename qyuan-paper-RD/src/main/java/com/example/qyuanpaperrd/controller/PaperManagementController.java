package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.PaperAddRequest;
import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.service.PaperService;
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
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 论文管理控制器 - 管理员功能
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/papers")
@RequiredArgsConstructor
@Validated
@Tag(name = "论文管理（管理员）", description = "管理员专用的论文增删改查管理接口")
public class PaperManagementController {

    private final PaperService paperService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @Operation(summary = "添加论文", description = "新增论文信息")
    public ResponseEntity<Result<PaperDTO>> addPaper(
            @Parameter(description = "论文信息", required = true)
            @Valid @RequestBody PaperAddRequest request) {
        try {
            PaperDTO paper = paperService.addPaper(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success("论文添加成功", paper));
        } catch (Exception e) {
            log.error("添加论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("添加失败：" + e.getMessage()));
        }
    }

    @PutMapping("/{paperId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @Operation(summary = "更新论文", description = "更新论文信息")
    public ResponseEntity<Result<PaperDTO>> updatePaper(
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId,
            
            @Parameter(description = "论文信息", required = true)
            @Valid @RequestBody PaperAddRequest request) {
        try {
            PaperDTO paper = paperService.updatePaper(paperId, request);
            return ResponseEntity.ok(Result.success("论文更新成功", paper));
        } catch (Exception e) {
            log.error("更新论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("更新失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/{paperId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除论文", description = "删除指定论文")
    public ResponseEntity<Result<String>> deletePaper(
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId) {
        try {
            paperService.deletePaper(paperId);
            return ResponseEntity.ok(Result.success("论文删除成功", "success"));
        } catch (Exception e) {
            log.error("删除论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("删除失败：" + e.getMessage()));
        }
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @Operation(summary = "批量导入论文", description = "批量导入论文信息")
    public ResponseEntity<Result<Integer>> batchImportPapers(
            @Parameter(description = "论文列表", required = true)
            @Valid @RequestBody List<PaperAddRequest> papers) {
        try {
            int count = paperService.batchImportPapers(papers);
            return ResponseEntity.ok(Result.success("批量导入成功，共导入" + count + "篇论文", count));
        } catch (Exception e) {
            log.error("批量导入论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("批量导入失败：" + e.getMessage()));
        }
    }
}
