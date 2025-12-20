package org.example.qyuanuser.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWT {
    // 密钥 - 原始字符串（至少32位）
    private static final String SECRET = "qyuan_secret_qyuan_secret_qyuan_"; // 至少32位
    private static final String ISS = "qyuan_iss";
    // 过期时间 24小时
    private static final long EXPIRATION = 86400L;
    
    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param email 用户邮箱
     * @return JWT令牌
     */
    public static String generateJWT(Integer userId, String email) {
        // 创建签名密钥
        byte[] apiKeySecretBytes = Base64.getDecoder().decode(SECRET);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());
        
        // 设置载荷
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        
        // 设置过期时间
        Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION * 1000);
        
        // 生成JWT令牌
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuer(ISS)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact();
    }
    
    /**
     * 解析JWT令牌
     * @param token JWT令牌
     * @return Claims对象
     */
    public static Claims parseJWT(String token) {
        byte[] apiKeySecretBytes = Base64.getDecoder().decode(SECRET);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());
        
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