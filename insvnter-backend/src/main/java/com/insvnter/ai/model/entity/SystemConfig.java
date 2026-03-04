package com.insvnter.ai.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统配置（MongoDB）— 存储 SMTP 等可变配置
 */
@Data
@Document(collection = "system_config")
public class SystemConfig {

    @Id
    private String id;

    /** 配置分组, 例如 "email" */
    private String group;

    /** 配置项键值对 */
    private Map<String, String> values;

    private LocalDateTime updatedAt;
}
