package org.example.qyuansocial.controller;

import com.alibaba.fastjson2.JSONObject;
import org.example.qyuancommon.Result;
import org.example.qyuansocial.common.PageResponse;
import org.example.qyuansocial.dto.CreateCommentResponse;
import org.example.qyuansocial.entity.Comment;
import org.example.qyuansocial.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/social")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/createComment")
    public Result<CreateCommentResponse> createComment(@RequestBody JSONObject data) {
        Comment comment = new Comment();
        comment.setUserId(data.getLong("user_id"));
        comment.setUserName(data.getString("user_name"));
        comment.setUserAvatar(data.getString("user_avatar"));
        comment.setAchievementId(data.getLong("achievement_id"));
        comment.setAchievementType(data.getString("achievement_type"));
        comment.setParentId(data.getLong("parent_comment_id"));
        comment.setContent(data.getString("content"));
        Long commentId = commentService.createComment(comment);
        CreateCommentResponse result = new CreateCommentResponse(commentId);
        return Result.ok(result, "评论成功");
    }

    @DeleteMapping("/deleteComment/{commentId}")
    public Result<Map<String, Object>> deleteComment(@PathVariable("commentId") Long commentId) {
        // 幂等：不存在也返回成功
        commentService.deleteComment(commentId);
        Map<String, Object> data = new HashMap<>();
        return Result.ok(data, "删除成功");
    }

    @GetMapping("/getReplies/{commentId}")
    public Result<PageResponse<Comment>> getReplies(@PathVariable("commentId") Long commentId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        PageResponse<Comment> data = commentService.listReplies(commentId, page, size);
        return Result.ok(data, "查询成功");
    }

    @GetMapping("/by-user/{userId}")
    public Result<PageResponse<Comment>> listByUser(@PathVariable("userId") Long userId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        PageResponse<Comment> data = commentService.listByUser(userId, page, size);
        return Result.ok(data, "查询成功");
    }

    @GetMapping("/by-achievement/{achievementId}/{achievementType}")
    public Result<PageResponse<Comment>> listByAchievement(@PathVariable("achievementId") Long achievementId,
                                                           @PathVariable("achievementType") String achievementType,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        PageResponse<Comment> data = commentService.listByAchievement(achievementId,achievementType, page, size);
        return Result.ok(data, "查询成功");
    }

    @PostMapping("/createLike/{commentId}")
    public Result<Map<String, Object>> likeComment(@PathVariable("commentId") Long commentId,
                                                   @RequestHeader("USER-ID") Long userId) {
        boolean success = commentService.likeComment(commentId, userId);
        Map<String, Object> data = new HashMap<>();
        if (success) {
            return Result.ok(data, "点赞成功");
        } else {
            // 不存在时返回成功以保持幂等（也可改为错误提示）
            return Result.ok(data, "评论不存在，已忽略");
        }
    }

    @DeleteMapping("/deleteLike/{commentId}")
    public Result<Map<String, Object>> unlikeComment(@PathVariable("commentId") Long commentId,
                                                     @RequestHeader("USER-ID") Long userId) {
        boolean success = commentService.unlikeComment(commentId, userId);
        Map<String, Object> data = new HashMap<>();
        if (success) {
            return Result.ok(data, "取消点赞成功");
        } else {
            // 不存在同样幂等
            return Result.ok(data, "评论不存在，已忽略");
        }
    }
}

