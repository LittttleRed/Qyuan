package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.dto.PatentDTO;
import com.example.qyuanpaperrd.service.PatentService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 专利控制器
 */
@Slf4j
@RestController
@RequestMapping("/paper/patents")
@RequiredArgsConstructor
@Validated
@Tag(name = "专利管理", description = "专利搜索、详情获取等接口")
public class PatentController {

    private final PatentService patentService;

    @GetMapping("/{patentId}")
    @Operation(summary = "根据专利ID获取专利详情", description = "通过专利ID获取专利的详细信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "专利不存在")
    })
    public Result<PatentDTO> getPatentById(
            @Parameter(description = "专利ID", required = true)
            @PathVariable
            @NotNull(message = "专利ID不能为空")
            Integer patentId,
            HttpServletRequest request) {

        PatentDTO patent = patentService.getPatentById(patentId);
        if (patent == null) {
            log.warn("专利不存在 - ID: {}", patentId);
            return Result.error(404, "专利不存在");
        }

        log.info("专利详情获取成功 - ID: {}", patentId);
        return Result.success(patent);
    }

}