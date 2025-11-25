package com.example.qyuanpaperrd.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 论文数据传输对象
 */
@Data
public class PaperDTO {

    /**
     * 论文ID
     */
    private Long paperId;

    /**
     * 论文标题
     */
    private String title;

    /**
     * 提交人
     */
    private String submitter;

    /**
     * 摘要
     */
    private String abstractText;

    /**
     * DOI
     */
    private String doi;

    /**
     * 期刊来源
     */
    private String journalSource;

    /**
     * PDF文件URL
     */
    private String pdfFileUrl;

    /**
     * 原链接
     */
    private String url;

    /**
     * 领域信息
     */
    private Long categoryId;

    /**
     * 创建时间
     */
    private LocalDateTime created;

    /**
     * 更新时间
     */
    private LocalDateTime updated;

    /**
     * 作者列表
     */
    private List<AuthorDTO> authors;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 是否已被当前用户收藏
     */
    private Boolean isFavorited;

    /**
     * 是否已被当前用户认领
     */
    private Boolean isClaimed;
}