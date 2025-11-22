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
 * 課程實體類別
 * 儲存平台上所有的課程資訊，包含課程代碼、名稱、描述、難度等級等資訊
 */
@Entity
@Table(name = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    /** 課程唯一識別碼（UUID 格式） */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** 課程代碼，用於路由與識別（例如：BACKEND_JAVA） */
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    /** 課程名稱 */
    @Column(name = "title", nullable = false)
    private String title;

    /** URL Slug（可選） */
    @Column(name = "slug")
    private String slug;

    /** 課程描述 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 難度標籤（例如：beginner, intermediate, advanced） */
    @Column(name = "level_tag", length = 50)
    private String levelTag;

    /** 單元總數 */
    @Column(name = "total_units", nullable = false)
    @Builder.Default
    private Integer totalUnits = 0;

    /** 前端顯示用的封面圖示名稱或 URL */
    @Column(name = "cover_icon", length = 100)
    private String coverIcon;

    /** 老師名稱 */
    @Column(name = "teacher_name", nullable = false)
    private String teacherName;

    /** 課程價格（新台幣，單位：元） */
    @Column(name = "price_twd", nullable = false)
    @Builder.Default
    private Integer priceTwd = 0;

    /** 課程封面圖片 URL */
    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    /** 課程是否已上架 */
    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = true;

    /** 建立時間（自動產生） */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間（自動更新） */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
