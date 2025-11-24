package com.wsa.dto;

import com.wsa.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * 使用者資料傳輸物件
 * 用於 API 回應，只包含前端需要的使用者資訊
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    /** 使用者唯一識別碼 */
    private UUID id;

    /** 使用者顯示名稱 */
    private String displayName;

    /** 使用者電子郵件 */
    private String email;

    /** 使用者頭像 URL */
    private String avatarUrl;

    /** OAuth 提供者類型（google 或 facebook） */
    private String provider;

    /** 使用者等級 */
    private Integer level;

    /** 使用者總經驗值 */
    private Integer totalXp;

    /** 使用者本週經驗值 */
    private Integer weeklyXp;

    /**
     * 從 User 實體轉換為 UserDto
     *
     * @param user 使用者實體
     * @return 使用者 DTO
     */
    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .provider(user.getProvider())
                .level(user.getLevel())
                .totalXp(user.getTotalXp())
                .weeklyXp(user.getWeeklyXp())
                .build();
    }
}
