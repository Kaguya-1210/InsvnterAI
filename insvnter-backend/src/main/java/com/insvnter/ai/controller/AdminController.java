package com.insvnter.ai.controller;

import com.insvnter.ai.model.dto.ApiResult;
import com.insvnter.ai.model.entity.SystemConfig;
import com.insvnter.ai.model.entity.User;
import com.insvnter.ai.repository.SystemConfigRepository;
import com.insvnter.ai.repository.UserRepository;
import com.insvnter.ai.security.JwtTokenProvider;
import com.insvnter.ai.service.EmailService;
import lombok.RequiredArgsConstructor;
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
    private final SystemConfigRepository systemConfigRepository;
    private final EmailService emailService;

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
        assertNotSelf(authentication, target, "不能修改自己的角色");

        String newRole = body.get("role");
        if (newRole == null) throw new IllegalArgumentException("角色不能为空");
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
        assertNotSelf(authentication, target, "不能禁用自己的账户");

        Boolean enabled = body.get("enabled");
        if (enabled == null) throw new IllegalArgumentException("状态不能为空");
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
        assertNotSelf(authentication, target, "不能删除自己的账户，请联系其他管理员");

        if (target.getRole() == User.Role.ADMIN) {
            long adminCount = userRepository.countByRole(User.Role.ADMIN);
            if (adminCount <= 1) throw new IllegalArgumentException("不能删除最后一个管理员");
        }
        userRepository.deleteById(id);
        jwtTokenProvider.blacklistByUsername(target.getUsername());
        return ApiResult.ok("用户已删除", null);
    }

    // ==================== 邮件配置 ====================

    @GetMapping("/email-config")
    public ApiResult<Map<String, Object>> getEmailConfig() {
        Map<String, Object> result = new LinkedHashMap<>();
        var opt = systemConfigRepository.findByGroup("email");
        if (opt.isPresent()) {
            Map<String, String> v = opt.get().getValues();
            result.put("smtpHost", v.getOrDefault("smtpHost", ""));
            result.put("smtpPort", v.getOrDefault("smtpPort", "587"));
            result.put("smtpUsername", v.getOrDefault("smtpUsername", ""));
            result.put("smtpPassword", maskPassword(v.getOrDefault("smtpPassword", "")));
            result.put("fromAddress", v.getOrDefault("fromAddress", ""));
            result.put("fromName", v.getOrDefault("fromName", ""));
            result.put("activeTemplateId", v.getOrDefault("activeTemplateId", ""));
            result.put("encryption", v.getOrDefault("encryption", "starttls"));
            result.put("configured", true);
        } else {
            result.put("smtpHost", "");
            result.put("smtpPort", "587");
            result.put("smtpUsername", "");
            result.put("smtpPassword", "");
            result.put("fromAddress", "");
            result.put("fromName", "");
            result.put("activeTemplateId", "");
            result.put("encryption", "starttls");
            result.put("configured", false);
        }
        return ApiResult.ok(result);
    }

    @PutMapping("/email-config")
    public ApiResult<Void> saveEmailConfig(@RequestBody Map<String, String> body) {
        SystemConfig config = systemConfigRepository.findByGroup("email")
                .orElseGet(() -> {
                    SystemConfig c = new SystemConfig();
                    c.setGroup("email");
                    return c;
                });

        Map<String, String> values = new LinkedHashMap<>(body);
        if (config.getValues() != null && values.get("smtpPassword") != null
                && values.get("smtpPassword").contains("****")) {
            values.put("smtpPassword", config.getValues().getOrDefault("smtpPassword", ""));
        }

        config.setValues(values);
        config.setUpdatedAt(LocalDateTime.now());
        systemConfigRepository.save(config);

        return ApiResult.ok("邮件配置已保存", null);
    }

    @PostMapping("/email-config/test")
    public ApiResult<Void> testEmailConfig(@RequestBody Map<String, String> body) {
        String toEmail = body.get("email");
        if (!StringUtils.hasText(toEmail)) throw new IllegalArgumentException("请填写收件邮箱");
        emailService.sendTestEmail(toEmail);
        return ApiResult.ok("测试邮件已发送", null);
    }

    // ==================== 工具方法 ====================

    private void assertNotSelf(Authentication auth, User target, String message) {
        if (auth.getName().equals(target.getUsername())) throw new IllegalArgumentException(message);
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
        try { return User.Role.valueOf(role.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(12);
        var random = new java.security.SecureRandom();
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private String maskPassword(String pwd) {
        if (!StringUtils.hasText(pwd)) return "";
        return pwd.charAt(0) + "****" + pwd.charAt(pwd.length() - 1);
    }
}
