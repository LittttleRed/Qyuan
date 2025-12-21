package com.example.qyuanpaperrd.controller;

import com.example.qyuanpaperrd.dto.PaperDTO;
import com.example.qyuanpaperrd.dto.PaperSearchRequest;
import com.example.qyuanpaperrd.dto.AuthorDTO;
import com.example.qyuanpaperrd.dto.PaperExportRequest;
import com.example.qyuanpaperrd.dto.CitationDTO;
import com.example.qyuanpaperrd.dto.PaperAddRequest;
import com.example.qyuanpaperrd.common.Result;
import com.example.qyuanpaperrd.common.PageResult;
import com.example.qyuanpaperrd.service.PaperService;
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
@RequestMapping("/paper/papers")
@RequiredArgsConstructor
@Validated
@Tag(name = "论文管理", description = "论文搜索、下载、详情获取、增删改查等接口")
public class PaperController {

    private final PaperService paperService;

    @GetMapping("/search")
    @Operation(summary = "搜索论文", description = "根据关键词、分类、期刊等条件搜索论文")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "搜索成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "500", description = "搜索失败")
    })
    public ResponseEntity<Result<PageResult<PaperDTO>>> searchPapers(
            @Parameter(description = "搜索关键词，可以为空")
            @RequestParam(required = false) String query,

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
            // 处理空关键词的情况
            if (query == null || query.trim().isEmpty()) {
                if (category_id == null) {
                    return ResponseEntity.badRequest()
                            .body(Result.error("搜索关键词或分类ID至少需要提供一个"));
                }
                query = ""; // 设置为空字符串，让搜索逻辑处理分类查询
            }

            // 构建搜索请求对象
            PaperSearchRequest request = new PaperSearchRequest();
            request.setKeyword(query.trim());
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "论文不存在"),
        @ApiResponse(responseCode = "500", description = "获取详情失败")
    })
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取下载链接成功"),
        @ApiResponse(responseCode = "500", description = "下载失败")
    })
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

    @GetMapping("/{paperId}/authors")
    @Operation(summary = "获取论文作者列表", description = "获取指定论文的所有作者信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "论文不存在"),
        @ApiResponse(responseCode = "500", description = "获取作者失败")
    })
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

    
    
    @PostMapping("/export")
    @Operation(summary = "导出论文信息", description = "导出论文为BibTeX或RIS格式")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "导出成功"),
        @ApiResponse(responseCode = "500", description = "导出失败")
    })
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

    @GetMapping("/{paperId}/references")
    @Operation(summary = "获取论文引用关系", description = "获取指定论文的引用信息或被引用信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "500", description = "获取引用关系失败")
    })
    public ResponseEntity<Result<List<CitationDTO>>> getPaperReferences(
            @Parameter(description = "论文ID", required = true)
            @PathVariable @NotNull Long paperId,

            @Parameter(description = "引用方向：citations（引用的文献）或 cited-by（引用该论文的文献）", required = true)
            @RequestParam @NotBlank String direction) {
        try {
            List<CitationDTO> citations;
            if ("citations".equalsIgnoreCase(direction)) {
                citations = paperService.getPaperCitations(paperId);
            } else if ("cited-by".equalsIgnoreCase(direction)) {
                citations = paperService.getPapersCitedBy(paperId);
            } else {
                return ResponseEntity.badRequest()
                        .body(Result.error("无效的引用方向，支持：citations, cited-by"));
            }
            return ResponseEntity.ok(Result.success(citations));
        } catch (Exception e) {
            log.error("获取{}引用关系失败", direction, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取引用关系失败：" + e.getMessage()));
        }
    }

    // ========== 管理功能接口 ==========

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @Operation(summary = "添加论文", description = "新增论文信息（管理员/编辑者权限）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "论文添加成功"),
        @ApiResponse(responseCode = "500", description = "添加失败")
    })
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
    @Operation(summary = "更新论文", description = "更新论文信息（管理员/编辑者权限）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "论文更新成功"),
        @ApiResponse(responseCode = "500", description = "更新失败")
    })
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
    @Operation(summary = "删除论文", description = "删除指定论文（管理员权限）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "论文删除成功"),
        @ApiResponse(responseCode = "500", description = "删除失败")
    })
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
    @Operation(summary = "批量导入论文", description = "批量导入论文信息（管理员/编辑者权限）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "批量导入成功"),
        @ApiResponse(responseCode = "500", description = "批量导入失败")
    })
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
