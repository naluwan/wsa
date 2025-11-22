package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth 登入回應 DTO
 * 包含 JWT token 和使用者基本資料
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthLoginResponse {
    /** JWT 認證 token */
    private String token;

    /** 使用者基本資料 */
    private UserDto user;
}
