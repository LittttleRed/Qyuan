package com.example.qyuanpaperrd.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTTP请求头工具类
 */
public class HeaderUtil {

    private static final String USER_ID_HEADER = "USER-ID";

    /**
     * 从请求头获取用户ID
     * @param request HTTP请求
     * @return 用户ID
     * @throws IllegalArgumentException 如果请求头中不存在USER-ID
     */
    public static Long getUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("请求头中缺少USER-ID参数");
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("USER-ID参数格式错误，必须是数字");
        }
    }

    /**
     * 从请求头获取用户ID（可选）
     * @param request HTTP请求
     * @return 用户ID，如果不存在返回null
     */
    public static Long getUserIdOptional(HttpServletRequest request) {
        try {
            return getUserId(request);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}