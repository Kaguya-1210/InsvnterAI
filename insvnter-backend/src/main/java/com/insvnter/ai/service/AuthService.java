package com.insvnter.ai.service;

import com.insvnter.ai.model.dto.AuthResponse;
import com.insvnter.ai.model.dto.LoginRequest;
import com.insvnter.ai.model.dto.RegisterRequest;
import com.insvnter.ai.model.entity.User;
import com.insvnter.ai.repository.UserRepository;
import com.insvnter.ai.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:";

    public AuthResponse register(RegisterRequest request) {
        // 校验验证码
        validateCaptcha(request.getCaptchaId(), request.getCaptcha());

        // 检查重复
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        userRepository.save(user);

        // 生成 token
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        // 校验验证码
        validateCaptcha(request.getCaptchaId(), request.getCaptcha());

        // 校验用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 检查账户状态
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("账户已被禁用，请联系管理员");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole().name());
    }

    public void logout(String token) {
        jwtTokenProvider.blacklistToken(token);
    }

    private void validateCaptcha(String captchaId, String captchaCode) {
        String key = CAPTCHA_PREFIX + captchaId;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            throw new IllegalArgumentException("验证码已过期");
        }
        if (!stored.equalsIgnoreCase(captchaCode)) {
            throw new IllegalArgumentException("验证码错误");
        }
        // 验证后删除，防止重复使用
        redisTemplate.delete(key);
    }
}
