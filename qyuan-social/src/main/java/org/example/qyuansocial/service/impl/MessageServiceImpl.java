package org.example.qyuansocial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.qyuansocial.common.PageResponse;
import org.example.qyuansocial.entity.SystemMessage;
import org.example.qyuansocial.entity.UserMessageRelation;
import org.example.qyuansocial.mapper.SystemMessageMapper;
import org.example.qyuansocial.mapper.UserMessageRelationMapper;
import org.example.qyuansocial.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统消息服务实现
 */
@Service
public class MessageServiceImpl extends ServiceImpl<SystemMessageMapper, SystemMessage> implements MessageService {

    private final UserMessageRelationMapper userMessageRelationMapper;

    public MessageServiceImpl(UserMessageRelationMapper userMessageRelationMapper) {
        this.userMessageRelationMapper = userMessageRelationMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMessage(SystemMessage message) {
        // 仅保存系统消息本身（不建立用户关系）
        message.setCreatedAt(LocalDateTime.now());
        this.save(message);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMessageAndRelateUser(SystemMessage message, Long userId) {
        message.setCreatedAt(LocalDateTime.now());
        // 保存系统消息
        this.save(message);

        // 建立用户与消息关系
        UserMessageRelation relation = new UserMessageRelation();
        relation.setUserId(userId);
        relation.setMessageId(message.getMessageId());
        relation.setIsRead(false);
        userMessageRelationMapper.insert(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMessageAndRelateUsers(SystemMessage message, List<Long> userIds) {
        message.setCreatedAt(LocalDateTime.now());
        this.save(message);

        for (Long userId : userIds) {
            UserMessageRelation relation = new UserMessageRelation();
            relation.setUserId(userId);
            relation.setMessageId(message.getMessageId());
            relation.setIsRead(false);
            userMessageRelationMapper.insert(relation);
        }
    }

    @Override
    public PageResponse<SystemMessage> listUserMessages(Long userId, int page, int size) {
        // 先分页查出该用户的消息关联关系
        Page<UserMessageRelation> pageParam = new Page<>(page, size);
        QueryWrapper<UserMessageRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("id");
        Page<UserMessageRelation> relationPage = userMessageRelationMapper.selectPage(pageParam, wrapper);

        if (relationPage.getRecords().isEmpty()) {
            return new PageResponse<>(0L, page, size, java.util.Collections.emptyList());
        }

        // 根据关联关系中的 messageId 列表查询系统消息
        List<Long> messageIds = relationPage.getRecords().stream()
                .map(UserMessageRelation::getMessageId)
                .toList();

        List<SystemMessage> messages = this.listByIds(messageIds);

        return new PageResponse<>(
                relationPage.getTotal(),
                (int) relationPage.getCurrent(),
                (int) relationPage.getSize(),
                messages
        );
    }

    @Override
    public void markMessageRead(Long userId, Long messageId) {
        QueryWrapper<UserMessageRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("message_id", messageId);
        UserMessageRelation relation = userMessageRelationMapper.selectOne(wrapper);
        if (relation != null && Boolean.FALSE.equals(relation.getIsRead())) {
            relation.setIsRead(true);
            userMessageRelationMapper.updateById(relation);
        }
    }
}


