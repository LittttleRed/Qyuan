package org.example.qyuansocial.controller;

import org.example.qyuansocial.common.PageResponse;
import org.example.qyuansocial.entity.SystemMessage;
import org.example.qyuansocial.entity.UserMessageRelation;
import org.example.qyuansocial.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/social")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/creatMessage")
    public ResponseEntity<Void> createMessage(@RequestBody SystemMessage message) {
        // 这里演示创建“广播类”系统消息（发给所有用户）时的接口占位
        // 具体是否需要，可以根据业务再补充 userIds 等参数
        return ResponseEntity.ok().build();
    }

    @PostMapping("/relateUser/{messageId}")
    public ResponseEntity<Void> relateUser(@PathVariable Long messageId, @RequestBody UserMessageRelation relation) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getListUserMessage/{userId}")
    public PageResponse<SystemMessage> listUserMessages(@PathVariable Long userId,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return messageService.listUserMessages(userId, page, size);
    }

}
