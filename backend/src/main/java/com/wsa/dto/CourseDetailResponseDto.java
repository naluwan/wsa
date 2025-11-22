package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 課程詳情回應資料傳輸物件
 * 用於課程詳情頁面，包含課程資訊與章節化的單元清單
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailResponseDto {

    /** 課程基本資訊 */
    private CourseDto course;

    /** 章節列表（包含各章節的單元） */
    private List<SectionDto> sections;
}
