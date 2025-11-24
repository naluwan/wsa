package com.wsa.controller;

import com.wsa.entity.UserUnitProgress;
import com.wsa.service.UserProgressService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 使用者單元進度 API Controller
 * 用途：
 *   - 處理使用者觀看單元進度的更新（不處理 XP 與等級）
 *   - 前端播放器每 5 秒呼叫一次，記錄最後觀看位置
 *
 * 重要：
 *   - 此 API 「不得」增加 XP、weeklyXp 或 level
 *   - 完成單元並獲得 XP 的邏輯在 UnitController.completeUnit() 中處理
 */
@RestController
@RequestMapping("/api/user/progress")
@RequiredArgsConstructor
public class UserProgressController {

    private final UserProgressService userProgressService;

    /**
     * 更新使用者觀看單元的進度
     * POST /api/user/progress/{unitId}
     *
     * 使用場景：
     *   - 前端播放器每 5 秒自動呼叫
     *   - 記錄使用者目前觀看到的秒數
     *   - 讓使用者下次可以從此位置繼續觀看
     *
     * 重要：
     *   - 不會增加 XP 或等級
     *   - 不會標記單元為完成
     *   - 只更新 lastPositionSeconds 和 lastWatchedAt
     *
     * @param unitId 單元 ID（例如：intro-design-principles）
     * @param request 請求內容（包含 lastPositionSeconds）
     * @param authentication Spring Security 認證物件
     * @return 更新後的進度資訊
     */
    @PostMapping("/{unitId}")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @PathVariable("unitId") String unitId,
            @RequestBody UpdateProgressRequest request,
            Authentication authentication) {

        // 檢查是否已認證（必須登入才能記錄進度）
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        // 取得當前使用者 ID
        UUID userId = extractUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        // 驗證 lastPositionSeconds（不可為負數）
        int lastPos = request.getLastPositionSeconds() != null ? request.getLastPositionSeconds() : 0;
        if (lastPos < 0) {
            lastPos = 0;
        }

        try {
            // 呼叫 Service 更新進度（不會增加 XP）
            UserUnitProgress progress = userProgressService.updateLastPosition(userId, unitId, lastPos);

            // 建立回應（只回傳 unitId 和 lastPositionSeconds）
            Map<String, Object> response = new HashMap<>();
            response.put("unitId", unitId);
            response.put("lastPositionSeconds", progress.getLastPositionSeconds());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 找不到單元或其他錯誤
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 從 Authentication 物件中提取使用者 ID
     *
     * @param authentication Spring Security 認證物件
     * @return 使用者 UUID（若無法提取則返回 null）
     */
    private UUID extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            try {
                return (UUID) authentication.getPrincipal();
            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 更新進度請求 DTO
     * 用途：接收前端傳來的最後觀看秒數
     */
    @Data
    public static class UpdateProgressRequest {
        /**
         * 使用者最後觀看到的影片秒數
         * 前端播放器每 5 秒回報一次
         */
        private Integer lastPositionSeconds;
    }
}
