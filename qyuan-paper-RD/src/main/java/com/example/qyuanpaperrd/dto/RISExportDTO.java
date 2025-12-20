package com.example.qyuanpaperrd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * RIS导出格式DTO
 */
@Data
@Schema(description = "RIS导出数据")
public class RISExportDTO {

    @Schema(description = "RIS类型", example = "JOUR")
    private String type;

    @Schema(description = "论文标题", example = "Deep Learning in Natural Language Processing")
    private String title;

    @Schema(description = "作者列表", example = "[Zhang W.; Li M.]")
    private String author;

    @Schema(description = "期刊名称", example = "Journal of AI Research")
    private String secondaryTitle;

    @Schema(description = "年份", example = "2023")
    private String year;

    @Schema(description = "期刊缩写", example = "J. AI Res.")
    private String secondaryTitle2;

    @Schema(description = "卷", example = "15")
    private String volume;

    @Schema(description = "期", example = "2")
    private String number;

    @Schema(description = "页码", example = "123-145")
    private String pages;

    @Schema(description = "出版商", example = "Academic Publishing")
    private String publisher;

    @Schema(description = "DOI", example = "10.1000/dl-nlp-2023")
    private String doi;

    @Schema(description = "URL", example = "https://example.com/paper.pdf")
    private String url;

    @Schema(description = "摘要", example = "This paper presents...")
    private String abstractText;

    @Schema(description = "关键词", example = "deep learning; NLP")
    private String keywords;

    @Schema(description = "ISSN", example = "1234-5678")
    private String issn;
}