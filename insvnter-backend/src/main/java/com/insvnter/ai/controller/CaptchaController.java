package com.insvnter.ai.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.insvnter.ai.model.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final DefaultKaptcha kaptchaProducer;
    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_TTL = 60; // 秒

    @GetMapping
    public ApiResult<Map<String, String>> getCaptcha() throws Exception {
        // 生成验证码文字
        String text = kaptchaProducer.createText();
        // 生成图片
        BufferedImage image = kaptchaProducer.createImage(text);

        // 转 Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

        // 存 Redis
        String captchaId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaId, text, CAPTCHA_TTL, TimeUnit.SECONDS);

        return ApiResult.ok(Map.of(
                "captchaId", captchaId,
                "image", "data:image/png;base64," + base64
        ));
    }
}
