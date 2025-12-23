package org.example.qyuanuser.DTO.favorite;

import lombok.Data;

@Data
public class FolderDTO {
    private String folder_name;
    private Integer is_public;

    public boolean isFull() {
        return folder_name != null && is_public != null;
    }
}
