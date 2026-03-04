package com.insvnter.ai.config;

import com.insvnter.ai.model.entity.EmailTemplate;
import com.insvnter.ai.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 初始化 4 套内置邮件模板
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTemplateInitializer implements CommandLineRunner {

    private final EmailTemplateRepository repo;

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        log.info("Initializing built-in email templates...");

        create("iOS 简约风",
                "苹果风格白底圆角卡片，蓝色验证码高亮",
                IOS_TEMPLATE);

        create("暗黑科技风",
                "深色渐变背景，霓虹色验证码",
                DARK_TEMPLATE);

        create("经典商务风",
                "品牌色头部横幅，正式排版",
                BUSINESS_TEMPLATE);

        create("极简文字风",
                "纯文字布局，细线分隔，无图片",
                MINIMAL_TEMPLATE);

        log.info("4 built-in email templates created.");
    }

    private void create(String name, String desc, String html) {
        EmailTemplate t = new EmailTemplate();
        t.setName(name);
        t.setDescription(desc);
        t.setHtmlContent(html);
        t.setBuiltIn(true);
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        repo.save(t);
    }

    // ============================================================
    // 模板 HTML
    // ============================================================

    private static final String IOS_TEMPLATE = """
<!DOCTYPE html>
<html><head><meta charset="utf-8"></head>
<body style="margin:0;padding:0;background:#f2f2f7;font-family:-apple-system,BlinkMacSystemFont,'SF Pro','Helvetica Neue',sans-serif;">
  <div style="max-width:420px;margin:40px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
    <div style="padding:32px 28px;text-align:center;">
      <div style="font-size:40px;margin-bottom:12px;">✦</div>
      <h1 style="font-size:20px;font-weight:600;color:#1d1d1f;margin:0 0 8px;">邮箱验证</h1>
      <p style="font-size:14px;color:#86868b;margin:0 0 28px;">请使用以下验证码完成验证</p>
      <div style="background:#f5f5f7;border-radius:12px;padding:20px;margin:0 0 24px;">
        <div style="font-size:36px;font-weight:700;letter-spacing:8px;color:#007aff;">{{code}}</div>
      </div>
      <p style="font-size:13px;color:#86868b;margin:0;">验证码将在 <strong>{{minutes}} 分钟</strong>后过期</p>
    </div>
    <div style="padding:16px 28px;background:#f9f9f9;text-align:center;">
      <p style="font-size:11px;color:#aeaeb2;margin:0;">如果这不是您的操作，请忽略此邮件。</p>
      <p style="font-size:11px;color:#aeaeb2;margin:4px 0 0;">—— InsvnterAI</p>
    </div>
  </div>
</body></html>
""";

    private static final String DARK_TEMPLATE = """
<!DOCTYPE html>
<html><head><meta charset="utf-8"></head>
<body style="margin:0;padding:0;background:#0a0a0f;font-family:'Inter','Segoe UI',sans-serif;">
  <div style="max-width:420px;margin:40px auto;background:linear-gradient(145deg,#1a1a2e,#16162a);border-radius:16px;border:1px solid rgba(99,102,241,0.2);overflow:hidden;">
    <div style="padding:32px 28px;text-align:center;">
      <div style="font-size:36px;margin-bottom:12px;background:linear-gradient(135deg,#6366f1,#a78bfa);-webkit-background-clip:text;-webkit-text-fill-color:transparent;">✦</div>
      <h1 style="font-size:20px;font-weight:700;color:#e4e4e7;margin:0 0 8px;">邮箱验证</h1>
      <p style="font-size:14px;color:#71717a;margin:0 0 28px;">请使用以下验证码完成验证</p>
      <div style="background:rgba(99,102,241,0.1);border:1px solid rgba(99,102,241,0.25);border-radius:12px;padding:20px;margin:0 0 24px;">
        <div style="font-size:36px;font-weight:700;letter-spacing:8px;background:linear-gradient(135deg,#818cf8,#c084fc);-webkit-background-clip:text;-webkit-text-fill-color:transparent;">{{code}}</div>
      </div>
      <p style="font-size:13px;color:#71717a;margin:0;">验证码将在 <span style="color:#a78bfa;">{{minutes}} 分钟</span>后过期</p>
    </div>
    <div style="padding:16px 28px;border-top:1px solid rgba(255,255,255,0.05);text-align:center;">
      <p style="font-size:11px;color:#52525b;margin:0;">如果这不是您的操作，请忽略此邮件。</p>
      <p style="font-size:11px;color:#52525b;margin:4px 0 0;">—— InsvnterAI</p>
    </div>
  </div>
</body></html>
""";

    private static final String BUSINESS_TEMPLATE = """
<!DOCTYPE html>
<html><head><meta charset="utf-8"></head>
<body style="margin:0;padding:0;background:#f4f4f5;font-family:'Segoe UI','Helvetica Neue',Arial,sans-serif;">
  <div style="max-width:480px;margin:40px auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,0.1);">
    <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:24px 28px;text-align:center;">
      <h1 style="font-size:22px;font-weight:700;color:#fff;margin:0;">InsvnterAI</h1>
    </div>
    <div style="padding:32px 28px;">
      <h2 style="font-size:18px;font-weight:600;color:#18181b;margin:0 0 12px;">邮箱验证码</h2>
      <p style="font-size:14px;color:#52525b;line-height:1.6;margin:0 0 24px;">您正在进行身份验证，请使用以下验证码：</p>
      <div style="text-align:center;background:#f9fafb;border:2px dashed #d4d4d8;border-radius:8px;padding:18px;margin:0 0 24px;">
        <div style="font-size:32px;font-weight:700;letter-spacing:6px;color:#6366f1;">{{code}}</div>
      </div>
      <p style="font-size:13px;color:#71717a;margin:0;">⏱ 验证码有效期为 <strong>{{minutes}} 分钟</strong>，请及时使用。</p>
    </div>
    <div style="padding:16px 28px;background:#fafafa;border-top:1px solid #f0f0f0;text-align:center;">
      <p style="font-size:11px;color:#a1a1aa;margin:0;">此邮件由系统自动发送，请勿直接回复。</p>
    </div>
  </div>
</body></html>
""";

    private static final String MINIMAL_TEMPLATE = """
<!DOCTYPE html>
<html><head><meta charset="utf-8"></head>
<body style="margin:0;padding:0;background:#fff;font-family:Georgia,'Times New Roman',serif;">
  <div style="max-width:440px;margin:40px auto;padding:32px;">
    <h2 style="font-size:18px;font-weight:normal;color:#111;margin:0 0 20px;padding-bottom:16px;border-bottom:1px solid #eee;">InsvnterAI · 邮箱验证</h2>
    <p style="font-size:15px;color:#333;line-height:1.8;margin:0 0 24px;">您好，您的验证码是：</p>
    <p style="font-size:28px;font-weight:bold;letter-spacing:6px;color:#111;margin:0 0 24px;padding:16px 0;text-align:center;">{{code}}</p>
    <p style="font-size:14px;color:#666;line-height:1.8;margin:0 0 24px;">验证码将在 {{minutes}} 分钟后过期。如非本人操作，请忽略此邮件。</p>
    <div style="border-top:1px solid #eee;padding-top:16px;">
      <p style="font-size:12px;color:#999;margin:0;">—— InsvnterAI</p>
    </div>
  </div>
</body></html>
""";
}
