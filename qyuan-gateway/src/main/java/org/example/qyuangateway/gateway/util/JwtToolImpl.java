package org.example.qyuangateway.gateway.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.qyuangateway.common.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtToolImpl implements JwtTool {

    // 这里使用一个示例密钥，实际项目中应该从配置文件读取
    //TODO:和user模块的key对应
    private final SecretKey secretKey = Keys.hmacShaKeyFor("123456789123456789123456789123456789".getBytes(StandardCharsets.UTF_8));

    @Override
    public String parseToken(String token) {
        // 空值检查
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Token cannot be null or empty");
        }

        // 格式检查
        if (!token.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid token format. Expected 'Bearer <token>'");
        }

        try {
            String jwtToken = token.substring(7).trim();

            // 检查token是否为空 after removing "Bearer "
            if (jwtToken.isEmpty()) {
                throw new UnauthorizedException("Token is empty after removing 'Bearer ' prefix");
            }

            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .setAllowedClockSkewSeconds(36000) // 允许10小时的时钟偏差（这个值有点大，通常30-60秒就够了）
                    .build()
                    .parseClaimsJws(jwtToken);

            Claims claims = claimsJws.getBody();

            // 直接获取user_id作为字符串
            String userId = claims.get("user_id", String.class);

            // 检查user_id是否为空
            if (userId == null || userId.isBlank()) {
                throw new UnauthorizedException("User ID not found in token");
            }

            return userId;

        }  catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Token is illegal", e);
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid token", e);
        }
    }
}
