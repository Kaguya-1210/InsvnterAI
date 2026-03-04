package com.insvnter.ai.controller;

import com.insvnter.ai.model.dto.ApiResult;
import com.insvnter.ai.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send-code")
    public ApiResult<Void> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        emailService.sendVerificationCode(email);
        return ApiResult.ok("验证码已发送", null);
    }
}
