package com.wsa.service;

import com.wsa.dto.OAuthLoginRequest;
import com.wsa.entity.User;
import com.wsa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 使用者服務類別
 * 處理使用者相關的業務邏輯，包含建立新使用者與更新現有使用者資料
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 建立新使用者或更新現有使用者資料
     * 根據 OAuth 提供者和外部 ID 判斷使用者是否已存在：
     * - 若已存在：更新使用者的顯示名稱、電子郵件與頭像
     * - 若不存在：建立新使用者，並初始化等級與經驗值
     *
     * @param request OAuth 登入請求資料
     * @return 建立或更新後的使用者實體
     */
    @Transactional
    public User createOrUpdateUser(OAuthLoginRequest request) {
        // 根據 provider 和 externalId 查詢使用者是否已存在
        Optional<User> existingUser = userRepository.findByProviderAndExternalId(
                request.getProvider(),
                request.getExternalId()
        );

        if (existingUser.isPresent()) {
            // 使用者已存在，更新資料（displayName、email、avatarUrl 可能會變更）
            User user = existingUser.get();
            user.setDisplayName(request.getDisplayName());
            user.setEmail(request.getEmail());
            user.setAvatarUrl(request.getAvatarUrl());
            return userRepository.save(user);
        } else {
            // 使用者不存在，建立新使用者
            // 初始等級為 1，經驗值為 0
            User newUser = User.builder()
                    .externalId(request.getExternalId())
                    .provider(request.getProvider())
                    .displayName(request.getDisplayName())
                    .email(request.getEmail())
                    .avatarUrl(request.getAvatarUrl())
                    .level(1)      // 新使用者初始等級為 1
                    .totalXp(0)    // 新使用者初始總經驗值為 0
                    .weeklyXp(0)   // 新使用者初始本週經驗值為 0
                    .build();
            return userRepository.save(newUser);
        }
    }
}
