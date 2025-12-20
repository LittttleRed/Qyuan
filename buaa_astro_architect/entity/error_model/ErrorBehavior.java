package org.buaa.buaa_astro_architect.entity.error_model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("error_behavior")
public class ErrorBehavior {
    @TableId(type = IdType.AUTO)
    public int id;

    @TableField("composite_error_behavior")
    public String compositeErrorBehavior;

    @TableField("component_error_behavior")
    public String componentErrorBehavior;

    @TableField("view_id")
    public String viewId;

    @TableField("view_type")
    public ViewType viewType;
}
