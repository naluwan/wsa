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
     * 取得單元詳情
     * GET /api/units/{unitId}
     *
     * @param unitId 單元 ID（例如：intro-design-principles）
     * @param authentication Spring Security 認證物件
     * @return 單元詳情
     */
    @GetMapping("/{unitId}")
    public ResponseEntity<UnitDto> getUnitByUnitId(
            @PathVariable String unitId,
            Authentication authentication) {

        // 檢查是否已認證
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        // 取得當前使用者 ID
        UUID userId = (UUID) authentication.getPrincipal();

        UnitDto unit = unitService.getUnitByUnitId(unitId, userId);
        return ResponseEntity.ok(unit);
    }

    /**
     * 完成單元並獲得經驗值
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

        // 檢查是否已認證
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
}
