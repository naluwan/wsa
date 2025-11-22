package com.wsa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 使用者單元完成進度實體類別
 * 記錄使用者完成了哪些單元，避免重複獲得經驗值
 */
@Entity
@Table(
    name = "user_unit_progress",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "unit_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUnitProgress {

    /** 進度記錄唯一識別碼（UUID 格式） */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** 使用者 UUID */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** 單元 UUID */
    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    /** 完成時間（自動產生） */
    @CreationTimestamp
    @Column(name = "completed_at", nullable = false, updatable = false)
    private LocalDateTime completedAt;
}
