package org.example.qyuanmanage.entity;

import lombok.Data;

/**
 * @description:
 * @author: 29177
 * @time: 2025/12/26 22:45
 */
@Data
public class VIPState {
    private int permission_level;
    private String expire_time;
    private int use_times;
}