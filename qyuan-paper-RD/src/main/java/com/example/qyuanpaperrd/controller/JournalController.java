package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.dto.JournalDTO;
import com.example.qyuanpaperrd.service.JournalService;
import com.example.qyuanpaperrd.util.HeaderUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;

/**
 * 期刊控制器
 */
@Slf4j
@RestController
@RequestMapping("/paper/journals")
@RequiredArgsConstructor
@Validated
@Tag(name = "期刊管理", description = "期刊详情获取等接口")
public class JournalController {

    private final JournalService journalService;

    @GetMapping("/{journalId}")
    @Operation(summary = "根据期刊ID获取期刊详情", description = "通过期刊ID获取期刊的详细信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "期刊不存在")
    })
    public Result<JournalDTO> getJournalById(
            @Parameter(description = "期刊ID", required = true)
            @PathVariable
            @NotNull(message = "期刊ID不能为空")
            Long journalId,
            HttpServletRequest request) {

        JournalDTO journal = journalService.getJournalById(journalId);
        if (journal == null) {
            log.warn("期刊不存在 - ID: {}", journalId);
            return Result.error(404, "期刊不存在");
        }

        log.info("期刊详情获取成功 - ID: {}", journalId);
        return Result.success(journal);
    }

}