package com.example.qyuanpaperrd.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 论文认领请求DTO
 */
@Data
public class ClaimRequest {

    /**
     * 论文ID
     */
    @NotNull(message = "论文ID不能为空")
    private Long paperId;

    /**
     * 认领信息图片URL
     */
    private String claimPicture;

    /**
     * 认领说明
     */
    private String claimDescription;
}