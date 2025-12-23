package org.example.qyuanuser.Result;

import lombok.Data;
import java.util.ArrayList;

@Data

public class RecordSepcInfoResult {
    private boolean success;
    private String message;
    private ArrayList<RecordSepcInfo> recordSepcInfoes;

    @Data
    public static class RecordSepcInfo { 
        private int record_id;
        private int folder_id;
        private int paper_id;
        private String paper_title;
        private int user_id;
    }

    public void setData(ArrayList<RecordSepcInfo> recordSepcInfoes) { 
        this.recordSepcInfoes = recordSepcInfoes;
    }
}
