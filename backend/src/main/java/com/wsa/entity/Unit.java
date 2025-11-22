package com.wsa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 單元實體類別
 * 儲存課程中的各個學習單元資訊，包含影片、測驗等不同類型的單元
 */
@Entity
@Table(name = "units")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Unit {

    /** 單元唯一識別碼（UUID 格式） */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** 對外顯示的單元 ID，用於 URL 路由 */
    @Column(name = "unit_id", nullable = false, unique = true, length = 100)
    private String unitId;

    /** 所屬課程的 UUID */
    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    /** 單元標題 */
    @Column(name = "title", nullable = false)
    private String title;

    /** 單元類型（例如：video, quiz, article。R1 階段固定為 video） */
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    /** 單元在課程中的排序順序 */
    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    /** 影片網址（若單元類型為 video） */
    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    /** 完成此單元後獲得的經驗值 */
    @Column(name = "xp_reward", nullable = false)
    @Builder.Default
    private Integer xpReward = 100;

    /** 章節標題（用於 Accordion 分組顯示） */
    @Column(name = "section_title", nullable = false)
    @Builder.Default
    private String sectionTitle = "未分類章節";

    /** 是否為免費試看單元 */
    @Column(name = "is_free_preview", nullable = false)
    @Builder.Default
    private Boolean isFreePreview = false;

    /** 在章節內的排序順序 */
    @Column(name = "order_in_section", nullable = false)
    @Builder.Default
    private Integer orderInSection = 0;

    /** 建立時間（自動產生） */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間（自動更新） */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
