package com.wsa.controller;

import com.wsa.dto.CourseDto;
import com.wsa.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 課程相關 API Controller
 * 處理課程查詢相關的 HTTP 請求
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 取得所有課程列表
     * GET /api/courses
     *
     * @param authentication Spring Security 認證物件
     * @return 課程列表
     */
    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses(Authentication authentication) {
        // 檢查是否已認證
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        List<CourseDto> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * 取得單一課程詳情（包含單元列表）
     * GET /api/courses/{courseCode}
     *
     * @param courseCode 課程代碼（例如：SOFTWARE_DESIGN_PATTERN）
     * @param authentication Spring Security 認證物件
     * @return 課程詳情
     */
    @GetMapping("/{courseCode}")
    public ResponseEntity<CourseDto> getCourseByCode(
            @PathVariable String courseCode,
            Authentication authentication) {

        // 檢查是否已認證
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        // 取得當前使用者 ID
        UUID userId = (UUID) authentication.getPrincipal();

        CourseDto course = courseService.getCourseByCode(courseCode, userId);
        return ResponseEntity.ok(course);
    }

    /**
     * 取得課程的單元列表
     * GET /api/courses/{courseCode}/units
     *
     * @param courseCode 課程代碼
     * @param authentication Spring Security 認證物件
     * @return 單元列表
     */
    @GetMapping("/{courseCode}/units")
    public ResponseEntity<List<com.wsa.dto.UnitSummaryDto>> getCourseUnits(
            @PathVariable String courseCode,
            Authentication authentication) {

        // 檢查是否已認證
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        // 取得當前使用者 ID
        UUID userId = (UUID) authentication.getPrincipal();

        CourseDto course = courseService.getCourseByCode(courseCode, userId);
        return ResponseEntity.ok(course.getUnits());
    }
}
