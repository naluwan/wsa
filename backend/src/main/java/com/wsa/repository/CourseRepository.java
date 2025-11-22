package com.wsa.repository;

import com.wsa.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 課程資料存取介面
 * 提供課程資料的查詢與持久化操作
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    /**
     * 根據課程代碼查詢課程
     *
     * @param code 課程代碼（例如：BACKEND_JAVA, SOFTWARE_DESIGN_PATTERN）
     * @return 課程資料（若存在）
     */
    Optional<Course> findByCode(String code);
}
