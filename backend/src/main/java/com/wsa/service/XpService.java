package com.wsa.service;

import com.wsa.entity.User;
import com.wsa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 經驗值與等級服務
 * 處理使用者經驗值的增加與等級計算
 */
@Service
@RequiredArgsConstructor
public class XpService {

    private final UserRepository userRepository;

    /**
     * 等級表：定義每個等級所需的累積經驗值
     * 根據規格書的等級表實作
     */
    private static final int[] LEVEL_THRESHOLDS = {
        0,      // Level 1
        200,    // Level 2
        500,    // Level 3
        1500,   // Level 4
        3000,   // Level 5
        5000,   // Level 6
        7000,   // Level 7
        9000,   // Level 8
        11000,  // Level 9
        13000,  // Level 10
        15000,  // Level 11
        17000,  // Level 12
        19000,  // Level 13
        21000,  // Level 14
        23000,  // Level 15
        25000,  // Level 16
        27000,  // Level 17
        29000,  // Level 18
        31000,  // Level 19
        33000,  // Level 20
        35000,  // Level 21
        37000,  // Level 22
        39000,  // Level 23
        41000,  // Level 24
        43000,  // Level 25
        45000,  // Level 26
        47000,  // Level 27
        49000,  // Level 28
        51000,  // Level 29
        53000,  // Level 30
        55000,  // Level 31
        57000,  // Level 32
        59000,  // Level 33
        61000,  // Level 34
        63000,  // Level 35
        65000   // Level 36（最高等級）
    };

    /**
     * 為使用者增加經驗值並更新等級
     *
     * @param userId 使用者 UUID
     * @param xpAmount 要增加的經驗值數量
     * @return 更新後的使用者資料
     */
    @Transactional
    public User addXp(UUID userId, int xpAmount) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("找不到使用者"));

        // 增加總經驗值和本週經驗值
        user.setTotalXp(user.getTotalXp() + xpAmount);
        user.setWeeklyXp(user.getWeeklyXp() + xpAmount);

        // 根據新的總經驗值計算等級
        int newLevel = calculateLevel(user.getTotalXp());
        user.setLevel(newLevel);

        return userRepository.save(user);
    }

    /**
     * 根據總經驗值計算使用者等級
     * 找出累積 XP >= totalXp 的最大等級
     *
     * @param totalXp 總經驗值
     * @return 對應的等級（1-36）
     */
    public int calculateLevel(int totalXp) {
        // 從最高等級開始往下找，找到第一個門檻 <= totalXp 的等級
        for (int level = LEVEL_THRESHOLDS.length - 1; level >= 0; level--) {
            if (totalXp >= LEVEL_THRESHOLDS[level]) {
                return level + 1; // 等級從 1 開始，陣列索引從 0 開始
            }
        }
        return 1; // 預設為等級 1
    }

    /**
     * 取得升到下一等級所需的經驗值
     *
     * @param currentLevel 目前等級
     * @return 升級所需的經驗值（如果已是最高等級則回傳 0）
     */
    public int getXpToNextLevel(int currentLevel) {
        if (currentLevel >= LEVEL_THRESHOLDS.length) {
            return 0; // 已達最高等級
        }
        return LEVEL_THRESHOLDS[currentLevel];
    }
}
