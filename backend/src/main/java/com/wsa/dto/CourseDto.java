package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 課程資料傳輸物件
 * 用於前後端之間傳遞課程資料
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {

    /** 課程 UUID */
    private UUID id;

    /** 課程代碼 */
    private String code;

    /** 課程名稱 */
    private String title;

    /** 課程描述 */
    private String description;

    /** 難度標籤 */
    private String levelTag;

    /** 單元總數 */
    private Integer totalUnits;

    /** 封面圖示 */
    private String coverIcon;

    /** 老師名稱 */
    private String teacherName;

    /** 課程價格（新台幣，單位：元） */
    private Integer priceTwd;

    /** 課程封面圖片 URL */
    private String thumbnailUrl;

    /** 課程是否已上架 */
    private Boolean isPublished;

    /** 使用者是否擁有此課程（需根據登入使用者計算） */
    private Boolean isOwned;

    /** 課程是否有免費試看單元 */
    private Boolean hasFreePreview;

    /** 單元列表（僅在取得課程詳情時包含） */
    private List<UnitSummaryDto> units;
}
