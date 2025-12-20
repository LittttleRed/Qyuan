package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.dto.PatentDTO;
import com.example.qyuanpaperrd.service.PatentService;
import com.example.qyuanpaperrd.util.HeaderUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @GetMapping("/{patentNumber}")
    @Operation(summary = "获取专利详情", description = "根据专利申请号获取专利详细信息")
    public ResponseEntity<Result<PatentDTO>> getPatentByNumber(
            @Parameter(description = "专利申请号", required = true)
            @PathVariable @NotBlank String patentNumber,

            HttpServletRequest request) {

        Long userId = HeaderUtil.getUserIdOptional(request);
        log.info("获取专利详情: patentNumber={}, userId={}", patentNumber, userId);

        PatentDTO patent = patentService.getPatentByNumber(patentNumber, userId);
        if (patent == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.notFound("专利不存在"));
        }

        return ResponseEntity.ok(Result.success(patent));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索专利", description = "根据关键词、发明人、专利权人或国家搜索专利")
    public ResponseEntity<Result<PageResult<PatentDTO>>> searchPatents(
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "发明人姓名")
            @RequestParam(required = false) String inventor,

            @Parameter(description = "专利权人")
            @RequestParam(required = false) String assignee,

            @Parameter(description = "国家")
            @RequestParam(required = false) String country,

            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,

            @Parameter(description = "页大小", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) Integer pageSize) {

        log.info("搜索专利: keyword={}, inventor={}, assignee={}, country={}, pageNum={}, pageSize={}",
                keyword, inventor, assignee, country, pageNum, pageSize);

        PageResult<PatentDTO> result = patentService.searchPatents(keyword, inventor, assignee, country, pageNum, pageSize);

        return ResponseEntity.ok(Result.success(result));
    }
}
