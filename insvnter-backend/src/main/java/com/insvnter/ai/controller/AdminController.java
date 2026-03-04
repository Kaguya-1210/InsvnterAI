package com.insvnter.ai.controller;

import com.insvnter.ai.model.dto.ApiResult;
import com.insvnter.ai.model.entity.User;
import com.insvnter.ai.repository.UserRepository;
import com.insvnter.ai.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:0}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${app.mail.name:}")
    private String mailFromName;

    // ==================== 仪表盘 ====================

    @GetMapping("/dashboard")
    public ApiResult<Map<String, Object>> dashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("todayNewUsers", userRepository.countByCreatedAtAfter(todayStart));
        stats.put("activeUsers", userRepository.countByEnabled(true));
        stats.put("disabledUsers", userRepository.countByEnabled(false));
        stats.put("adminCount", userRepository.countByRole(User.Role.ADMIN));
        stats.put("userCount", userRepository.countByRole(User.Role.USER));

        return ApiResult.ok(stats);
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public ApiResult<Page<Map<String, Object>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> users;
        User.Role roleFilter = parseRole(role);

        if (StringUtils.hasText(keyword) && roleFilter != null) {
            users = userRepository.searchByKeywordAndRole(keyword, roleFilter, pageable);
        } else if (StringUtils.hasText(keyword)) {
            users = userRepository.searchByKeyword(keyword, pageable);
        } else if (roleFilter != null) {
            users = userRepository.findByRole(roleFilter, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        Page<Map<String, Object>> result = users.map(this::toUserMap);
        return ApiResult.ok(result);
    }

    @PutMapping("/users/{id}/role")
    public ApiResult<Void> updateRole(@PathVariable Long id,
                                       @RequestBody Map<String, String> body,
                                       Authentication authentication) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 禁止修改自己的角色
        assertNotSelf(authentication, target, "不能修改自己的角色");

        String newRole = body.get("role");
        if (newRole == null) {
            throw new IllegalArgumentException("角色不能为空");
        }

        try {
            target.setRole(User.Role.valueOf(newRole.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效角色: " + newRole);
        }

        userRepository.save(target);
        return ApiResult.ok("角色已更新", null);
    }

    @PutMapping("/users/{id}/status")
    public ApiResult<Void> updateStatus(@PathVariable Long id,
                                         @RequestBody Map<String, Boolean> body,
                                         Authentication authentication) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 禁止禁用自己
        assertNotSelf(authentication, target, "不能禁用自己的账户");

        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            throw new IllegalArgumentException("状态不能为空");
        }

        target.setEnabled(enabled);
        userRepository.save(target);
        return ApiResult.ok(enabled ? "用户已启用" : "用户已禁用", null);
    }

    @PostMapping("/users/{id}/reset-password")
    public ApiResult<Map<String, String>> resetPassword(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        String tempPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        return ApiResult.ok("密码已重置", Map.of("tempPassword", tempPassword));
    }

    @DeleteMapping("/users/{id}")
    public ApiResult<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 禁止删除自己
        assertNotSelf(authentication, target, "不能删除自己的账户，请联系其他管理员");

        if (target.getRole() == User.Role.ADMIN) {
            long adminCount = userRepository.countByRole(User.Role.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalArgumentException("不能删除最后一个管理员");
            }
        }

        userRepository.deleteById(id);
        // 被删除用户的 JWT 可能仍然有效，加入黑名单
        jwtTokenProvider.blacklistByUsername(target.getUsername());

        return ApiResult.ok("用户已删除", null);
    }

    // ==================== 工具方法 ====================

    /** 检查操作目标是否为当前登录用户 */
    private void assertNotSelf(Authentication auth, User target, String message) {
        if (auth.getName().equals(target.getUsername())) {
            throw new IllegalArgumentException(message);
        }
    }

    private Map<String, Object> toUserMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("role", user.getRole().name());
        map.put("enabled", user.isEnabled());
        map.put("lastLoginAt", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);
        map.put("createdAt", user.getCreatedAt().toString());
        return map;
    }

    private User.Role parseRole(String role) {
        if (!StringUtils.hasText(role) || "ALL".equalsIgnoreCase(role)) return null;
        try {
            return User.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(12);
        var random = new java.security.SecureRandom();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ==================== 邮件配置 ====================

    @GetMapping("/email-config")
    public ApiResult<Map<String, Object>> emailConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("smtpHost", mailHost);
        config.put("smtpPort", mailPort);
        config.put("smtpUsername", maskEmail(mailUsername));
        config.put("fromAddress", mailFrom);
        config.put("fromName", mailFromName);
        config.put("configured", StringUtils.hasText(mailHost) && StringUtils.hasText(mailUsername));
        return ApiResult.ok(config);
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email)) return "";
        int at = email.indexOf('@');
        if (at <= 2) return "***" + email.substring(at);
        return email.charAt(0) + "***" + email.substring(at);
    }
}
