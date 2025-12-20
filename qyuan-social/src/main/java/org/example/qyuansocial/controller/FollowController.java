package org.example.qyuansocial.controller;

import org.example.qyuancommon.Result;
import org.example.qyuansocial.common.PageResponse;
import org.example.qyuansocial.dto.CreateFollowResponse;
import org.example.qyuansocial.entity.Follow;
import org.example.qyuansocial.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/social")
public class FollowController {

    @Autowired
    private FollowService followService;

    @GetMapping("/getListIFollow/{followerId}")
    public Result<PageResponse<Follow>> listIFollow(@PathVariable("followerId") Long followerId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        PageResponse<Follow> data = followService.listIFollow(followerId, page, size);
        return Result.ok(data, "查询成功");
    }

    @GetMapping("/getListFollowMe/{followedId}")
    public Result<PageResponse<Follow>> listFollowMe(@PathVariable("followedId") Long followedId,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        PageResponse<Follow> data = followService.listFollowMe(followedId, page, size);
        return Result.ok(data, "查询成功");
    }

    @PostMapping("/createFollow")
    public Result<CreateFollowResponse> newFollow(@RequestBody Follow follow) {
        // 关注功能：幂等操作，即使已存在也返回成功（像微博/抖音一样）
        Long followId = followService.createFollow(follow);
        CreateFollowResponse data = new CreateFollowResponse(followId);
        return Result.ok(data, "关注成功");
    }

    @DeleteMapping("/deleteFollow/{followId}")
    public Result<Map<String, Object>> cancelFollow(@PathVariable("followId") Long followId) {
        // 取消关注功能：幂等操作，即使不存在也返回成功（像微博/抖音一样）
        followService.cancelFollow(followId);
        // 按照接口文档：code 为 0 表示成功，msg 为提示信息，data 为对象（这里返回一个空对象 {}）
        Map<String, Object> data = new HashMap<>();
        return Result.ok(data, "取消关注成功");
    }

    /**
     * 查询当前用户与目标用户之间的关注关系
     */
    @GetMapping("/getFollowRelation")
    public Result<Map<String, Object>> getFollowRelation(@RequestParam("userId") Long userId,
                                                         @RequestParam("targetUserId") Long targetUserId) {
        boolean isFollowing = followService.isFollowing(userId, targetUserId);
        boolean isFollower = followService.isFollowing(targetUserId, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("isFollowing", isFollowing);  // userId 是否关注 targetUserId
        data.put("isFollower", isFollower);    // targetUserId 是否关注 userId
        data.put("isMutual", isFollowing && isFollower); // 是否互相关注
        return Result.ok(data, "success");
    }

    /**
     * 查询某个用户的关注数量
     */
    @GetMapping("/getFollowCount/{userId}")
    public Result<Map<String, Object>> getFollowCount(@PathVariable("userId") Long userId) {
        long count = followService.countFollowings(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("followCount", count);
        return Result.ok(data, "查询成功");
    }

    /**
     * 查询某个用户的粉丝数量
     */
    @GetMapping("/getFollowerCount/{userId}")
    public Result<Map<String, Object>> getFollowerCount(@PathVariable("userId") Long userId) {
        long count = followService.countFollowers(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("followerCount", count);
        return Result.ok(data, "查询成功");
    }

}
