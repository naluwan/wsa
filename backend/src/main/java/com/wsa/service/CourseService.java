package com.wsa.service;

import com.wsa.dto.CourseDto;
import com.wsa.dto.UnitSummaryDto;
import com.wsa.entity.Course;
import com.wsa.entity.Unit;
import com.wsa.repository.CourseRepository;
import com.wsa.repository.UnitRepository;
import com.wsa.repository.UserUnitProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 課程服務
 * 處理課程相關的業務邏輯
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UnitRepository unitRepository;
    private final UserUnitProgressRepository progressRepository;

    /**
     * 取得所有課程列表
     *
     * @return 課程列表
     */
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * 根據課程代碼取得課程詳情（包含單元列表）
     *
     * @param courseCode 課程代碼
     * @param userId 使用者 UUID（用於判斷單元是否已完成）
     * @return 課程詳情
     */
    public CourseDto getCourseByCode(String courseCode, UUID userId) {
        Course course = courseRepository.findByCode(courseCode)
            .orElseThrow(() -> new RuntimeException("找不到課程：" + courseCode));

        // 取得該課程的所有單元
        List<Unit> units = unitRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());

        // 轉換為 DTO 並標記完成狀態
        List<UnitSummaryDto> unitDtos = units.stream()
            .map(unit -> convertToUnitSummaryDto(unit, userId))
            .collect(Collectors.toList());

        CourseDto courseDto = convertToDto(course);
        courseDto.setUnits(unitDtos);

        return courseDto;
    }

    /**
     * 將 Course Entity 轉換為 CourseDto
     *
     * @param course 課程實體
     * @return 課程 DTO
     */
    private CourseDto convertToDto(Course course) {
        return CourseDto.builder()
            .id(course.getId())
            .code(course.getCode())
            .title(course.getTitle())
            .description(course.getDescription())
            .levelTag(course.getLevelTag())
            .totalUnits(course.getTotalUnits())
            .coverIcon(course.getCoverIcon())
            .build();
    }

    /**
     * 將 Unit Entity 轉換為 UnitSummaryDto
     *
     * @param unit 單元實體
     * @param userId 使用者 UUID
     * @return 單元摘要 DTO
     */
    private UnitSummaryDto convertToUnitSummaryDto(Unit unit, UUID userId) {
        boolean isCompleted = progressRepository.existsByUserIdAndUnitId(userId, unit.getId());

        return UnitSummaryDto.builder()
            .id(unit.getId())
            .unitId(unit.getUnitId())
            .title(unit.getTitle())
            .type(unit.getType())
            .orderIndex(unit.getOrderIndex())
            .isCompleted(isCompleted)
            .build();
    }
}
