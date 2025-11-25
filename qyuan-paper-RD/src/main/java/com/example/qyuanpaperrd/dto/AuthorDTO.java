package com.example.qyuanpaperrd.dto;

import lombok.Data;

/**
 * 作者数据传输对象
 */
@Data
public class AuthorDTO {

    /**
     * 作者ID（关联作者论文关系表的ID）
     */
    private Long id;

    /**
     * 论文ID
     */
    private Long paperId;

    /**
     * 作者姓
     */
    private String lastName;

    /**
     * 作者名
     */
    private String firstName;

    /**
     * 作者全名
     */
    private String fullName;

    /**
     * 作者次序
     */
    private Integer rank;

    /**
     * 作者ORCID
     */
    private String orcid;

    /**
     * 是否为通讯作者
     */
    private Boolean isCorresponding;
}