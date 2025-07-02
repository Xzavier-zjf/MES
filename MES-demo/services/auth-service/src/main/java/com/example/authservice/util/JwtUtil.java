package com.example.authservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;


public class JwtUtil {

    // 你的 secret key，长度必须足够（HMAC-SHA256 最少 256 bits = 32 chars）
    private static final String SECRET_KEY = "ymy-jwt-secret-key-my-jwt-secret-key"; // 必须 ≥32 位

    // Token 有效期：24 小时（毫秒）
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    // 生成 JWT
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)                          // 主题：用户名
                .setIssuedAt(new Date())                      // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 签名算法和密钥
                .compact();
    }

    // 校验 token 是否有效
    public static boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false; // 包括过期、伪造等
        }
    }

    // 从 token 中获取用户名
    public static String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 获取签名密钥
    private static Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
