package com.wsa.service;

import com.wsa.dto.LeaderboardEntryDto;
import com.wsa.entity.User;
import com.wsa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
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
     * 取得總經驗值排行榜
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
            .mapToObj(index -> convertToLeaderboardEntry(users.get(index), index + 1))
            .collect(Collectors.toList());
    }

    /**
     * 取得本週經驗值排行榜
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
            .mapToObj(index -> convertToLeaderboardEntry(users.get(index), index + 1))
            .collect(Collectors.toList());
    }

    /**
     * 將 User 轉換為 LeaderboardEntryDto
     *
     * @param user 使用者實體
     * @param rank 排名
     * @return 排行榜項目 DTO
     */
    private LeaderboardEntryDto convertToLeaderboardEntry(User user, int rank) {
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
