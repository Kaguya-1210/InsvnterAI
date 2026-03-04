package com.insvnter.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.mail.from:noreply@insvnter.ai}")
    private String fromAddress;

    @Value("${app.mail.name:InsvnterAI}")
    private String fromName;

    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final String EMAIL_COOLDOWN_PREFIX = "email:cooldown:";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_TTL_MINUTES = 5;
    private static final int COOLDOWN_SECONDS = 60;

    /**
     * 发送邮箱验证码
     */
    public void sendVerificationCode(String email) {
        // 检查冷却时间（60秒内不允许重发）
        String cooldownKey = EMAIL_COOLDOWN_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new IllegalArgumentException("验证码发送过于频繁，请60秒后重试");
        }

        // 生成6位验证码
        String code = generateCode();

        // 存入Redis（5分钟TTL）
        String codeKey = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);

        // 设置冷却标记
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);

        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromAddress + ">");
            message.setTo(email);
            message.setSubject("InsvnterAI 邮箱验证码");
            message.setText("您的验证码是: " + code + "\n\n"
                    + "验证码有效期为 " + CODE_TTL_MINUTES + " 分钟，请尽快使用。\n"
                    + "如果这不是您的操作，请忽略此邮件。\n\n"
                    + "—— InsvnterAI");
            mailSender.send(message);
            log.info("Verification code sent to {}", email);
        } catch (Exception e) {
            // 发送失败，清除code和冷却
            redisTemplate.delete(codeKey);
            redisTemplate.delete(cooldownKey);
            log.error("Failed to send email to {}", email, e);
            throw new IllegalArgumentException("邮件发送失败，请检查邮箱地址或稍后重试");
        }
    }

    /**
     * 验证邮箱验证码
     */
    public void validateEmailCode(String email, String code) {
        String key = EMAIL_CODE_PREFIX + email;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            throw new IllegalArgumentException("邮箱验证码已过期，请重新获取");
        }
        if (!stored.equals(code)) {
            // 验证失败，立即删除验证码（防暴力破解）
            redisTemplate.delete(key);
            throw new IllegalArgumentException("邮箱验证码错误");
        }
        // 验证成功，删除
        redisTemplate.delete(key);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
