package org.example.qyuansocial.kafka;

import lombok.Data;

/**
 * 学术成果平台推送到 Kafka 的系统消息载体
 * 按你的设计：只包含 title 和 content 两个字段
 */
@Data
public class BuyVipMessage {

    /**
     * 系统消息标题
     */
    private String title;

    /**
     * 系统消息内容
     */
    private String content;

    private String userId;
}


