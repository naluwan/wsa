package com.wsa.controller;

import com.wsa.dto.OAuthLoginRequest;
import com.wsa.dto.OAuthLoginResponse;
import com.wsa.dto.UserDto;
import com.wsa.entity.User;
import com.wsa.service.JwtService;
import com.wsa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認證相關 API Controller
 * 處理 OAuth 登入等認證相關的 HTTP 請求
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    /**
     * OAuth 登入 API
     * 接收前端傳來的 OAuth 使用者資料，建立或更新使用者，並產生 JWT token
     *
     * 流程：
     * 1. 根據 provider 和 externalId 建立或更新使用者
     * 2. 產生 JWT token
     * 3. 回傳 token 和使用者資料給前端
     *
     * @param request OAuth 登入請求（包含 provider、externalId、email、displayName、avatarUrl）
     * @return OAuthLoginResponse（包含 JWT token 和使用者資料）
     */
    @PostMapping("/oauth-login")
    public ResponseEntity<OAuthLoginResponse> oauthLogin(@RequestBody OAuthLoginRequest request) {
        // 建立或更新使用者資料
        User user = userService.createOrUpdateUser(request);

        // 為使用者產生 JWT token
        String token = jwtService.generateToken(user.getId());

        // 建立回應物件，包含 token 和使用者基本資料
        OAuthLoginResponse response = OAuthLoginResponse.builder()
                .token(token)
                .user(UserDto.from(user))
                .build();

        return ResponseEntity.ok(response);
    }
}
