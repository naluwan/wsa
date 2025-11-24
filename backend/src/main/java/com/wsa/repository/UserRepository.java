package com.wsa.repository;

import com.wsa.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 使用者資料存取介面
 * 提供使用者資料的查詢與持久化操作
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * 根據 OAuth 提供者和外部 ID 查詢使用者
     * 用於判斷使用者是否已透過特定 OAuth 提供者註冊過
     *
     * @param provider OAuth 提供者（google 或 facebook）
     * @param externalId OAuth 提供者提供的使用者唯一 ID
     * @return 使用者資料（若存在）
     */
    Optional<User> findByProviderAndExternalId(String provider, String externalId);

    /**
     * 根據電子郵件查詢使用者
     *
     * @param email 使用者電子郵件
     * @return 使用者資料（若存在）
     */
    Optional<User> findByEmail(String email);

    /**
     * 計算總經驗值大於指定值的使用者數量
     * 用於計算排行榜排名
     *
     * @param totalXp 總經驗值
     * @return 使用者數量
     */
    long countByTotalXpGreaterThan(Integer totalXp);

    /**
     * 計算本週經驗值大於指定值的使用者數量
     * 用於計算排行榜排名
     *
     * @param weeklyXp 本週經驗值
     * @return 使用者數量
     */
    long countByWeeklyXpGreaterThan(Integer weeklyXp);

    /**
     * 查詢本週經驗值大於指定值的使用者（支援分頁和排序）
     * 用於本週排行榜（只顯示 weeklyXp > 0 的使用者）
     *
     * @param weeklyXp 本週經驗值下限
     * @param pageable 分頁和排序參數
     * @return 使用者分頁資料
     */
    Page<User> findByWeeklyXpGreaterThan(Integer weeklyXp, Pageable pageable);

    /**
     * 使用 offset 和 limit 查詢總排行榜
     * 依照總經驗值降序、建立時間升序排列
     *
     * @param limit 回傳筆數
     * @param offset 跳過筆數
     * @return 使用者列表
     */
    @Query(value = "SELECT * FROM users ORDER BY total_xp DESC, created_at ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<User> findAllOrderByTotalXpWithOffset(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * 使用 offset 和 limit 查詢本週排行榜
     * 依照本週經驗值降序、建立時間升序排列，只查詢 weeklyXp > 0 的使用者
     *
     * @param limit 回傳筆數
     * @param offset 跳過筆數
     * @return 使用者列表
     */
    @Query(value = "SELECT * FROM users WHERE weekly_xp > 0 ORDER BY weekly_xp DESC, created_at ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<User> findByWeeklyXpGreaterThanZeroWithOffset(@Param("limit") int limit, @Param("offset") int offset);
}
