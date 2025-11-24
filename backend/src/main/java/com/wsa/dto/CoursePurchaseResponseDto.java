package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 課程購買回應資料傳輸物件
 * 用於 Mock 購買 API 的回應，包含購買後的課程狀態
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePurchaseResponseDto {

    /** 購買成功的課程資訊 */
    private PurchasedCourseInfo course;

    /**
     * 購買的課程資訊內嵌類別
     */
    @Getter
@Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchasedCourseInfo {

        /** 課程代碼 */
        private String courseCode;

        /** 課程名稱 */
        private String title;

        /** 是否已擁有（購買後應為 true） */
        private Boolean isOwned;
    }
}
