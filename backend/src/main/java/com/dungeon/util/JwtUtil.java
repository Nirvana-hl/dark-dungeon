package com.dungeon.util;

import com.dungeon.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * 生成 Token（支持String类型的userId）
     */
    public String generateToken(String userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成 Token（兼容Long类型，自动转换为String）
     */
    public String generateToken(Long userId, String username) {
        return generateToken(userId != null ? userId.toString() : null, username);
    }

    /**
     * 从 Token 中获取 Claims
     */
    public Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中获取用户ID（返回String类型）
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userId = claims.get("userId");
        if (userId == null) {
            return null;
        }
        // 兼容Long和String类型
        return userId.toString();
    }

    /**
     * 从 Token 中获取用户ID（返回Long类型，兼容旧代码）
     */
    public Long getUserIdFromTokenAsLong(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userId = claims.get("userId");
        if (userId == null) {
            return null;
        }
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId instanceof String) {
            try {
                return Long.parseLong((String) userId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            boolean isValid = expiration != null && !expiration.before(new Date());
            if (!isValid) {
                System.out.println("[JwtUtil] Token 已过期: expiration=" + expiration);
            }
            return isValid;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("[JwtUtil] Token 已过期: " + e.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.out.println("[JwtUtil] Token 格式错误: " + e.getMessage());
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            System.out.println("[JwtUtil] Token 签名验证失败: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("[JwtUtil] Token 验证异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }
}

