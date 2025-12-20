package org.example.qyuansocial.service;

import org.example.qyuansocial.common.PageResponse;
import org.example.qyuansocial.entity.SystemMessage;
import org.example.qyuansocial.entity.UserMessageRelation;

public interface MessageService {

    /**
     * 仅创建一条系统消息（不绑定具体用户）
     * 典型场景：Kafka 消费到的平台广播/系统公告
     */
    void createMessage(SystemMessage message);

    /**
     * 创建系统消息并与单个用户建立关联
     */
    void createMessageAndRelateUser(SystemMessage message, Long userId);

    /**
     * 创建系统消息并与多个用户建立关联
     */
    void createMessageAndRelateUsers(SystemMessage message, java.util.List<Long> userIds);

    /**
     * 分页查询用户收到的系统消息列表
     */
    PageResponse<SystemMessage> listUserMessages(Long userId, int page, int size);

    /**
     * 将某条消息标记为已读
     */
    void markMessageRead(Long userId, Long messageId);
}
