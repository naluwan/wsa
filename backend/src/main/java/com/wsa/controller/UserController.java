package com.wsa.controller;

import com.wsa.dto.UserDto;
import com.wsa.entity.User;
import com.wsa.repository.UserRepository;
import com.wsa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 使用者相關 API Controller
 * 處理使用者資料查詢等 HTTP 請求
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * 取得當前登入使用者的資料
     * 從 Spring Security 的 Authentication 物件中取得使用者 ID，
     * 然後查詢資料庫並回傳使用者資料
     *
     * @param authentication Spring Security 認證物件（由 JwtFilter 設定）
     * @return UserDto 使用者資料
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        System.out.println("[UserController] /api/user/me 被調用");
        System.out.println("[UserController] Authentication: " + authentication);
        System.out.println("[UserController] Principal: " + (authentication != null ? authentication.getPrincipal() : "null"));

        // 檢查是否已認證
        if (authentication == null || authentication.getPrincipal() == null) {
            System.out.println("[UserController] 未認證，回傳 401");
            return ResponseEntity.status(401).build();
        }

        // 從 Authentication 中取得使用者 ID（JwtFilter 設定的 principal）
        UUID userId = (UUID) authentication.getPrincipal();
        System.out.println("[UserController] 使用者 ID: " + userId);

        // 從資料庫查詢使用者資料
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("[UserController] 找到使用者: " + user.getDisplayName());

        // 轉換為 DTO 並回傳
        return ResponseEntity.ok(UserDto.from(user));
    }

    /**
     * 重置使用者的所有學習資料
     *
     * 功能說明：
     *   1. 清除所有課程觀看進度（user_unit_progress）
     *   2. 重置經驗值（totalXp = 0, weeklyXp = 0, level = 1）
     *   3. 清除所有課程訂單（user_courses）
     *
     * 使用場景：
     *   - 開發測試時重置資料
     *   - 使用者想要重新開始學習
     *
     * 注意事項：
     *   - 此操作無法復原
     *   - 需要使用者登入（從 Authentication 取得使用者 ID）
     *
     * @param authentication Spring Security 認證物件（由 JwtFilter 設定）
     * @return UserDto 重置後的使用者資料
     */
    @PostMapping("/reset")
    public ResponseEntity<UserDto> resetUserData(Authentication authentication) {
        System.out.println("[UserController] /api/user/reset 被調用");
        System.out.println("[UserController] Authentication: " + authentication);

        // 步驟 1：檢查是否已認證
        if (authentication == null || authentication.getPrincipal() == null) {
            System.out.println("[UserController] 未認證，回傳 401");
            return ResponseEntity.status(401).build();
        }

        // 步驟 2：從 Authentication 中取得使用者 ID
        UUID userId = (UUID) authentication.getPrincipal();
        System.out.println("[UserController] 準備重置使用者 ID: " + userId);

        // 步驟 3：呼叫 UserService 進行重置
        User resetUser = userService.resetUserData(userId);
        System.out.println("[UserController] 使用者 " + userId + " 資料重置完成");

        // 步驟 4：回傳重置後的使用者資料
        return ResponseEntity.ok(UserDto.from(resetUser));
    }
}
