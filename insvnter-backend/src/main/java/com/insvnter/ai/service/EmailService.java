package com.insvnter.ai.service;

import com.insvnter.ai.model.entity.EmailTemplate;
import com.insvnter.ai.model.entity.SystemConfig;
import com.insvnter.ai.repository.EmailTemplateRepository;
import com.insvnter.ai.repository.SystemConfigRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final StringRedisTemplate redisTemplate;
    private final SystemConfigRepository systemConfigRepository;
    private final EmailTemplateRepository emailTemplateRepository;

    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final String EMAIL_COOLDOWN_PREFIX = "email:cooldown:";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_TTL_MINUTES = 5;
    private static final int COOLDOWN_SECONDS = 60;

    /**
     * 发送验证码 — 自动选用管理员配置的模板
     */
    public void sendVerificationCode(String email) {
        String cooldownKey = EMAIL_COOLDOWN_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new IllegalArgumentException("验证码发送过于频繁，请60秒后重试");
        }

        String code = generateCode();
        String codeKey = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);

        try {
            JavaMailSender sender = createMailSender();
            String[] from = getFromInfo();

            MimeMessage mimeMessage = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from[1] + " <" + from[0] + ">");
            helper.setTo(email);
            helper.setSubject("InsvnterAI 邮箱验证码");

            // 查找激活的模板
            EmailTemplate template = getActiveTemplate();
            if (template != null) {
                String html = template.getHtmlContent()
                        .replace("{{code}}", code)
                        .replace("{{minutes}}", String.valueOf(CODE_TTL_MINUTES));
                helper.setText(html, true);
            } else {
                helper.setText("您的验证码是: " + code + "\n\n"
                        + "验证码有效期为 " + CODE_TTL_MINUTES + " 分钟。\n"
                        + "如果这不是您的操作，请忽略此邮件。\n\n—— InsvnterAI");
            }

            sender.send(mimeMessage);
            log.info("Verification code sent to {}", email);
        } catch (IllegalArgumentException e) {
            redisTemplate.delete(codeKey);
            redisTemplate.delete(cooldownKey);
            throw e;
        } catch (Exception e) {
            redisTemplate.delete(codeKey);
            redisTemplate.delete(cooldownKey);
            log.error("Failed to send email to {}", email, e);
            throw new IllegalArgumentException("邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 发送测试邮件
     */
    public void sendTestEmail(String toEmail) {
        try {
            JavaMailSender sender = createMailSender();
            String[] from = getFromInfo();

            MimeMessage mimeMessage = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from[1] + " <" + from[0] + ">");
            helper.setTo(toEmail);
            helper.setSubject("InsvnterAI 邮件测试");
            helper.setText("<h2>✅ 邮件配置成功！</h2><p>SMTP 服务已正常工作。</p>", true);

            sender.send(mimeMessage);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("测试邮件发送失败: " + e.getMessage());
        }
    }

    public void validateEmailCode(String email, String code) {
        String key = EMAIL_CODE_PREFIX + email;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            throw new IllegalArgumentException("邮箱验证码已过期，请重新获取");
        }
        if (!stored.equals(code)) {
            redisTemplate.delete(key);
            throw new IllegalArgumentException("邮箱验证码错误");
        }
        redisTemplate.delete(key);
    }

    // ---- 内部方法 ----

    private EmailTemplate getActiveTemplate() {
        return systemConfigRepository.findByGroup("email")
                .map(config -> config.getValues().get("activeTemplateId"))
                .filter(StringUtils::hasText)
                .flatMap(emailTemplateRepository::findById)
                .orElse(null);
    }

    private JavaMailSender createMailSender() {
        SystemConfig config = systemConfigRepository.findByGroup("email")
                .orElseThrow(() -> new IllegalArgumentException("邮件服务未配置，请在管理后台设置 SMTP"));

        Map<String, String> v = config.getValues();
        String host = v.get("smtpHost");
        String username = v.get("smtpUsername");

        if (!StringUtils.hasText(host) || !StringUtils.hasText(username)) {
            throw new IllegalArgumentException("SMTP 配置不完整，请在管理后台补充");
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(Integer.parseInt(v.getOrDefault("smtpPort", "587")));
        sender.setUsername(username);
        sender.setPassword(v.get("smtpPassword"));
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");

        // 加密模式: ssl (465) / starttls (587) / none (25/22)
        String encryption = v.getOrDefault("encryption", "starttls");
        switch (encryption) {
            case "ssl":
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.ssl.trust", "*");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", String.valueOf(sender.getPort()));
                props.put("mail.smtp.socketFactory.fallback", "false");
                break;
            case "starttls":
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.trust", "*");
                break;
            // "none" — 不设置加密属性
        }

        return sender;
    }

    private String[] getFromInfo() {
        SystemConfig config = systemConfigRepository.findByGroup("email")
                .orElseThrow(() -> new IllegalArgumentException("邮件服务未配置"));
        Map<String, String> v = config.getValues();
        return new String[]{
                v.getOrDefault("fromAddress", v.get("smtpUsername")),
                v.getOrDefault("fromName", "InsvnterAI")
        };
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }
}
