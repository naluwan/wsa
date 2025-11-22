package com.wsa.repository;

import com.wsa.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 單元資料存取介面
 * 提供單元資料的查詢與持久化操作
 */
@Repository
public interface UnitRepository extends JpaRepository<Unit, UUID> {

    /**
     * 根據單元 ID 查詢單元
     *
     * @param unitId 單元 ID（例如：intro-design-principles）
     * @return 單元資料（若存在）
     */
    Optional<Unit> findByUnitId(String unitId);

    /**
     * 根據課程 ID 查詢該課程的所有單元，並依照排序順序排列
     *
     * @param courseId 課程 UUID
     * @return 單元列表
     */
    List<Unit> findByCourseIdOrderByOrderIndexAsc(UUID courseId);
}
