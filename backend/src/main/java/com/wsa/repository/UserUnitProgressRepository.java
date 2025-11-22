package com.wsa.repository;

import com.wsa.entity.UserUnitProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 使用者單元完成進度資料存取介面
 * 提供使用者完成單元紀錄的查詢與持久化操作
 */
@Repository
public interface UserUnitProgressRepository extends JpaRepository<UserUnitProgress, UUID> {

    /**
     * 查詢使用者是否已完成特定單元
     *
     * @param userId 使用者 UUID
     * @param unitId 單元 UUID
     * @return 完成紀錄（若存在）
     */
    Optional<UserUnitProgress> findByUserIdAndUnitId(UUID userId, UUID unitId);

    /**
     * 查詢使用者已完成的所有單元
     *
     * @param userId 使用者 UUID
     * @return 完成紀錄列表
     */
    List<UserUnitProgress> findByUserId(UUID userId);

    /**
     * 檢查使用者是否已完成特定單元
     *
     * @param userId 使用者 UUID
     * @param unitId 單元 UUID
     * @return true 表示已完成，false 表示未完成
     */
    boolean existsByUserIdAndUnitId(UUID userId, UUID unitId);
}
