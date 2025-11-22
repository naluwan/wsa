package com.wsa.service;

import com.wsa.dto.CourseDetailResponseDto;
import com.wsa.dto.CourseDto;
import com.wsa.dto.SectionDto;
import com.wsa.dto.UnitSummaryDto;
import com.wsa.entity.Course;
import com.wsa.entity.Unit;
import com.wsa.repository.CourseRepository;
import com.wsa.repository.UnitRepository;
import com.wsa.repository.UserUnitProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 課程服務
 * 處理課程相關的業務邏輯，包含課程列表、詳情、存取權限判斷等
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UnitRepository unitRepository;
    private final UserUnitProgressRepository progressRepository;
    private final UserCourseService userCourseService;

    /**
     * 取得所有課程列表（包含擁有狀態）
     *
     * @param userId 使用者 UUID（可為 null，表示未登入）
     * @return 課程列表
     */
    public List<CourseDto> getAllCourses(UUID userId) {
        return courseRepository.findAll().stream()
            .map(course -> convertToDto(course, userId))
            .collect(Collectors.toList());
    }

    /**
     * 根據課程代碼取得課程詳情（包含章節化的單元列表）
     *
     * @param courseCode 課程代碼
     * @param userId 使用者 UUID（可為 null，表示未登入）
     * @return 課程詳情（包含 sections）
     */
    public CourseDetailResponseDto getCourseDetailByCode(String courseCode, UUID userId) {
        Course course = courseRepository.findByCode(courseCode)
            .orElseThrow(() -> new RuntimeException("找不到課程：" + courseCode));

        // 計算使用者是否擁有此課程
        boolean isOwned = userId != null && userCourseService.isUserOwnedCourse(userId, course.getId());

        // 取得該課程的所有單元，按 section_title 和 order_in_section 排序
        List<Unit> units = unitRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());

        // 按 section_title 分組單元
        Map<String, List<Unit>> unitsBySection = units.stream()
            .collect(Collectors.groupingBy(
                Unit::getSectionTitle,
                LinkedHashMap::new,  // 保持插入順序
                Collectors.toList()
            ));

        // 轉換為 SectionDto 列表
        List<SectionDto> sections = unitsBySection.entrySet().stream()
            .map(entry -> {
                String sectionTitle = entry.getKey();
                List<UnitSummaryDto> unitDtos = entry.getValue().stream()
                    // 按 orderInSection 排序
                    .sorted(Comparator.comparing(Unit::getOrderInSection))
                    .map(unit -> convertToUnitSummaryDto(unit, userId, isOwned))
                    .collect(Collectors.toList());

                return SectionDto.builder()
                    .sectionTitle(sectionTitle)
                    .units(unitDtos)
                    .build();
            })
            .collect(Collectors.toList());

        // 建立課程基本資訊 DTO
        CourseDto courseDto = convertToDto(course, userId);

        // 建立完整的課程詳情回應
        return CourseDetailResponseDto.builder()
            .course(courseDto)
            .sections(sections)
            .build();
    }

    /**
     * 將 Course Entity 轉換為 CourseDto
     *
     * @param course 課程實體
     * @param userId 使用者 UUID（可為 null）
     * @return 課程 DTO
     */
    private CourseDto convertToDto(Course course, UUID userId) {
        // 計算使用者是否擁有此課程
        boolean isOwned = userId != null && userCourseService.isUserOwnedCourse(userId, course.getId());

        // 檢查課程是否有免費試看單元
        boolean hasFreePreview = unitRepository.findByCourseIdOrderByOrderIndexAsc(course.getId())
            .stream()
            .anyMatch(Unit::getIsFreePreview);

        return CourseDto.builder()
            .id(course.getId())
            .code(course.getCode())
            .title(course.getTitle())
            .description(course.getDescription())
            .levelTag(course.getLevelTag())
            .totalUnits(course.getTotalUnits())
            .coverIcon(course.getCoverIcon())
            .teacherName(course.getTeacherName())
            .priceTwd(course.getPriceTwd())
            .thumbnailUrl(course.getThumbnailUrl())
            .isPublished(course.getIsPublished())
            .isOwned(isOwned)
            .hasFreePreview(hasFreePreview)
            .build();
    }

    /**
     * 將 Unit Entity 轉換為 UnitSummaryDto（包含存取權限判斷）
     *
     * @param unit 單元實體
     * @param userId 使用者 UUID（可為 null）
     * @param isOwned 使用者是否擁有該課程
     * @return 單元摘要 DTO
     */
    private UnitSummaryDto convertToUnitSummaryDto(Unit unit, UUID userId, boolean isOwned) {
        // 檢查是否已完成
        boolean isCompleted = userId != null && progressRepository.existsByUserIdAndUnitId(userId, unit.getId());

        // 計算 canAccess（存取權限）
        // 規則：
        // 1. 若未登入（userId == null）→ false
        // 2. 若已擁有課程（isOwned）→ true
        // 3. 若未擁有但是免費試看（isFreePreview）→ true
        // 4. 其他情況 → false
        boolean canAccess = calculateCanAccess(userId, isOwned, unit.getIsFreePreview());

        return UnitSummaryDto.builder()
            .id(unit.getId())
            .unitId(unit.getUnitId())
            .title(unit.getTitle())
            .type(unit.getType())
            .orderIndex(unit.getOrderIndex())
            .sectionTitle(unit.getSectionTitle())
            .orderInSection(unit.getOrderInSection())
            .isFreePreview(unit.getIsFreePreview())
            .canAccess(canAccess)
            .isCompleted(isCompleted)
            .build();
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
