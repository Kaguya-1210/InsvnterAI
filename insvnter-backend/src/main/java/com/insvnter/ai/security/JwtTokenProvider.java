package com.insvnter.ai.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;
    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            StringRedisTemplate redisTemplate) {
        // SHA-256 ensures key is always 256 bits regardless of secret length
        byte[] keyBytes;
        try {
            keyBytes = java.security.MessageDigest.getInstance("SHA-256").digest(secret.getBytes());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        this.redisTemplate = redisTemplate;
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            if (isBlacklisted(token)) return false;
            // 检查用户级黑名单（用户被删除或禁用时生效）
            String username = claims.getSubject();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(USER_BLACKLIST_PREFIX + username))) return false;
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = parseClaims(token);
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + token, "1", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (JwtException ignored) {
        }
    }

    /** 按用户名拉黑，所有该用户的 JWT 立即失效 */
    public void blacklistByUsername(String username) {
        // 黑名单有效期 = JWT 最大有效期，确保所有已签发的 token 都过期
        redisTemplate.opsForValue().set(
                USER_BLACKLIST_PREFIX + username, "1", expirationMs, TimeUnit.MILLISECONDS);
    }

    private boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
