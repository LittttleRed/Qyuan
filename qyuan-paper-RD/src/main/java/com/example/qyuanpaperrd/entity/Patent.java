package com.example.qyuanpaperrd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 专利实体类
 * 对应数据库表：patent
 * 注意：该表没有主键
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("patent")
public class Patent {

    /**
     * 专利申请号
     */
    @TableField("patent_number")
    private String patentNumber;

    /**
     * 专利名称
     */
    @TableField("patent_name")
    private String patentName;

    /**
     * 专利摘要
     */
    @TableField("abstract")
    private String abstractText;

    /**
     * 发明人
     */
    @TableField("inventor")
    private String inventor;

    /**
     * 专利权人
     */
    @TableField("assignee")
    private String assignee;

    /**
     * 国家
     */
    @TableField("country")
    private String country;

    /**
     * 申请日期
     */
    @TableField("application_date")
    private LocalDateTime applicationDate;

    /**
     * 授权日期
     */
    @TableField("authorization_date")
    private LocalDateTime authorizationDate;

    /**
     * 专利被引数
     */
    @TableField("citation_count")
    private Integer citationCount;
}
