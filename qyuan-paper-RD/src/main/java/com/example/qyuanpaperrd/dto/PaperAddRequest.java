package com.example.qyuanpaperrd.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 新增论文请求DTO
 */
@Data
public class PaperAddRequest {

    /**
     * 论文标题
     */
    @NotBlank(message = "论文标题不能为空")
    @Size(max = 255, message = "论文标题长度不能超过255个字符")
    private String title;

    /**
     * 提交人
     */
    @NotBlank(message = "提交人不能为空")
    @Size(max = 50, message = "提交人长度不能超过50个字符")
    private String submitter;

    /**
     * 摘要
     */
    @Size(max = 5000, message = "摘要长度不能超过5000个字符")
    private String abstractText;

    /**
     * DOI
     */
    @Size(max = 100, message = "DOI长度不能超过100个字符")
    private String doi;

    /**
     * 期刊来源
     */
    @Size(max = 100, message = "期刊来源长度不能超过100个字符")
    private String journalSource;

    /**
     * PDF文件URL
     */
    @Size(max = 500, message = "PDF文件URL长度不能超过500个字符")
    private String pdfFileUrl;

    /**
     * 原链接
     */
    @NotBlank(message = "原链接不能为空")
    @Size(max = 500, message = "原链接长度不能超过500个字符")
    private String url;

    /**
     * 领域信息
     */
    private Long categoryId;

    /**
     * 作者列表
     */
    private List<AuthorRequest> authors;

    /**
     * 作者请求DTO
     */
    @Data
    public static class AuthorRequest {
        /**
         * 作者姓
         */
        @NotBlank(message = "作者姓不能为空")
        private String lastName;

        /**
         * 作者名
         */
        @NotBlank(message = "作者名不能为空")
        private String firstName;

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
        private Boolean isCorresponding = false;
    }
}