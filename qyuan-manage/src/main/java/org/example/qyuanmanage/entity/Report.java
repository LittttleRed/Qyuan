package org.example.qyuanmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long reportId;

    private Long userId;

     /**
     * 举报对象类型
     * 1 - 论文
     * 2 - 用户
     * 3 - 评论
     */
    private Integer targetType;

    private Long targetId;

    private String reportReason;

    private String reportUrl;

    private LocalDateTime submitTime;

     /**
     * 举报状态
     * 0 - 待审核
     * 1 - 驳回
     * 2 - 成立
     */
    private Integer status;
}
