package com.example.qyuanpaperrd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 专利认领信息实体类
 * 对应数据库表：patent_claim
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("patent_claim")
public class PatentClaim {

    /**
     * 认领ID（主键）
     */
    @TableId(value = "claim_id", type = IdType.AUTO)
    private Long claimId;

    /**
     * 认领用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 专利申请号
     */
    @TableField("patent_number")
    private String patentNumber;

    /**
     * 认领信息图片url
     */
    @TableField("claim_picture")
    private String claimPicture;

    /**
     * 认领说明
     */
    @TableField("claim_description")
    private String claimDescription;

    /**
     * 状态：0-未认领 1-待审核 2-驳回 3-通过
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 认领状态枚举
     */
    @Getter
    public enum ClaimStatus {
        UNCLAIMED(0, "未认领"),
        PENDING(1, "待审核"),
        REJECTED(2, "驳回"),
        APPROVED(3, "通过");

        private final Integer code;
        private final String description;

        ClaimStatus(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public static ClaimStatus fromCode(Integer code) {
            for (ClaimStatus status : ClaimStatus.values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown status code: " + code);
        }
    }
}
