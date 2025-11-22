package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 單元詳細資料傳輸物件
 * 用於單元頁面顯示完整的單元資訊
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitDto {

    /** 單元 UUID */
    private UUID id;

    /** 單元 ID（用於 URL） */
    private String unitId;

    /** 所屬課程代碼 */
    private String courseCode;

    /** 單元標題 */
    private String title;

    /** 單元類型 */
    private String type;

    /** 排序順序 */
    private Integer orderIndex;

    /** 影片 URL */
    private String videoUrl;

    /** 經驗值獎勵 */
    private Integer xpReward;

    /** 是否已完成 */
    private Boolean isCompleted;
}
