package com.insvnter.ai.repository;

import com.insvnter.ai.model.entity.SystemConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SystemConfigRepository extends MongoRepository<SystemConfig, String> {
    Optional<SystemConfig> findByGroup(String group);
}
