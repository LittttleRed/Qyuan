package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.PaperSearchRequest;
import com.example.qyuanpaperrd.dto.PaperAddRequest;
import com.example.qyuanpaperrd.dto.AuthorDTO;
import com.example.qyuanpaperrd.dto.PaperExportRequest;
import com.example.qyuanpaperrd.dto.CitationDTO;
import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.service.PaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 论文控制器 - 标准RESTful API实现
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/papers")
@RequiredArgsConstructor
@Validated
@Tag(name = "论文管理", description = "论文搜索、下载、详情获取等接口")
public class PaperController {

    private final PaperService paperService;

    @GetMapping("/search")
    @Operation(summary = "搜索论文", description = "根据关键词、分类、期刊等条件搜索论文")
    public ResponseEntity<Result<PageResult<PaperDTO>>> searchPapers(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam @NotBlank String query,

            @Parameter(description = "搜索字段：title,author,abstract")
            @RequestParam(required = false) String search_field,

            @Parameter(description = "分类ID")
            @RequestParam(required = false) Long category_id,

            @Parameter(description = "期刊/来源")
            @RequestParam(required = false) String journal,

            @Parameter(description = "页码，从1开始")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") @Min(1) Integer size,

            @Parameter(description = "排序字段：relevance,title,author,date")
            @RequestParam(defaultValue = "relevance") String sort_by,

            @Parameter(description = "排序方向：asc,desc")
            @RequestParam(defaultValue = "desc") String order) {

        try {
            // 构建搜索请求对象
            PaperSearchRequest request = new PaperSearchRequest();
            request.setKeyword(query);
            request.setSearchField(search_field);
            request.setCategoryId(category_id);
            request.setJournalName(journal);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sort_by);
            request.setSortOrder(order);

            PageResult<PaperDTO> result = paperService.searchPapers(request);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("搜索论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("搜索失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{paperId}")
    @Operation(summary = "获取论文详情", description = "根据论文ID获取详细信息")
    public ResponseEntity<Result<PaperDTO>> getPaperById(
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId) {

        try {
            PaperDTO paper = paperService.getPaperById(paperId);
            if (paper == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.notFound("论文不存在"));
            }
            return ResponseEntity.ok(Result.success(paper));
        } catch (Exception e) {
            log.error("获取论文详情失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取详情失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{paperId}/download")
    @Operation(summary = "下载论文PDF", description = "获取论文PDF文件的下载链接")
    public ResponseEntity<Result<Map<String, String>>> downloadPaper(
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId) {

        try {
            String downloadUrl = paperService.downloadPaperPdf(paperId);
            Map<String, String> data = Map.of("download_url", downloadUrl);
            return ResponseEntity.ok(Result.success(data));
        } catch (Exception e) {
            log.error("下载论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("下载失败：" + e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "新增论文", description = "管理员添加新论文")
    public ResponseEntity<Result<PaperDTO>> addPaper(@Valid @RequestBody PaperAddRequest request) {
        try {
            PaperDTO paper = paperService.addPaper(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success("论文添加成功", paper));
        } catch (Exception e) {
            log.error("添加论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("添加论文失败：" + e.getMessage()));
        }
    }

    @PostMapping("/{paperId}/claim")
    @Operation(summary = "提交论文认领申请", description = "用户提交论文认领申请")
    public ResponseEntity<Result<String>> submitClaim(
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId,
            @Parameter(description = "认领证明图片")
            @RequestParam(required = false) String claim_picture) {

        try {
            // 这里需要从请求头或上下文获取用户ID
            // Long userId = getCurrentUserId();
            // claimService.submitClaim(userId, paperId, claim_picture);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success("认领申请提交成功", "success"));
        } catch (Exception e) {
            log.error("提交认领申请失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("提交失败：" + e.getMessage()));
        }
    }

    @GetMapping("/me/claims")
    @Operation(summary = "查看当前用户认领记录", description = "获取当前用户的论文认领记录")
    public ResponseEntity<Result<PageResult<Object>>> getUserClaims(
            @Parameter(description = "认领状态：1-待审核，2-驳回，3-通过")
            @RequestParam(required = false) Integer status,

            @Parameter(description = "页码，从1开始")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {

        try {
            // 这里需要从请求头或上下文获取用户ID
            // Long userId = getCurrentUserId();
            // PageResult<Claim> result = claimService.getUserClaims(userId, page, size);

            // 临时返回空结果
            PageResult<Object> result = PageResult.empty(page.longValue(), size.longValue());
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("获取认领记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取记录失败：" + e.getMessage()));
        }
    }

    @PutMapping("/{paperId}")
    @Operation(summary = "更新论文信息", description = "管理员更新论文信息")
    public ResponseEntity<Result<PaperDTO>> updatePaper(
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId,
            @Valid @RequestBody PaperAddRequest request) {
        try {
            PaperDTO paper = paperService.updatePaper(paperId, request);
            if (paper != null) {
                return ResponseEntity.ok(Result.success("论文更新成功", paper));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.notFound("论文不存在"));
            }
        } catch (Exception e) {
            log.error("更新论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("更新论文失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/{paperId}")
    @Operation(summary = "删除论文", description = "管理员删除论文")
    public ResponseEntity<Result<String>> deletePaper(
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId) {
        try {
            Boolean result = paperService.deletePaper(paperId);
            if (result) {
                return ResponseEntity.ok(Result.success("论文删除成功", String.valueOf(paperId)));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.notFound("论文不存在"));
            }
        } catch (Exception e) {
            log.error("删除论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("删除论文失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{paperId}/authors")
    @Operation(summary = "获取论文作者列表", description = "获取指定论文的所有作者信息")
    public ResponseEntity<Result<List<AuthorDTO>>> getPaperAuthors(
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId) {
        try {
            PaperDTO paper = paperService.getPaperById(paperId);
            if (paper == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.notFound("论文不存在"));
            }

            List<AuthorDTO> authors = paper.getAuthors();
            return ResponseEntity.ok(Result.success(authors));
        } catch (Exception e) {
            log.error("获取论文作者失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取作者失败：" + e.getMessage()));
        }
    }

    @GetMapping("/popular")
    @Operation(summary = "获取热门论文", description = "获取下载量最高的论文列表")
    public ResponseEntity<Result<List<PaperDTO>>> getPopularPapers(
            @Parameter(description = "返回数量限制，默认10")
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        try {
            List<PaperDTO> papers = paperService.getPopularPapers(limit);
            return ResponseEntity.ok(Result.success(papers));
        } catch (Exception e) {
            log.error("获取热门论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取热门论文失败：" + e.getMessage()));
        }
    }

    @GetMapping("/latest")
    @Operation(summary = "获取最新论文", description = "获取最新发表的论文列表")
    public ResponseEntity<Result<List<PaperDTO>>> getLatestPapers(
            @Parameter(description = "返回数量限制，默认10")
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        try {
            List<PaperDTO> papers = paperService.getLatestPapers(limit);
            return ResponseEntity.ok(Result.success(papers));
        } catch (Exception e) {
            log.error("获取最新论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取最新论文失败：" + e.getMessage()));
        }
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "按分类获取论文", description = "根据分类ID获取论文列表，支持分页")
    public ResponseEntity<Result<PageResult<PaperDTO>>> getPapersByCategory(
            @Parameter(description = "分类ID", required = true)
            @PathVariable @NotNull Long categoryId,

            @Parameter(description = "页码，从1开始")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        try {
            PageResult<PaperDTO> result = paperService.getPapersByCategory(categoryId, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("按分类获取论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取分类论文失败：" + e.getMessage()));
        }
    }

    @PostMapping("/export")
    @Operation(summary = "导出论文信息", description = "导出论文为BibTeX或RIS格式")
    public ResponseEntity<Result<String>> exportPapers(
            @Parameter(description = "导出请求", required = true)
            @Valid @RequestBody PaperExportRequest request) {
        try {
            String exportUrl = paperService.exportPapers(request);
            return ResponseEntity.ok(Result.success("导出成功", exportUrl));
        } catch (Exception e) {
            log.error("导出论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("导出失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{paperId}/citations")
    @Operation(summary = "获取论文引用信息", description = "获取指定论文的引用信息")
    public ResponseEntity<Result<List<CitationDTO>>> getPaperCitations(
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId) {
        try {
            List<CitationDTO> citations = paperService.getPaperCitations(paperId);
            return ResponseEntity.ok(Result.success(citations));
        } catch (Exception e) {
            log.error("获取论文引用失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取引用失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{paperId}/cited-by")
    @Operation(summary = "获取被引用信息", description = "获取引用该论文的其他论文")
    public ResponseEntity<Result<List<CitationDTO>>> getPapersCitedBy(
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId) {
        try {
            List<CitationDTO> citations = paperService.getPapersCitedBy(paperId);
            return ResponseEntity.ok(Result.success(citations));
        } catch (Exception e) {
            log.error("获取被引用信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取被引用失败：" + e.getMessage()));
        }
    }
}