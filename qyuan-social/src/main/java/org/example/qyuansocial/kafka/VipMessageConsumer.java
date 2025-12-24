package org.example.qyuansocial.kafka;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.qyuansocial.entity.SystemMessage;
import org.example.qyuansocial.entity.UserMessageRelation;
import org.example.qyuansocial.service.MessageService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 学术成果平台系统消息的 Kafka 消费者
 * 示例：消费 topic：message-buy-vip-topic，JSON 中只包含 title 和 content
 */
@Slf4j
@Component
public class VipMessageConsumer {

    private final MessageService messageService;

    public VipMessageConsumer(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 监听学术成果平台的系统消息事件
     *
     * @param messageJson Kafka 中发送的 JSON 字符串，形如：
     *                    {"title":"xxx","content":"yyy"}
     */
    @KafkaListener(topics = "message-buy-vip-topic", groupId = "qyuan-social-group")
    public void onBuyVipMessage(String messageJson) {
        try {
            log.info("收到 message-buy-vip-topic 消息: {}", messageJson);
            BuyVipMessage buyVipMessage = JSON.parseObject(messageJson, BuyVipMessage.class);
            if (buyVipMessage == null) {
                log.warn("解析后的 BuyVipMessage 为空，忽略该消息");
                return;
            }

            // 根据 Kafka 中的 title / content 组装系统消息
            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setMessageTitle(buyVipMessage.getTitle());
            systemMessage.setMessageContent(buyVipMessage.getContent());
            // 仅写入系统消息，不直接绑定用户
            messageService.createMessageAndRelateUser(systemMessage,Long.valueOf(buyVipMessage.getUserId()));

        } catch (Exception e) {
            log.error("消费 message-buy-vip-topic 处理失败, message={}", messageJson, e);
            // 这里不抛异常，避免消息一直重试；如需重试可接入死信队列等机制
        }
    }

    @KafkaListener(topics = "message-claim-topic", groupId = "qyuan-social-group")
    public void onClaimMessage(String messageJson) {
        try {
            log.info("收到 message-claim-topic 消息: {}", messageJson);
            BuyVipMessage claimMessage = JSON.parseObject(messageJson, BuyVipMessage.class);
            if (claimMessage == null) {
                log.warn("解析后的认领通知消息为空，忽略该消息");
                return;
            }

            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setMessageTitle(claimMessage.getTitle());
            systemMessage.setMessageContent(claimMessage.getContent());
            messageService.createMessageAndRelateUser(systemMessage,Long.valueOf(claimMessage.getUserId()));
        } catch (Exception e) {
            log.error("消费 message-claim-topic 处理失败, message={}", messageJson, e);
        }
    }

    @KafkaListener(topics = "message-report-topic", groupId = "qyuan-social-group")
    public void onReportMessage(String messageJson) {
        try {
            log.info("收到 message-report-topic 消息: {}", messageJson);
            BuyVipMessage reportMessage = JSON.parseObject(messageJson, BuyVipMessage.class);
            if (reportMessage == null) {
                log.warn("解析后的举报通知消息为空，忽略该消息");
                return;
            }

            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setMessageTitle(reportMessage.getTitle());
            systemMessage.setMessageContent(reportMessage.getContent());
            messageService.createMessageAndRelateUser(systemMessage,Long.valueOf(reportMessage.getUserId()));
        } catch (Exception e) {
            log.error("消费 message-report-topic 处理失败, message={}", messageJson, e);
        }
    }

    @KafkaListener(topics = "message-vip-expired-topic", groupId = "qyuan-social-group")
    public void onVipExpiredMessage(String messageJson) {
        try {
            log.info("收到 message-vip-expired-topic 消息: {}", messageJson);
            BuyVipMessage expiredMessage = JSON.parseObject(messageJson, BuyVipMessage.class);
            if (expiredMessage == null) {
                log.warn("解析后的会员过期消息为空，忽略该消息");
                return;
            }

            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setMessageTitle(expiredMessage.getTitle());
            systemMessage.setMessageContent(expiredMessage.getContent());
            messageService.createMessageAndRelateUser(systemMessage,Long.valueOf(expiredMessage.getUserId()));
        } catch (Exception e) {
            log.error("消费 message-vip-expired-topic 处理失败, message={}", messageJson, e);
        }
    }
}


