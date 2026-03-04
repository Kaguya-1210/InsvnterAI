package com.insvnter.ai.controller;

import com.insvnter.ai.model.dto.ApiResult;
import com.insvnter.ai.model.entity.EmailTemplate;
import com.insvnter.ai.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateRepository repo;

    @GetMapping
    public ApiResult<List<EmailTemplate>> list() {
        return ApiResult.ok(repo.findAllByOrderByCreatedAtAsc());
    }

    @GetMapping("/{id}")
    public ApiResult<EmailTemplate> get(@PathVariable String id) {
        EmailTemplate t = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("模板不存在"));
        return ApiResult.ok(t);
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable String id, @RequestBody Map<String, String> body) {
        EmailTemplate t = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("模板不存在"));

        if (body.containsKey("name")) t.setName(body.get("name"));
        if (body.containsKey("description")) t.setDescription(body.get("description"));
        if (body.containsKey("htmlContent")) t.setHtmlContent(body.get("htmlContent"));
        t.setUpdatedAt(LocalDateTime.now());
        repo.save(t);

        return ApiResult.ok("模板已更新", null);
    }

    @PostMapping
    public ApiResult<EmailTemplate> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("模板名称不能为空");
        }

        EmailTemplate t = new EmailTemplate();
        t.setName(name);
        t.setDescription(body.getOrDefault("description", ""));
        t.setHtmlContent(body.getOrDefault("htmlContent", "<p>在此编写邮件内容</p>"));
        t.setBuiltIn(false);
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        repo.save(t);

        return ApiResult.ok("模板已创建", t);
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable String id) {
        EmailTemplate t = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("模板不存在"));
        if (t.isBuiltIn()) {
            throw new IllegalArgumentException("内置模板不可删除");
        }
        repo.deleteById(id);
        return ApiResult.ok("模板已删除", null);
    }
}
