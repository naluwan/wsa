package com.wsa.repository;

import com.wsa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 使用者資料存取介面
 * 提供使用者資料的查詢與持久化操作
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * 根據 OAuth 提供者和外部 ID 查詢使用者
     * 用於判斷使用者是否已透過特定 OAuth 提供者註冊過
     *
     * @param provider OAuth 提供者（google 或 facebook）
     * @param externalId OAuth 提供者提供的使用者唯一 ID
     * @return 使用者資料（若存在）
     */
    Optional<User> findByProviderAndExternalId(String provider, String externalId);

    /**
     * 根據電子郵件查詢使用者
     *
     * @param email 使用者電子郵件
     * @return 使用者資料（若存在）
     */
    Optional<User> findByEmail(String email);
}
