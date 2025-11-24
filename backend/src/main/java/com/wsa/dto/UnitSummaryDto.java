package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 單元摘要資料傳輸物件
 * 用於課程詳情中顯示單元列表的基本資訊
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitSummaryDto {

    /** 單元 UUID */
    private UUID id;

    /** 單元 ID（用於 URL） */
    private String unitId;

    /** 單元標題 */
    private String title;

    /** 單元類型 */
    private String type;

    /** 排序順序 */
    private Integer orderIndex;

    /** 章節標題 */
    private String sectionTitle;

    /** 在章節內的排序順序 */
    private Integer orderInSection;

    /** 是否為免費試看單元 */
    private Boolean isFreePreview;

    /** 使用者是否可以存取此單元（需根據登入狀態和擁有權計算） */
    private Boolean canAccess;

    /** 是否已完成（需根據使用者進度計算） */
    private Boolean isCompleted;
}
