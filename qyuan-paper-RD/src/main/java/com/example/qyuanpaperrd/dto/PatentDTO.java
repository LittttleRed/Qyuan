package com.example.qyuanpaperrd.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 专利数据传输对象
 */
@Data
@Accessors(chain = true)
public class PatentDTO {


    /**
     * 专利ID
     */
    private Integer patentId;
    /**
     * 专利申请号
     */
    private String patentNumber;

    /**
     * 专利名称
     */
    private String patentName;

    /**
     * 专利摘要
     */
    private String abstractText;

    /**
     * 发明人
     */
    private String inventor;

    /**
     * 专利权人
     */
    private String assignee;

    /**
     * 国家
     */
    private String country;

    /**
     * 申请日期
     */
    private LocalDateTime applicationDate;

    /**
     * 授权日期
     */
    private LocalDateTime authorizationDate;

    /**
     * 专利被引数
     */
    private Integer citationCount;
}