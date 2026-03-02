package com.insvnter.ai.controller;

import com.insvnter.ai.model.dto.*;
import com.insvnter.ai.model.entity.User;
import com.insvnter.ai.service.AuthService;
import com.insvnter.ai.repository.UserRepository;
import com.insvnter.ai.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ApiResult<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ApiResult.ok("注册成功", response);
    }

    @PostMapping("/login")
    public ApiResult<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResult.ok("登录成功", response);
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            authService.logout(header.substring(7));
        }
        return ApiResult.ok("登出成功", null);
    }

    @GetMapping("/me")
    public ApiResult<Map<String, Object>> me(Authentication authentication) {
        String username = authentication.getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return ApiResult.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "createdAt", user.getCreatedAt().toString()
        ));
    }

    @PutMapping("/profile/username")
    public ApiResult<AuthResponse> updateUsername(@RequestBody Map<String, String> body,
                                                  Authentication authentication) {
        String currentUsername = authentication.getName();
        String newUsername = body.get("username");

        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (newUsername.length() < 2 || newUsername.length() > 20) {
            throw new IllegalArgumentException("用户名长度需在 2-20 之间");
        }

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (!currentUsername.equals(newUsername) && userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("用户名已被占用");
        }

        user.setUsername(newUsername);
        userRepository.save(user);

        // 重新签发 token（用户名变了，旧 token 里的 subject 过时）
        String newToken = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        return ApiResult.ok("用户名已更新", new AuthResponse(newToken, user.getUsername(), user.getEmail(), user.getRole().name()));
    }

    @PutMapping("/profile/password")
    public ApiResult<Void> updatePassword(@RequestBody Map<String, String> body,
                                          Authentication authentication) {
        String username = authentication.getName();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("新密码至少 6 位");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("当前密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ApiResult.ok("密码已更新", null);
    }
}
