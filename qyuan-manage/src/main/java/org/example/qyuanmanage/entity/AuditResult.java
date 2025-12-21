package org.example.qyuanmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_result")
public class AuditResult {

    @TableId(type = IdType.AUTO)
    private Long auditId;

    private Long adminId;

     /**
     * 审核类型
     * 1 - 举报
     * 2 - 认领
     */
    private Integer auditType;

    private Long targetId;

    private Integer auditResult;

    private String auditOpinion;

    private LocalDateTime auditTime;
}
