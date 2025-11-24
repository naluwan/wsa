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
 * 使用者課程擁有權實體類別
 * 記錄使用者購買/擁有的課程，用於實現課程存取權限控制
 */
@Entity
@Table(
    name = "user_courses",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCourse {

    /** 擁有權記錄唯一識別碼（UUID 格式） */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** 使用者 ID（外鍵關聯 users 表） */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** 課程 ID（外鍵關聯 courses 表） */
    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    /** 購買時間 */
    @Column(name = "purchased_at", nullable = false)
    @Builder.Default
    private LocalDateTime purchasedAt = LocalDateTime.now();

    /** 建立時間（自動產生） */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間（自動更新） */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
