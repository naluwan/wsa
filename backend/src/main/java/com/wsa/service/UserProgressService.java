package com.wsa.service;

import com.wsa.entity.Unit;
import com.wsa.entity.UserUnitProgress;
import com.wsa.repository.UnitRepository;
import com.wsa.repository.UserUnitProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 使用者單元進度服務
 * 用途：
 *   - 處理使用者觀看單元的進度更新（不處理 XP 與等級）
 *   - 記錄最後觀看位置，讓使用者下次可以從此位置繼續觀看
 *
 * 重要：
 *   - 此服務「不得」增加 XP、weeklyXp 或 level
 *   - 完成單元並獲得 XP 的邏輯在 UnitService.completeUnit() 中處理
 *   - 進度更新時，completedAt 保持 null（只有完成單元時才設值）
 */
@Service
@RequiredArgsConstructor
public class UserProgressService {

    private final UserUnitProgressRepository userUnitProgressRepository;
    private final UnitRepository unitRepository;

    /**
     * 更新使用者觀看單元的最後秒數
     *
     * 行為：
     *   1. 若進度記錄不存在，建立新記錄（completedAt = null）
     *   2. 若進度記錄已存在，更新 lastPositionSeconds 和 lastWatchedAt
     *   3. 不修改 completedAt 欄位（保持原值）
     *   4. 不增加 XP 或等級
     *
     * 使用場景：
     *   - 前端播放器每 5 秒呼叫一次
     *   - 保存使用者目前觀看到的秒數
     *
     * @param userId 使用者 UUID
     * @param unitPublicId 單元對外 ID（例如：intro-design-principles）
     * @param lastPositionSeconds 最後觀看到的秒數位置
     * @return 更新後的進度記錄
     * @throws RuntimeException 當找不到對應的單元時
     */
    @Transactional
    public UserUnitProgress updateLastPosition(UUID userId, String unitPublicId, int lastPositionSeconds) {
        // 步驟 1：根據單元 ID 查詢單元資料
        Unit unit = unitRepository.findByUnitId(unitPublicId)
                .orElseThrow(() -> new RuntimeException("找不到對應的單元：" + unitPublicId));

        // 步驟 2：查詢是否已有進度記錄
        UserUnitProgress progress = userUnitProgressRepository
                .findByUserIdAndUnitId(userId, unit.getId())
                .orElseGet(() -> {
                    // 若不存在，建立新的進度記錄
                    // 注意：completedAt 保持 null，表示尚未完成
                    UserUnitProgress newProgress = new UserUnitProgress();
                    newProgress.setUserId(userId);
                    newProgress.setUnitId(unit.getId());
                    newProgress.setCompletedAt(null);  // 重要：進度記錄時不設 completedAt
                    return newProgress;
                });

        // 步驟 3：更新最後觀看秒數與時間
        progress.setLastPositionSeconds(lastPositionSeconds);
        progress.setLastWatchedAt(LocalDateTime.now());

        // 注意：不修改 completedAt（保持原值）
        // - 若是新記錄，completedAt 為 null
        // - 若是已完成的單元，completedAt 保持完成時的時間

        // 步驟 4：儲存並返回
        return userUnitProgressRepository.save(progress);
    }

    /**
     * 查詢使用者在特定單元的進度
     *
     * 用途：
     *   - 讓前端取得使用者上次觀看位置
     *   - 判斷單元是否已完成
     *
     * @param userId 使用者 UUID
     * @param unitId 單元 UUID（注意：這是內部 UUID，不是 unitId）
     * @return 進度記錄（若不存在則返回 null）
     */
    public UserUnitProgress getProgress(UUID userId, UUID unitId) {
        return userUnitProgressRepository
                .findByUserIdAndUnitId(userId, unitId)
                .orElse(null);
    }
}
