package com.wsa.controller;

import com.wsa.dto.UserDto;
import com.wsa.entity.User;
import com.wsa.repository.UserRepository;
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
}
