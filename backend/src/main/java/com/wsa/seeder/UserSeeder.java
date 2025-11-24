package com.wsa.seeder;

import com.wsa.entity.User;
import com.wsa.repository.UserRepository;
import com.wsa.service.XpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 使用者種子資料生成器
 * 在應用程式啟動時檢查並生成 100 個測試使用者
 *
 * ⚠️ 已停用：改用 Flyway Migration V11__Seed_test_users.sql
 * 此 Seeder 使用動態生成的 external_id，不符合 idempotent 原則
 * 新做法使用固定 UUID，確保可重複執行且不會產生重複資料
 */
// @Component  // ← 已停用，改用 Flyway V11
@RequiredArgsConstructor
@Slf4j
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final XpService xpService;

    /**
     * 等級表：定義每個等級所需的累積經驗值
     * 與 XpService 中的定義相同
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

    private static final String[] FIRST_NAMES = {
        "小明", "小華", "小美", "小芳", "小強", "小麗", "小傑", "小英", "小玲", "小龍",
        "大明", "大華", "大美", "大芳", "大強", "大麗", "大傑", "大英", "大玲", "大龍",
        "阿明", "阿華", "阿美", "阿芳", "阿強", "阿麗", "阿傑", "阿英", "阿玲", "阿龍",
        "志明", "志華", "志偉", "志豪", "志強", "雅婷", "雅玲", "雅文", "雅惠", "雅芳",
        "建國", "建宏", "建志", "建華", "建明", "淑芬", "淑娟", "淑華", "淑貞", "淑玲"
    };

    private static final String[] LAST_NAMES = {
        "王", "李", "張", "劉", "陳", "楊", "黃", "趙", "吳", "周",
        "徐", "孫", "馬", "朱", "胡", "郭", "何", "高", "林", "羅"
    };

    @Override
    public void run(String... args) {
        log.info("=== 開始檢查種子資料 ===");

        // 計算現有的種子使用者數量（provider = "seed"）
        long seedUserCount = userRepository.findAll().stream()
            .filter(user -> "seed".equals(user.getProvider()))
            .count();

        if (seedUserCount >= 100) {
            log.info("種子資料已存在，共 {} 筆，跳過生成", seedUserCount);
            return;
        }

        int needToCreate = (int) (100 - seedUserCount);
        log.info("目前種子使用者數量：{}，需要建立：{} 筆", seedUserCount, needToCreate);

        List<User> seedUsers = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < needToCreate; i++) {
            // 生成隨機名稱
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String displayName = lastName + firstName;

            // 生成隨機經驗值（0 到最高等級的經驗值）
            int maxXp = LEVEL_THRESHOLDS[LEVEL_THRESHOLDS.length - 1];
            int totalXp = random.nextInt(maxXp + 1);

            // 根據經驗值計算等級
            int level = xpService.calculateLevel(totalXp);

            // 生成隨機本週經驗值（0 到 totalXp 之間）
            int weeklyXp = totalXp > 0 ? random.nextInt(totalXp + 1) : 0;

            // 建立使用者
            User user = User.builder()
                .externalId("seed_" + System.currentTimeMillis() + "_" + i)
                .provider("seed")
                .displayName(displayName + (i + 1))
                .email("seed_user_" + (i + 1) + "@example.com")
                .avatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + i)
                .level(level)
                .totalXp(totalXp)
                .weeklyXp(weeklyXp)
                .build();

            seedUsers.add(user);
        }

        // 批次儲存
        userRepository.saveAll(seedUsers);

        log.info("=== 成功建立 {} 筆種子使用者資料 ===", needToCreate);
        log.info("總種子使用者數量：{}", seedUserCount + needToCreate);
    }
}
