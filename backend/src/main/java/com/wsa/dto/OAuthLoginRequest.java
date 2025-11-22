package com.wsa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth 登入請求 DTO
 * 從前端接收 OAuth 提供者回傳的使用者基本資料
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthLoginRequest {
    /** OAuth 提供者（google 或 facebook） */
    private String provider;

    /** OAuth 提供者提供的使用者唯一 ID */
    private String externalId;

    /** 使用者電子郵件 */
    private String email;

    /** 使用者顯示名稱 */
    private String displayName;

    /** 使用者頭像 URL */
    private String avatarUrl;
}
