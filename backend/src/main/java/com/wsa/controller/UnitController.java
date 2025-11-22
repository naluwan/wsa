package com.wsa.controller;

import com.wsa.dto.CompleteUnitResponseDto;
import com.wsa.dto.UnitDto;
import com.wsa.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 單元相關 API Controller
 * 處理單元查詢與完成相關的 HTTP 請求
 */
@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    /**
     * 取得單元詳情（公開端點，未登入也可存取，但 canAccess 會是 false）
     * GET /api/units/{unitId}
     *
     * @param unitId 單元 ID（例如：intro-design-principles）
     * @param authentication Spring Security 認證物件（可為 null）
     * @return 單元詳情（包含 canAccess 權限判斷）
     */
    @GetMapping("/{unitId}")
    public ResponseEntity<UnitDto> getUnitByUnitId(
            @PathVariable String unitId,
            Authentication authentication) {

        // 取得當前使用者 ID（未登入時為 null）
        UUID userId = extractUserId(authentication);

        UnitDto unit = unitService.getUnitByUnitId(unitId, userId);
        return ResponseEntity.ok(unit);
    }

    /**
     * 完成單元並獲得經驗值（必須登入）
     * POST /api/units/{unitId}/complete
     *
     * @param unitId 單元 ID
     * @param authentication Spring Security 認證物件
     * @return 完成單元後的回應資料（包含更新後的使用者資訊）
     */
    @PostMapping("/{unitId}/complete")
    public ResponseEntity<CompleteUnitResponseDto> completeUnit(
            @PathVariable String unitId,
            Authentication authentication) {

        // 檢查是否已認證（完成單元功能必須登入）
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        // 取得當前使用者 ID
        UUID userId = (UUID) authentication.getPrincipal();

        try {
            CompleteUnitResponseDto response = unitService.completeUnit(unitId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 如果單元已完成過，回傳 400
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 從 Authentication 物件中提取使用者 ID
     *
     * @param authentication Spring Security 認證物件
     * @return 使用者 UUID（未登入時返回 null）
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
}
