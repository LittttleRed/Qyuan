package org.example.qyuanuser.Result;

import java.util.ArrayList;
import lombok.Data;

@Data
public class FolderSpecInfoResult {
    private boolean success;
    private String message;
    private ArrayList<FolderSpecInfo> folderSpecInfoes;

    @Data
    public static class FolderSpecInfo { 
        private Integer folder_id;
        private Integer user_id;
        private String folder_name;
        private Integer is_public;
        private Integer paper_count;
    }

    public void setData(ArrayList<FolderSpecInfo> folderSpecInfoes) { 
        this.folderSpecInfoes = folderSpecInfoes;
    }
}