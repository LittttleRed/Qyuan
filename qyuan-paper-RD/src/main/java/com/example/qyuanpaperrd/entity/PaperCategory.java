package com.example.qyuanpaperrd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 论文种类实体类
 * 对应数据库表：paper_category
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("paper_category")
public class PaperCategory {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类中文名
     */
    @TableField("category_name")
    private String categoryName;
}
