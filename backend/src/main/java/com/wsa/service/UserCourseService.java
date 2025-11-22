package com.wsa.service;

import com.wsa.entity.Course;
import com.wsa.entity.UserCourse;
import com.wsa.repository.CourseRepository;
import com.wsa.repository.UserCourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 使用者課程擁有權服務
 * 處理課程購買與擁有權相關的業務邏輯
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserCourseService {

    private final UserCourseRepository userCourseRepository;
    private final CourseRepository courseRepository;

    /**
     * 檢查使用者是否擁有特定課程
     *
     * @param userId   使用者 ID
     * @param courseId 課程 ID
     * @return true 表示使用者擁有該課程，false 表示未擁有
     */
    public boolean isUserOwnedCourse(UUID userId, UUID courseId) {
        if (userId == null || courseId == null) {
            return false;
        }
        return userCourseRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    /**
     * 檢查使用者是否擁有特定課程（根據課程代碼）
     *
     * @param userId     使用者 ID
     * @param courseCode 課程代碼
     * @return true 表示使用者擁有該課程，false 表示未擁有
     */
    public boolean isUserOwnedCourseByCode(UUID userId, String courseCode) {
        if (userId == null || courseCode == null) {
            return false;
        }

        // 根據課程代碼查詢課程
        Optional<Course> courseOpt = courseRepository.findByCode(courseCode);
        if (courseOpt.isEmpty()) {
            log.warn("課程代碼不存在: {}", courseCode);
            return false;
        }

        return isUserOwnedCourse(userId, courseOpt.get().getId());
    }

    /**
     * Mock 購買課程（不串接真實金流）
     * 為使用者建立課程擁有權記錄
     *
     * @param userId     使用者 ID
     * @param courseCode 課程代碼
     * @return 建立或已存在的課程擁有權記錄
     * @throws IllegalArgumentException 若課程代碼不存在
     */
    @Transactional
    public UserCourse purchaseCourseMock(UUID userId, String courseCode) {
        log.info("Mock 購買課程 - 使用者: {}, 課程代碼: {}", userId, courseCode);

        // 檢查課程是否存在
        Course course = courseRepository.findByCode(courseCode)
                .orElseThrow(() -> new IllegalArgumentException("課程代碼不存在: " + courseCode));

        // 檢查使用者是否已擁有該課程
        Optional<UserCourse> existingOwnership = userCourseRepository
                .findByUserIdAndCourseId(userId, course.getId());

        if (existingOwnership.isPresent()) {
            log.info("使用者已擁有該課程，返回現有記錄");
            return existingOwnership.get();
        }

        // 建立新的課程擁有權記錄
        UserCourse userCourse = UserCourse.builder()
                .userId(userId)
                .courseId(course.getId())
                .purchasedAt(LocalDateTime.now())
                .build();

        UserCourse saved = userCourseRepository.save(userCourse);
        log.info("課程購買成功 - 記錄 ID: {}", saved.getId());

        return saved;
    }

    /**
     * 取得使用者擁有的所有課程
     *
     * @param userId 使用者 ID
     * @return 使用者擁有的所有課程記錄清單
     */
    public List<UserCourse> getUserOwnedCourses(UUID userId) {
        return userCourseRepository.findAllByUserId(userId);
    }

    /**
     * 取得特定課程的所有擁有者
     *
     * @param courseId 課程 ID
     * @return 擁有該課程的所有使用者記錄清單
     */
    public List<UserCourse> getCourseOwners(UUID courseId) {
        return userCourseRepository.findAllByCourseId(courseId);
    }
}
