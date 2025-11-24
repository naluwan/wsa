package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * 完成單元回應資料傳輸物件
 * 包含更新後的使用者資訊和單元完成狀態
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteUnitResponseDto {

    /** 更新後的使用者資訊 */
    private UserInfo user;

    /** 單元完成資訊 */
    private UnitInfo unit;

    /**
     * 使用者資訊子類別
     */
    @Getter
@Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        /** 使用者 UUID（轉為字串） */
        private String id;

        /** 使用者等級 */
        private Integer level;

        /** 總經驗值 */
        private Integer totalXp;

        /** 本週經驗值 */
        private Integer weeklyXp;
    }

    /**
     * 單元資訊子類別
     */
    @Getter
@Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitInfo {
        /** 單元 ID */
        private String unitId;

        /** 是否已完成 */
        private Boolean isCompleted;

        /** 獲得的經驗值 */
        private Integer xpEarned;
    }
}
