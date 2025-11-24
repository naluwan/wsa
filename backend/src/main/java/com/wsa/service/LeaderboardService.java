package com.wsa.service;

import com.wsa.dto.LeaderboardEntryDto;
import com.wsa.dto.LeaderboardResponseDto;
import com.wsa.entity.User;
import com.wsa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 排行榜服務
 * 處理排行榜相關的業務邏輯
 */
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserRepository userRepository;

    /**
     * 取得總經驗值排行榜（舊版，保留向後相容）
     *
     * @param limit 回傳人數上限（預設 50）
     * @return 排行榜列表
     */
    public List<LeaderboardEntryDto> getTotalXpLeaderboard(int limit) {
        // 依照總經驗值降序排列
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "totalXp"));
        List<User> users = userRepository.findAll(pageRequest).getContent();

        // 轉換為 DTO 並加上排名
        return IntStream.range(0, users.size())
            .mapToObj(index -> convertToLeaderboardEntry(users.get(index), String.valueOf(index + 1)))
            .collect(Collectors.toList());
    }

    /**
     * 取得總經驗值排行榜（新版，支援分頁和當前使用者排名）
     *
     * @param limit 每頁回傳人數（預設 20）
     * @param offset 起始位置（預設 0）
     * @param currentUserId 當前使用者 ID（可為 null）
     * @return 排行榜回應資料
     */
    public LeaderboardResponseDto getTotalXpLeaderboardWithPagination(int limit, int offset, UUID currentUserId) {
        // 使用 offset/limit 查詢（修正分頁錯誤）
        List<User> users = userRepository.findAllOrderByTotalXpWithOffset(limit, offset);

        // 取得總人數
        long total = userRepository.count();

        // 移除「強制插入當前使用者到列表」的邏輯
        // 當前使用者的排名會在 currentUserEntry 中單獨顯示
        // 這樣可以避免分頁時的資料重複問題

        // 轉換為 DTO 並計算正確排名（使用 countByTotalXpGreaterThan）
        List<LeaderboardEntryDto> leaderboard = users.stream()
            .map(user -> {
                // 計算排名：比此使用者經驗值高的人數 + 1
                long rank = userRepository.countByTotalXpGreaterThan(user.getTotalXp()) + 1;
                return convertToLeaderboardEntry(user, String.valueOf(rank));
            })
            .collect(Collectors.toList());

        // 計算是否有更多資料
        boolean hasMore = (offset + limit) < total;

        // 如果有當前使用者 ID，計算其排名
        LeaderboardEntryDto currentUserEntry = null;
        if (currentUserId != null) {
            currentUserEntry = getCurrentUserTotalXpRank(currentUserId);
        }

        return LeaderboardResponseDto.builder()
            .leaderboard(leaderboard)
            .currentUserEntry(currentUserEntry)
            .total(total)
            .hasMore(hasMore)
            .build();
    }

    /**
     * 取得本週經驗值排行榜（舊版，保留向後相容）
     *
     * @param limit 回傳人數上限（預設 50）
     * @return 排行榜列表
     */
    public List<LeaderboardEntryDto> getWeeklyXpLeaderboard(int limit) {
        // 依照本週經驗值降序排列
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "weeklyXp"));
        List<User> users = userRepository.findAll(pageRequest).getContent();

        // 轉換為 DTO 並加上排名
        return IntStream.range(0, users.size())
            .mapToObj(index -> convertToLeaderboardEntry(users.get(index), String.valueOf(index + 1)))
            .collect(Collectors.toList());
    }

    /**
     * 取得本週經驗值排行榜（新版，支援分頁和當前使用者排名）
     * 只顯示本週經驗值大於 0 的使用者
     *
     * @param limit 每頁回傳人數（預設 20）
     * @param offset 起始位置（預設 0）
     * @param currentUserId 當前使用者 ID（可為 null）
     * @return 排行榜回應資料
     */
    public LeaderboardResponseDto getWeeklyXpLeaderboardWithPagination(int limit, int offset, UUID currentUserId) {
        // 使用 offset/limit 查詢（修正分頁錯誤）
        List<User> users = userRepository.findByWeeklyXpGreaterThanZeroWithOffset(limit, offset);

        // 取得本週經驗值大於 0 的總人數
        long total = userRepository.countByWeeklyXpGreaterThan(0);

        // 移除「強制插入當前使用者到列表」的邏輯
        // 當前使用者的排名會在 currentUserEntry 中單獨顯示
        // 這樣可以避免分頁時的資料重複問題

        // 轉換為 DTO 並計算正確排名（使用 countByWeeklyXpGreaterThan）
        List<LeaderboardEntryDto> leaderboard = users.stream()
            .map(user -> {
                // 計算排名：比此使用者經驗值高的人數 + 1
                long rank = userRepository.countByWeeklyXpGreaterThan(user.getWeeklyXp()) + 1;
                return convertToLeaderboardEntry(user, String.valueOf(rank));
            })
            .collect(Collectors.toList());

        // 計算是否有更多資料
        boolean hasMore = (offset + limit) < total;

        // 如果有當前使用者 ID，計算其排名
        LeaderboardEntryDto currentUserEntry = null;
        if (currentUserId != null) {
            currentUserEntry = getCurrentUserWeeklyXpRank(currentUserId);
        }

        return LeaderboardResponseDto.builder()
            .leaderboard(leaderboard)
            .currentUserEntry(currentUserEntry)
            .total(total)
            .hasMore(hasMore)
            .build();
    }

    /**
     * 取得當前使用者在總經驗值排行榜中的排名
     *
     * @param userId 使用者 ID
     * @return 當前使用者的排行榜項目
     */
    private LeaderboardEntryDto getCurrentUserTotalXpRank(UUID userId) {
        User currentUser = userRepository.findById(userId).orElse(null);
        if (currentUser == null) {
            return null;
        }

        // 計算排名：比當前使用者經驗值高的人數 + 1
        long rank = userRepository.countByTotalXpGreaterThan(currentUser.getTotalXp()) + 1;

        return convertToLeaderboardEntry(currentUser, String.valueOf(rank));
    }

    /**
     * 取得當前使用者在本週經驗值排行榜中的排名
     *
     * @param userId 使用者 ID
     * @return 當前使用者的排行榜項目
     */
    private LeaderboardEntryDto getCurrentUserWeeklyXpRank(UUID userId) {
        User currentUser = userRepository.findById(userId).orElse(null);
        if (currentUser == null) {
            return null;
        }

        // 如果本週經驗值為 0，排名顯示 "-"
        if (currentUser.getWeeklyXp() == 0) {
            return convertToLeaderboardEntry(currentUser, "-");
        }

        // 計算排名：比當前使用者經驗值高的人數 + 1
        long rank = userRepository.countByWeeklyXpGreaterThan(currentUser.getWeeklyXp()) + 1;

        return convertToLeaderboardEntry(currentUser, String.valueOf(rank));
    }

    /**
     * 將 User 轉換為 LeaderboardEntryDto
     *
     * @param user 使用者實體
     * @param rank 排名（字串形式，可為數字或 "-"）
     * @return 排行榜項目 DTO
     */
    private LeaderboardEntryDto convertToLeaderboardEntry(User user, String rank) {
        return LeaderboardEntryDto.builder()
            .rank(rank)
            .userId(user.getId().toString())
            .displayName(user.getDisplayName())
            .avatarUrl(user.getAvatarUrl())
            .level(user.getLevel())
            .totalXp(user.getTotalXp())
            .weeklyXp(user.getWeeklyXp())
            .build();
    }
}
