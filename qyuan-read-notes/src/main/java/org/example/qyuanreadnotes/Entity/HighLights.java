package org.example.qyuanreadnotes.Entity;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/26 17:17
 */
@Data
@NoArgsConstructor
@TableName("highlights")
public class HighLights {
    @TableId(type = IdType.AUTO)
    public int id;

    @TableField("user_id")
    public int userId;

    @TableField("paper_id")
    public int paperId;

    @TableField("page_index")
    public int pageIndex;

    @TableField("highlighted_text")
    public String highlightedText;

    @TableField("position_data")
    public String positionData;

    @TableField("color")
    public String color;

    @TableField("opacity")
    public BigDecimal opacity;

    @TableField("created_at")
    public String createdAt;
}
