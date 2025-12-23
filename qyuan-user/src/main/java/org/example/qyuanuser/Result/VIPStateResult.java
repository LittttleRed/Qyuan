package org.example.qyuanuser.Result;

import lombok.Data;

@Data
public class VIPStateResult {
    private boolean success;
    private String message;
    private VIPState vipState;

    @Data
    public class VIPState {
        private int permission_level;
        private String expire_time;
        private int use_times;
    }

    public void setData(int permission_level, String expire_time, int use_times){
        vipState = this.getVipState();
        vipState.setPermission_level(permission_level);
        vipState.setExpire_time(expire_time);
        vipState.setUse_times(use_times);
    }
}
