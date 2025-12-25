package org.example.qyuanuser.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWT {
    // 密钥 - 原始字符串（至少32位）
    private static final String SECRET = "123456789123456789123456789123456789"; // 至少32位
    private static final String ISS = "qyuanIss";
    // 过期时间 24小时
    private static final long EXPIRATION = 86400L;
    
    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param email 用户邮箱
     * @return JWT令牌
     */
    public static String generateJWT(Integer userId, String email) {
        // 直接使用字符串密钥，不要Base64解码
        Key signingKey = new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName()
        );

        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);  // 改为user_id，与gateway匹配
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuer(ISS)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION * 1000))
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact();
    }
    
    /**
     * 解析JWT令牌
     * @param token JWT令牌
     * @return Claims对象
     */
    public static Claims parseJWT(String token) {
        Key signingKey = new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName()
        );

        return Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 验证JWT令牌是否有效
     * @param token JWT令牌
     * @return 是否有效
     */
    public static boolean validateJWT(String token) {
        try {
            parseJWT(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从JWT令牌中获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public static Integer getUserIdFromJWT(String token) {
        try {
            Claims claims = parseJWT(token);
            return claims.get("userId", Integer.class);
        } catch (Exception e) {
            return null;
        }
    }
}