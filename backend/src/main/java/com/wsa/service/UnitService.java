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
 * 處理單元相關的業務邏輯，包含單元完成、經驗值獲取與存取權限判斷
 */
@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;
    private final CourseRepository courseRepository;
    private final UserUnitProgressRepository progressRepository;
    private final UserCourseService userCourseService;
    private final XpService xpService;

    /**
     * 根據單元 ID 取得單元詳情（包含存取權限判斷）
     *
     * @param unitId 單元 ID
     * @param userId 使用者 UUID（可為 null，表示未登入）
     * @return 單元詳情
     */
    public UnitDto getUnitByUnitId(String unitId, UUID userId) {
        Unit unit = unitRepository.findByUnitId(unitId)
            .orElseThrow(() -> new RuntimeException("找不到單元：" + unitId));

        // 查詢課程資訊
        Course course = courseRepository.findById(unit.getCourseId())
            .orElseThrow(() -> new RuntimeException("找不到課程"));

        // 計算使用者是否擁有該課程
        boolean isOwned = userId != null && userCourseService.isUserOwnedCourse(userId, course.getId());

        // 計算存取權限（canAccess）
        // 規則：
        // - 若未登入（userId == null）→ false
        // - 若已擁有課程（isOwned）→ true
        // - 若未擁有但是免費試看（isFreePreview）→ true
        // - 其他情況 → false
        boolean canAccess = calculateCanAccess(userId, isOwned, unit.getIsFreePreview());

        // 檢查是否已完成
        boolean isCompleted = userId != null && progressRepository.existsByUserIdAndUnitId(userId, unit.getId());

        // 建立 UnitDto
        UnitDto.UnitDtoBuilder builder = UnitDto.builder()
            .id(unit.getId())
            .unitId(unit.getUnitId())
            .courseCode(course.getCode())
            .courseTitle(course.getTitle())
            .sectionTitle(unit.getSectionTitle())
            .title(unit.getTitle())
            .type(unit.getType())
            .orderIndex(unit.getOrderIndex())
            .orderInSection(unit.getOrderInSection())
            .xpReward(unit.getXpReward())
            .isFreePreview(unit.getIsFreePreview())
            .canAccess(canAccess)
            .isCompleted(isCompleted);

        // 當 canAccess = false 時，不回傳 videoUrl（保護影片資源）
        if (canAccess) {
            builder.videoUrl(unit.getVideoUrl());
        }

        return builder.build();
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

    /**
     * 計算使用者是否可以存取特定單元
     *
     * 存取規則（根據 R1-Course-Unit-Access-And-Ownership-Spec.md）：
     * - 若 !isLoggedIn → false（所有單元都需要登入）
     * - 若 isOwned → true（所有單元可看）
     * - 若 !isOwned && isFreePreview → true（免費試看）
     * - 其他情況 → false（顯示鎖頭與「無法觀看」訊息）
     *
     * @param userId 使用者 ID（null 表示未登入）
     * @param isOwned 是否擁有課程
     * @param isFreePreview 是否為免費試看單元
     * @return true 表示可以存取，false 表示無法存取
     */
    private boolean calculateCanAccess(UUID userId, boolean isOwned, boolean isFreePreview) {
        // 未登入 → 無法存取任何單元
        if (userId == null) {
            return false;
        }

        // 已擁有課程 → 可存取所有單元
        if (isOwned) {
            return true;
        }

        // 未擁有但是免費試看 → 可存取
        if (isFreePreview) {
            return true;
        }

        // 其他情況 → 無法存取
        return false;
    }
}
