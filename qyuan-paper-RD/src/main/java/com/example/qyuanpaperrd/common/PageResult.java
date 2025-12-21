package com.example.qyuanpaperrd.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分页响应结果类
 */
@Data
@Schema(description = "分页响应结果")
public class PageResult<T> {

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> records;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "100")
    private Long total;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Long current;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Long size;

    /**
     * 总页数
     */
    @Schema(description = "总页数", example = "5")
    private Long pages;

    public PageResult() {}

    public PageResult(List<T> records, Long total, Long current, Long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size; // 向上取整
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        return new PageResult<>(records, total, current, size);
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty(Long current, Long size) {
        return new PageResult<>(java.util.Collections.emptyList(), 0L, current, size);
    }
}
