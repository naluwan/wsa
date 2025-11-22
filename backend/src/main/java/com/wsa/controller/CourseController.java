package com.wsa.controller;

import com.wsa.dto.CourseDetailResponseDto;
import com.wsa.dto.CourseDto;
import com.wsa.dto.CoursePurchaseResponseDto;
import com.wsa.entity.UserCourse;
import com.wsa.service.CourseService;
import com.wsa.service.UserCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 課程相關 API Controller
 * 處理課程查詢、詳情與購買相關的 HTTP 請求
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserCourseService userCourseService;

    /**
     * 取得所有課程列表（公開端點，未登入也可存取）
     * GET /api/courses
     *
     * @param authentication Spring Security 認證物件（可為 null）
     * @return 課程列表（包含 isOwned 狀態）
     */
    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses(Authentication authentication) {
        // 取得當前使用者 ID（未登入時為 null）
        UUID userId = extractUserId(authentication);

        List<CourseDto> courses = courseService.getAllCourses(userId);
        return ResponseEntity.ok(courses);
    }

    /**
     * 取得單一課程詳情（包含章節化的單元列表）（公開端點，未登入也可存取）
     * GET /api/courses/{courseCode}
     *
     * @param courseCode 課程代碼（例如：SOFTWARE_DESIGN_PATTERN）
     * @param authentication Spring Security 認證物件（可為 null）
     * @return 課程詳情（包含 sections）
     */
    @GetMapping("/{courseCode}")
    public ResponseEntity<CourseDetailResponseDto> getCourseDetailByCode(
            @PathVariable String courseCode,
            Authentication authentication) {

        // 取得當前使用者 ID（未登入時為 null）
        UUID userId = extractUserId(authentication);

        CourseDetailResponseDto courseDetail = courseService.getCourseDetailByCode(courseCode, userId);
        return ResponseEntity.ok(courseDetail);
    }

    /**
     * Mock 購買課程（不串接真實金流）
     * POST /api/courses/{courseCode}/purchase/mock
     *
     * @param courseCode 課程代碼
     * @param authentication Spring Security 認證物件（必須登入）
     * @return 購買結果（包含課程擁有狀態）
     */
    @PostMapping("/{courseCode}/purchase/mock")
    public ResponseEntity<CoursePurchaseResponseDto> purchaseCourseMock(
            @PathVariable String courseCode,
            Authentication authentication) {

        // 檢查是否已認證（購買功能必須登入）
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        // 取得當前使用者 ID
        UUID userId = (UUID) authentication.getPrincipal();

        try {
            // 執行 Mock 購買
            UserCourse userCourse = userCourseService.purchaseCourseMock(userId, courseCode);

            // 重新取得課程資訊（此時 isOwned 應為 true）
            CourseDetailResponseDto courseDetail = courseService.getCourseDetailByCode(courseCode, userId);

            // 建立購買回應
            CoursePurchaseResponseDto response = CoursePurchaseResponseDto.builder()
                .course(CoursePurchaseResponseDto.PurchasedCourseInfo.builder()
                    .courseCode(courseDetail.getCourse().getCode())
                    .title(courseDetail.getCourse().getTitle())
                    .isOwned(true)
                    .build())
                .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 課程不存在
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // 其他錯誤
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 從 Authentication 物件中提取使用者 ID
     *
     * @param authentication Spring Security 認證物件
     * @return 使用者 UUID（未登入時返回 null）
     */
    private UUID extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            try {
                return (UUID) authentication.getPrincipal();
            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }
}
