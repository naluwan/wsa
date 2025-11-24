package com.wsa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 使用者實體類別
 * 儲存透過 OAuth 登入的使用者資料，包含基本資料、等級與經驗值等資訊
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** 使用者唯一識別碼（UUID 格式） */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** OAuth 提供者的使用者 ID（Google ID 或 Facebook ID） */
    @Column(name = "external_id", nullable = false)
    private String externalId;

    /** OAuth 提供者類型（google 或 facebook） */
    @Column(name = "provider", nullable = false)
    private String provider;

    /** 使用者顯示名稱 */
    @Column(name = "display_name", nullable = false)
    private String displayName;

    /** 使用者電子郵件 */
    @Column(name = "email", nullable = false)
    private String email;

    /** 使用者頭像 URL */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /** 使用者等級（預設為 1） */
    @Column(name = "level", nullable = false)
    @Builder.Default
    private Integer level = 1;

    /** 使用者總經驗值（預設為 0） */
    @Column(name = "total_xp", nullable = false)
    @Builder.Default
    private Integer totalXp = 0;

    /** 使用者本週經驗值（預設為 0） */
    @Column(name = "weekly_xp", nullable = false)
    @Builder.Default
    private Integer weeklyXp = 0;

    /** 建立時間（自動產生） */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間（自動更新） */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
