package com.example.qyuanpaperrd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import com.example.qyuanpaperrd.entity.AuthorPaper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 引用信息DTO
 */
@Data
@Accessors(chain = true)
@Schema(description = "论文引用信息")
public class CitationDTO {

    @Schema(description = "引用ID", example = "1")
    private Long citationId;

    @Schema(description = "被引用的论文ID", example = "2")
    private Long citedPaperId;

    @Schema(description = "引用论文标题", example = "Deep Learning Fundamentals")
    private String citedPaperTitle;

    @Schema(description = "引用论文作者", example = "Goodfellow et al.")
    private String citedPaperAuthors;

    @Schema(description = "引用论文期刊", example = "Nature")
    private String citedPaperJournal;

    @Schema(description = "引用论文年份", example = "2023")
    private String citedPaperYear;

    @Schema(description = "DOI", example = "10.1000/nature-2023")
    private String citedPaperDoi;

    @Schema(description = "引用位置", example = "Page 15-20")
    private String citationLocation;

    @Schema(description = "引用类型", example = "formal")
    private String citationType;

    @Schema(description = "引用作者信息", example = "[{\"lastName\": \"Zhang\", \"firstName\": \"Wei\", \"rank\": 1, \"orcid\": \"0000-0001-1234-5678\", \"affiliation\": \"Tsinghua University\"}]")
    private List<AuthorDTO> authors;

    @Schema(description = "创建时间", example = "2023-06-15T10:30:00")
    private LocalDateTime createdAt;
}