package com.insvnter.ai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的 IP 限流过滤器
 * - 登录: 10 次/分钟/IP
 * - 注册: 3 次/小时/IP
 * - 邮箱验证码: 5 次/小时/IP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate:";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 只拦截 POST 请求
        if ("POST".equalsIgnoreCase(method)) {
            RateRule rule = getRateRule(path);
            if (rule != null) {
                String ip = getClientIp(request);
                String key = RATE_LIMIT_PREFIX + rule.name + ":" + ip;

                Long count = redisTemplate.opsForValue().increment(key);
                if (count != null && count == 1) {
                    redisTemplate.expire(key, rule.windowSeconds, TimeUnit.SECONDS);
                }

                if (count != null && count > rule.maxRequests) {
                    log.warn("Rate limit [{}]: ip={}, count={}", rule.name, ip, count);
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            "{\"code\":429,\"message\":\"" + rule.message + "\",\"data\":null}");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private RateRule getRateRule(String path) {
        if (path.startsWith("/api/auth/login")) {
            return new RateRule("login", 10, 60, "登录请求过于频繁，请稍后再试");
        }
        if (path.startsWith("/api/auth/register")) {
            return new RateRule("register", 3, 3600, "注册次数已达上限，请1小时后再试");
        }
        if (path.startsWith("/api/email/send-code")) {
            return new RateRule("email", 5, 3600, "验证码发送过于频繁，请1小时后再试");
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private record RateRule(String name, int maxRequests, int windowSeconds, String message) {}
}
