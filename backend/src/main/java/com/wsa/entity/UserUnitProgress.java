package com.wsa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 使用者單元進度實體類別
 * 用途：
 *   1. 記錄使用者觀看單元的進度（最後觀看位置、最後觀看時間）
 *   2. 記錄使用者完成單元的狀態（完成時間）
 *
 * 重要：
 *   - 進度更新（觀看中）時，completedAt 為 null
 *   - 完成單元時，completedAt 才設為當前時間
 *   - 每個使用者對每個單元只能有一筆記錄（UNIQUE 約束）
 */
@Entity
@Table(
    name = "user_unit_progress",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "unit_id"})
    }
)
@Getter
@Setter
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

    /**
     * 使用者最後觀看到的影片秒數位置
     * 用途：讓使用者下次可以從此位置繼續觀看
     * 預設值：0（表示尚未觀看）
     */
    @Column(name = "last_position_seconds")
    private Integer lastPositionSeconds;

    /**
     * 使用者最後一次觀看此單元的時間
     * 用途：
     *   1. 追蹤使用者學習行為
     *   2. 分析使用者活躍度
     *   3. 提供「最近觀看」等功能
     */
    @Column(name = "last_watched_at")
    private LocalDateTime lastWatchedAt;

    /**
     * 完成時間
     * 重要：
     *   - 若為 null，表示單元尚未完成（可能正在觀看中）
     *   - 若不為 null，表示單元已完成（此時間用於避免重複獲得 XP）
     *   - 完成單元時，應手動設為 LocalDateTime.now()
     *
     * 注意：移除了 @CreationTimestamp 註解，因為進度更新時不應自動設值
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
