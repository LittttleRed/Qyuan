package org.example.qyuanuser.DTO.favorite;

import lombok.Data;

@Data
public class PaperDTO {
    private Integer paper_id;
    private Integer folder_id;

    public boolean isFull() {
        return paper_id != null && folder_id != null;
    }
}
