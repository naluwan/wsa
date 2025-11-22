package com.wsa.controller;

import com.wsa.dto.LeaderboardEntryDto;
import com.wsa.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排行榜相關 API Controller
 * 處理排行榜查詢相關的 HTTP 請求
 */
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    /**
     * 取得總經驗值排行榜
     * GET /api/leaderboard/total
     *
     * @param limit 回傳人數上限（預設 50）
     * @param authentication Spring Security 認證物件
     * @return 總經驗值排行榜
     */
    @GetMapping("/total")
    public ResponseEntity<List<LeaderboardEntryDto>> getTotalXpLeaderboard(
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication) {

        // 檢查是否已認證
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        List<LeaderboardEntryDto> leaderboard = leaderboardService.getTotalXpLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * 取得本週經驗值排行榜
     * GET /api/leaderboard/weekly
     *
     * @param limit 回傳人數上限（預設 50）
     * @param authentication Spring Security 認證物件
     * @return 本週經驗值排行榜
     */
    @GetMapping("/weekly")
    public ResponseEntity<List<LeaderboardEntryDto>> getWeeklyXpLeaderboard(
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication) {

        // 檢查是否已認證
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        List<LeaderboardEntryDto> leaderboard = leaderboardService.getWeeklyXpLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }
}
