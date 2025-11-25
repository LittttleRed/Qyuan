package com.example.qyuanpaperrd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * 论文导出请求DTO
 */
@Data
@Schema(description = "论文导出请求")
public class PaperExportRequest {

    @Schema(description = "论文ID列表", example = "[1, 2, 3]")
    private List<Long> paperIds;

    @Schema(description = "导出格式：bibtex, ris, endnote", example = "bibtex")
    private String format;

    @Schema(description = "是否包含引用信息", example = "false")
    private Boolean includeCitations;

    @Schema(description = "是否包含作者信息", example = "true")
    private Boolean includeAuthors;

    @Schema(description = "是否包含摘要", example = "true")
    private Boolean includeAbstract;
}