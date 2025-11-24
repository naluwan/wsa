package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 排行榜回應資料傳輸物件
 * 包含排行榜列表、當前使用者排名資訊和分頁資訊
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponseDto {

    /** 排行榜列表 */
    private List<LeaderboardEntryDto> leaderboard;

    /** 當前使用者的排名資訊（可能為 null，如果使用者未登入或無排名）*/
    private LeaderboardEntryDto currentUserEntry;

    /** 總人數（用於判斷是否還有更多資料） */
    private Long total;

    /** 是否有更多資料 */
    private Boolean hasMore;
}
