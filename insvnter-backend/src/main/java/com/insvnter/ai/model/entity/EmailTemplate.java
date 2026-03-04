package com.insvnter.ai.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 邮件模板（MongoDB）
 */
@Data
@Document(collection = "email_templates")
public class EmailTemplate {

    @Id
    private String id;

    /** 模板名称 */
    private String name;

    /** 模板描述 */
    private String description;

    /** HTML 内容（含 {{code}}, {{username}} 等变量占位符） */
    private String htmlContent;

    /** 是否内置模板（不可删除） */
    private boolean builtIn;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
