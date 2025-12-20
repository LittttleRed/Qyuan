package org.example.qyuansocial.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@Data
@NoArgsConstructor
// 按接口文档中的顺序返回字段：records、total、size、current、pages
@JsonPropertyOrder({"records", "total", "size", "current", "pages"})
public class PageResponse<T> {
    private List<T> records;
    private long total;
    private int size;
    private int current;
    private long pages;

    public PageResponse(long total, int current, int size, List<T> records) {
        this.total = total;
        this.current = current;
        this.size = size;
        this.records = records;
        if (size <= 0) {
            this.pages = 0;
        } else {
            this.pages = (total + size - 1) / size;
        }
    }
}
