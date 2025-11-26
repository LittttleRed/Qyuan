package org.example.qyuanorder.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/22 15:01
 */
@Data
@NoArgsConstructor
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer orderId;

    @TableField("user_id")
    private Integer userId;

    @TableField("pay_account")
    private BigDecimal payAccount;

   @TableField("buy_day")
   private Integer buyDay;

   @TableField("pay_status")
    private Integer payStatus;

   @TableField("pay_method")
    private String payMethod;

   @TableField(value = "pay_time", fill = FieldFill.UPDATE)
    private LocalDateTime payTime;

   @TableField(value = "create_at",fill = FieldFill.INSERT)
    private LocalDateTime createAt;
}
