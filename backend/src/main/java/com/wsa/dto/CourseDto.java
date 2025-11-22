package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 課程資料傳輸物件
 * 用於前後端之間傳遞課程資料
 */
@Data
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

    /** 單元列表（僅在取得課程詳情時包含） */
    private List<UnitSummaryDto> units;
}
