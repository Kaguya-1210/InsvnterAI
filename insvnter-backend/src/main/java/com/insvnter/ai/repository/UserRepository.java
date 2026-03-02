package com.insvnter.ai.repository;

import com.insvnter.ai.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // 搜索用户（用户名或邮箱模糊匹配）
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 按角色筛选
    Page<User> findByRole(User.Role role, Pageable pageable);

    // 按角色 + 关键词筛选
    @Query("SELECT u FROM User u WHERE u.role = :role AND (u.username LIKE %:keyword% OR u.email LIKE %:keyword%)")
    Page<User> searchByKeywordAndRole(@Param("keyword") String keyword, @Param("role") User.Role role, Pageable pageable);

    // 统计
    long countByRole(User.Role role);

    long countByEnabled(boolean enabled);

    long countByCreatedAtAfter(LocalDateTime after);
}
