package com.wsa.service;

import com.wsa.dto.OAuthLoginRequest;
import com.wsa.entity.User;
import com.wsa.repository.UserRepository;
import com.wsa.repository.UserUnitProgressRepository;
import com.wsa.repository.UserCourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 使用者服務類別
 * 處理使用者相關的業務邏輯，包含建立新使用者與更新現有使用者資料
 */
@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserUnitProgressRepository userUnitProgressRepository;

    @Autowired
    private UserCourseRepository userCourseRepository;

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

    /**
     * 重置使用者的所有學習資料
     *
     * 功能：
     *   1. 清除所有課程觀看進度（user_unit_progress）
     *   2. 重置經驗值（totalXp = 0, weeklyXp = 0, level = 1）
     *   3. 清除所有課程訂單（user_courses）
     *
     * 使用場景：
     *   - 開發測試時重置資料
     *   - 使用者想要重新開始學習
     *
     * 注意：
     *   - 此操作無法復原
     *   - 使用 @Transactional 確保資料一致性
     *
     * @param userId 使用者 UUID
     * @return 重置後的使用者實體
     * @throws RuntimeException 當找不到使用者時
     */
    @Transactional
    public User resetUserData(UUID userId) {
        // 步驟 1：查詢使用者
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到使用者：" + userId));

        // 步驟 2：清除所有課程觀看進度
        // 刪除該使用者的所有 user_unit_progress 記錄
        userUnitProgressRepository.deleteByUserId(userId);
        log.info("[UserService] 已清除使用者 {} 的所有課程觀看進度", userId);

        // 步驟 3：清除所有課程訂單
        // 刪除該使用者的所有 user_courses 記錄
        userCourseRepository.deleteByUserId(userId);
        log.info("[UserService] 已清除使用者 {} 的所有課程訂單", userId);

        // 步驟 4：重置經驗值和等級
        user.setTotalXp(0);
        user.setWeeklyXp(0);
        user.setLevel(1);
        log.info("[UserService] 已重置使用者 {} 的經驗值和等級", userId);

        // 步驟 5：保存更新後的使用者資料
        return userRepository.save(user);
    }
}
