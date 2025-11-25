package com.example.qyuanpaperrd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 认领信息实体类
 * 对应数据库表：claim
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("claim")
public class Claim {

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
     * 论文ID
     */
    @TableField("paper_id")
    private Long paperId;

    /**
     * 认领信息图片url
     */
    @TableField("claim_picture")
    private String claimPicture;

    /**
     * 状态：0-未认领 1-待审核 2-驳回 3-通过
     */
    @TableField("status")
    private Integer status;

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
