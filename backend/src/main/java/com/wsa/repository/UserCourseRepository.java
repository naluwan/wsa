package com.wsa.repository;

import com.wsa.entity.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 使用者課程擁有權 Repository
 * 提供課程購買記錄的資料庫操作方法
 */
@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, UUID> {

    /**
     * 查詢使用者是否擁有特定課程
     *
     * @param userId   使用者 ID
     * @param courseId 課程 ID
     * @return 擁有權記錄（若存在）
     */
    Optional<UserCourse> findByUserIdAndCourseId(UUID userId, UUID courseId);

    /**
     * 查詢使用者擁有的所有課程
     *
     * @param userId 使用者 ID
     * @return 使用者擁有的所有課程記錄清單
     */
    List<UserCourse> findAllByUserId(UUID userId);

    /**
     * 查詢特定課程的所有擁有者
     *
     * @param courseId 課程 ID
     * @return 擁有該課程的所有使用者記錄清單
     */
    List<UserCourse> findAllByCourseId(UUID courseId);

    /**
     * 檢查使用者是否擁有特定課程
     *
     * @param userId   使用者 ID
     * @param courseId 課程 ID
     * @return true 表示使用者擁有該課程，false 表示未擁有
     */
    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);
}
