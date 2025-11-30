package org.example.qyuanreadnotes.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/26 17:39
 */
@Data
@TableName("note")
public class Note {

    @TableId(type = IdType.AUTO)
    public int id;

    @TableField("user_id")
    public int userId;

    @TableField("paper_id")
    public int paperId;

    @TableField("note_title")
    public String noteTitle;

    @TableField("note_content")
    public String noteContent;

    @TableField("create_at")
    public LocalDateTime createAt;

}
