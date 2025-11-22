package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排行榜項目資料傳輸物件
 * 用於顯示排行榜中的使用者資訊
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto {

    /** 排名（從 1 開始） */
    private Integer rank;

    /** 使用者 UUID（轉為字串） */
    private String userId;

    /** 使用者顯示名稱 */
    private String displayName;

    /** 使用者頭像 URL */
    private String avatarUrl;

    /** 使用者等級 */
    private Integer level;

    /** 總經驗值 */
    private Integer totalXp;

    /** 本週經驗值 */
    private Integer weeklyXp;
}
