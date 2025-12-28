package org.example.qyuanuser.DTO.favorite;

import lombok.Data;

@Data
public class PaperDTO {
    private Integer paper_id;
    private Integer folder_id;
    private String paper_title;
    public boolean isFull() {
        return paper_id != null && folder_id != null;
    }
}
