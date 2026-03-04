package com.insvnter.ai.controller;

import com.insvnter.ai.model.dto.ApiResult;
import com.insvnter.ai.repository.UserRepository;
import com.insvnter.ai.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @PostMapping("/send-code")
    public ApiResult<Void> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        // 基本格式校验
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        // 安全：检查邮箱是否已被注册（注册场景下不应重复）
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }
        emailService.sendVerificationCode(email);
        return ApiResult.ok("验证码已发送", null);
    }
}
