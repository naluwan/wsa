package com.wsa.service;

import com.wsa.dto.CompleteUnitResponseDto;
import com.wsa.dto.UnitDto;
import com.wsa.entity.Course;
import com.wsa.entity.Unit;
import com.wsa.entity.User;
import com.wsa.entity.UserUnitProgress;
import com.wsa.repository.CourseRepository;
import com.wsa.repository.UnitRepository;
import com.wsa.repository.UserUnitProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 單元服務
 * 處理單元相關的業務邏輯，包含單元完成與經驗值獲取
 */
@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;
    private final CourseRepository courseRepository;
    private final UserUnitProgressRepository progressRepository;
    private final XpService xpService;

    /**
     * 根據單元 ID 取得單元詳情
     *
     * @param unitId 單元 ID
     * @param userId 使用者 UUID（用於判斷是否已完成）
     * @return 單元詳情
     */
    public UnitDto getUnitByUnitId(String unitId, UUID userId) {
        Unit unit = unitRepository.findByUnitId(unitId)
            .orElseThrow(() -> new RuntimeException("找不到單元：" + unitId));

        // 查詢課程代碼
        Course course = courseRepository.findById(unit.getCourseId())
            .orElseThrow(() -> new RuntimeException("找不到課程"));

        // 檢查是否已完成
        boolean isCompleted = progressRepository.existsByUserIdAndUnitId(userId, unit.getId());

        return UnitDto.builder()
            .id(unit.getId())
            .unitId(unit.getUnitId())
            .courseCode(course.getCode())
            .title(unit.getTitle())
            .type(unit.getType())
            .orderIndex(unit.getOrderIndex())
            .videoUrl(unit.getVideoUrl())
            .xpReward(unit.getXpReward())
            .isCompleted(isCompleted)
            .build();
    }

    /**
     * 完成單元並獲得經驗值
     * 如果已經完成過，不會重複給予經驗值
     *
     * @param unitId 單元 ID
     * @param userId 使用者 UUID
     * @return 完成單元後的回應資料
     */
    @Transactional
    public CompleteUnitResponseDto completeUnit(String unitId, UUID userId) {
        Unit unit = unitRepository.findByUnitId(unitId)
            .orElseThrow(() -> new RuntimeException("找不到單元：" + unitId));

        // 檢查是否已經完成過
        boolean alreadyCompleted = progressRepository.existsByUserIdAndUnitId(userId, unit.getId());

        if (!alreadyCompleted) {
            // 建立完成紀錄
            UserUnitProgress progress = UserUnitProgress.builder()
                .userId(userId)
                .unitId(unit.getId())
                .build();
            progressRepository.save(progress);

            // 增加經驗值並更新等級
            User updatedUser = xpService.addXp(userId, unit.getXpReward());

            // 建立回應
            return CompleteUnitResponseDto.builder()
                .user(CompleteUnitResponseDto.UserInfo.builder()
                    .id(updatedUser.getId().toString())
                    .level(updatedUser.getLevel())
                    .totalXp(updatedUser.getTotalXp())
                    .weeklyXp(updatedUser.getWeeklyXp())
                    .build())
                .unit(CompleteUnitResponseDto.UnitInfo.builder()
                    .unitId(unit.getUnitId())
                    .isCompleted(true)
                    .build())
                .build();
        } else {
            // 已經完成過，不重複給經驗值，但回傳目前狀態
            throw new RuntimeException("單元已經完成過了");
        }
    }
}
