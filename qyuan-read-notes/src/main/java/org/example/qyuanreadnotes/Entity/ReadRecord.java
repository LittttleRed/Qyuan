package org.example.qyuanreadnotes.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/19 20:58
 */
@Data
@TableName("read_record")
public class ReadRecord {
    @TableId(type = IdType.AUTO)
    public int id;

    @TableField("user_id")
    public int userId;

    @TableField("paper_id")
    public int paperId;

    @TableField("page_number")
    public int pageNumber;

    @TableField("scroll_position")
    public BigDecimal scrollPosition;

    @TableField("updated_at")
    public LocalDateTime updatedAt;

    @TableField("paper_title")
    public String paperTitle;

    @TableField("first_author")
    public String firstAuthor;

    @TableField("abstract")
    public String abstractText;
}
