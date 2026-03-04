package com.insvnter.ai.repository;

import com.insvnter.ai.model.entity.EmailTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EmailTemplateRepository extends MongoRepository<EmailTemplate, String> {
    boolean existsByName(String name);
    List<EmailTemplate> findAllByOrderByCreatedAtAsc();
}
