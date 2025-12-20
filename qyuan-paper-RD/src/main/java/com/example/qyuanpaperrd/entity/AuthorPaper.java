package com.example.qyuanpaperrd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 作者-论文关系实体类
 * 对应数据库表：author_paper
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("author_paper")
public class AuthorPaper {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 论文ID
     */
    @TableField("paper_id")
    private Long paperId;

    /**
     * 作者姓
     */
    @TableField("author_last_name")
    private String authorLastName;

    /**
     * 作者名
     */
    @TableField("author_first_name")
    private String authorFirstName;

    /**
     * 作者次序(一作,二作)
     */
    @TableField("author_rank")
    private Integer authorRank;

    /**
     * 作者ORCID
     */
    @TableField("author_orcid")
    private String authorOrcid;
}
