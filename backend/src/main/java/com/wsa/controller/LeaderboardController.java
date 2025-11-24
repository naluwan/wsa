package com.wsa.controller;

import com.wsa.dto.LeaderboardEntryDto;
import com.wsa.dto.LeaderboardResponseDto;
import com.wsa.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
     * 取得總經驗值排行榜（新版，支援分頁和當前使用者排名）
     * GET /api/leaderboard/total/v2
     *
     * @param limit 每頁回傳人數（預設 20）
     * @param offset 起始位置（預設 0）
     * @param authentication Spring Security 認證物件（可選，未登入也可查看）
     * @return 排行榜回應資料
     */
    @GetMapping("/total/v2")
    public ResponseEntity<LeaderboardResponseDto> getTotalXpLeaderboardV2(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Authentication authentication) {

        // 取得當前使用者 ID（如果已登入）
        UUID currentUserId = null;
        if (authentication != null && authentication.getPrincipal() != null) {
            try {
                currentUserId = UUID.fromString(authentication.getName());
            } catch (Exception e) {
                // 如果無法解析 UUID，則當作未登入處理
            }
        }

        LeaderboardResponseDto response = leaderboardService.getTotalXpLeaderboardWithPagination(
            limit, offset, currentUserId
        );
        return ResponseEntity.ok(response);
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

    /**
     * 取得本週經驗值排行榜（新版，支援分頁和當前使用者排名）
     * GET /api/leaderboard/weekly/v2
     *
     * @param limit 每頁回傳人數（預設 20）
     * @param offset 起始位置（預設 0）
     * @param authentication Spring Security 認證物件（可選，未登入也可查看）
     * @return 排行榜回應資料
     */
    @GetMapping("/weekly/v2")
    public ResponseEntity<LeaderboardResponseDto> getWeeklyXpLeaderboardV2(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Authentication authentication) {

        // 取得當前使用者 ID（如果已登入）
        UUID currentUserId = null;
        if (authentication != null && authentication.getPrincipal() != null) {
            try {
                currentUserId = UUID.fromString(authentication.getName());
            } catch (Exception e) {
                // 如果無法解析 UUID，則當作未登入處理
            }
        }

        LeaderboardResponseDto response = leaderboardService.getWeeklyXpLeaderboardWithPagination(
            limit, offset, currentUserId
        );
        return ResponseEntity.ok(response);
    }
}
