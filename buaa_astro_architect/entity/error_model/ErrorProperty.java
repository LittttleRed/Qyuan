package org.buaa.buaa_astro_architect.entity.error_model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("error_property")
public class ErrorProperty {
    @TableId(type = IdType.AUTO)
    public int id;

    @TableField("name")
    public String name;

    @TableField("probability_value")
    public BigDecimal probabilityValue;

    @TableField("distribution")
    public Distribution distribution;

    @TableField("view_id")
    public String viewId;

    @TableField("view_type")
    public ViewType viewType;

    @TableField("port_item_id")
    public String portItemId;
}


