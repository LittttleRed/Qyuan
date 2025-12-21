package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.PatentDTO;
import com.example.qyuanpaperrd.service.PaperService;
import com.example.qyuanpaperrd.service.PatentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 内容控制器 - 统一处理论文和专利的热门/最新内容
 */
@Slf4j
@RestController
@RequestMapping("/paper/contents")
@RequiredArgsConstructor
@Validated
@Tag(name = "内容管理", description = "获取热门/最新论文和专利的统一接口")
public class ContentController {

    private final PaperService paperService;
    private final PatentService patentService;

    @GetMapping("/popular")
    @Operation(summary = "获取热门内容", description = "获取热门论文或专利")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "500", description = "获取热门内容失败")
    })
    public ResponseEntity<Result<List<?>>> getPopularContent(
            @Parameter(description = "返回数量限制，默认10")
            @RequestParam(defaultValue = "10") @Min(1) Integer limit,

            @Parameter(description = "内容类型：paper（论文）或 patent（专利）", required = true)
            @RequestParam @NotBlank String type) {

        try {
            if ("paper".equalsIgnoreCase(type)) {
                List<PaperDTO> papers = paperService.getPopularPapers(limit);
                return ResponseEntity.ok(Result.success(papers));
            } else if ("patent".equalsIgnoreCase(type)) {
                List<PatentDTO> patents = patentService.getHotPatents(limit);
                return ResponseEntity.ok(Result.success(patents));
            } else {
                return ResponseEntity.badRequest()
                        .body(Result.error("无效的内容类型，支持：paper, patent"));
            }
        } catch (Exception e) {
            log.error("获取热门{}失败", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取热门内容失败：" + e.getMessage()));
        }
    }

    @GetMapping("/latest")
    @Operation(summary = "获取最新内容", description = "获取最新论文或专利")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "500", description = "获取最新内容失败")
    })
    public ResponseEntity<Result<List<?>>> getLatestContent(
            @Parameter(description = "返回数量限制，默认10")
            @RequestParam(defaultValue = "10") @Min(1) Integer limit,

            @Parameter(description = "内容类型：paper（论文）或 patent（专利）", required = true)
            @RequestParam @NotBlank String type) {

        try {
            if ("paper".equalsIgnoreCase(type)) {
                List<PaperDTO> papers = paperService.getLatestPapers(limit);
                return ResponseEntity.ok(Result.success(papers));
            } else if ("patent".equalsIgnoreCase(type)) {
                List<PatentDTO> patents = patentService.getLatestPatents(limit);
                return ResponseEntity.ok(Result.success(patents));
            } else {
                return ResponseEntity.badRequest()
                        .body(Result.error("无效的内容类型，支持：paper, patent"));
            }
        } catch (Exception e) {
            log.error("获取最新{}失败", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取最新内容失败：" + e.getMessage()));
        }
    }
}
