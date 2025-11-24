package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 章節資料傳輸物件
 * 用於課程詳情中以 Accordion 方式顯示章節與單元清單
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionDto {

    /** 章節標題（例如：課程介紹 & 視聽(免費試看)） */
    private String sectionTitle;

    /** 該章節包含的單元列表 */
    private List<UnitSummaryDto> units;
}
