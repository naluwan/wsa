# R1-User-Progress-Spec.md

## 目的

本規格定義「使用者觀看單元的進度紀錄」行為。  
**重點：此 API 只負責紀錄最後觀看秒數，不給 XP，也不改 level。**  
XP 與升級只透過 `/api/units/{unitId}/complete` 處理（見 `R1-Unit-And-XP-Spec.md`）。

---

## 1. Domain：UserUnitProgress 擴充欄位

### 1.1 資料表變更

現有 `user_unit_progress` 需新增兩個欄位：

- `last_position_seconds` (INTEGER) — 使用者最後觀看到的秒數
- `last_watched_at` (TIMESTAMP) — 最後一次觀看的時間

Migration（建議檔名：`V5__Alter_user_unit_progress_add_progress_fields.sql`）：

```sql
ALTER TABLE user_unit_progress
  ADD COLUMN last_position_seconds INTEGER DEFAULT 0,
  ADD COLUMN last_watched_at TIMESTAMP DEFAULT NOW();
注意：舊資料可以保持預設值 0 / NOW() 即可。

1.2 Entity 更新：UserUnitProgress.java
在 backend/src/main/java/com/wsa/entity/UserUnitProgress.java 中新增欄位：

java
複製程式碼
// 使用者最後觀看到的秒數（影片秒數位置）
@Column(name = "last_position_seconds")
private Integer lastPositionSeconds;

// 使用者最後一次觀看此單元的時間
@Column(name = "last_watched_at")
private LocalDateTime lastWatchedAt;
並確保：

仍然保留 UNIQUE(user_id, unit_id) 約束

仍然有 completed_at 欄位用於判斷是否完成

2. API：更新單元觀看進度
2.1 Endpoint
Method：POST /api/user/progress/{unitId}
Authentication：需要登入（JWT）

用途：
前端每隔 5 秒回報目前觀看到的秒數，後端更新該單元的進度。

2.2 Request
Path Param：

unitId：單元對外 ID（units.unit_id）

Header：

Authorization: Bearer <jwt-token>

Body（JSON）：

json
複製程式碼
{
  "lastPositionSeconds": 123
}
R1 階段只需要這一個欄位。

2.3 行為規則
伺服器處理流程：

從 JWT 取得 userId

根據 unitId 查詢 units 表，取出 unit 記錄

在 user_unit_progress 中找是否已有 (userId, unit.id) 記錄：

若 不存在：建立一筆新紀錄

user_id = userId

unit_id = unit.id

last_position_seconds = body.lastPositionSeconds

last_watched_at = NOW()

completed_at 保持 NULL

若 已存在：更新現有紀錄

last_position_seconds = body.lastPositionSeconds

last_watched_at = NOW()

不得修改 completed_at

回傳 200 OK，內容可簡單回傳目前狀態：

json
複製程式碼
{
  "unitId": "intro-design-principles",
  "lastPositionSeconds": 123
}
⚠️ 此 API 不得 增加 XP / weeklyXp / level，也不可以呼叫完成單元的邏輯。

3. 補充：GET /api/units/{unitId} 需帶 lastPositionSeconds
目前已存在的 GET /api/units/{unitId}（UnitController.java:32）需要補齊：

DTO 更新：UnitDto.java 新增欄位

java
複製程式碼
// 使用者上次觀看到的秒數（若未看過則為 0）
private Integer lastPositionSeconds;
Controller 在組裝 UnitDto 時：

從 JWT 取得 userId

查詢 user_unit_progress 是否有 (userId, unit.id) 記錄

若有，將 dto.setLastPositionSeconds(progress.getLastPositionSeconds())

若沒有，lastPositionSeconds = 0

isCompleted 也可沿用同一筆進度紀錄中的 completed_at != null 來判斷

4. Service 設計建議
新增 Service 類別（若尚未存在）：

java
複製程式碼
// package: com.wsa.service;

@Service
@RequiredArgsConstructor
public class UserProgressService {

    private final UserUnitProgressRepository userUnitProgressRepository;
    private final UnitRepository unitRepository;

    /**
     * 更新使用者觀看單元的最後秒數（不處理 XP 與等級）
     */
    @Transactional
    public UserUnitProgress updateLastPosition(UUID userId, String unitPublicId, int lastPositionSeconds) {
        // 1. 取得 unit
        Unit unit = unitRepository.findByUnitId(unitPublicId)
                .orElseThrow(() -> new NotFoundException("找不到對應的單元：" + unitPublicId));

        // 2. 查詢是否已有進度紀錄
        UserUnitProgress progress = userUnitProgressRepository
                .findByUserIdAndUnitId(userId, unit.getId())
                .orElseGet(() -> {
                    UserUnitProgress p = new UserUnitProgress();
                    p.setUserId(userId);
                    p.setUnit(unit);
                    p.setCompletedAt(null);
                    return p;
                });

        // 3. 更新最後觀看秒數與時間
        progress.setLastPositionSeconds(lastPositionSeconds);
        progress.setLastWatchedAt(LocalDateTime.now());

        return userUnitProgressRepository.save(progress);
    }
}
UserUnitProgressRepository 需提供：

java
複製程式碼
Optional<UserUnitProgress> findByUserIdAndUnitId(UUID userId, UUID unitId);
5. Controller Endpoint 範例
新增一個 Controller，例如 UserProgressController.java：

java
複製程式碼
// package: com.wsa.controller;

@RestController
@RequestMapping("/api/user/progress")
@RequiredArgsConstructor
public class UserProgressController {

    private final UserProgressService userProgressService;
    private final JwtService jwtService;

    // 用來接收前端傳來的 lastPositionSeconds
    @Data
    public static class UpdateProgressRequest {
        private Integer lastPositionSeconds;
    }

    @PostMapping("/{unitId}")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @PathVariable("unitId") String unitId,
            @RequestBody UpdateProgressRequest request,
            HttpServletRequest httpRequest
    ) {
        // 從 JWT 中取得 userId（可抽成共用方法）
        String authHeader = httpRequest.getHeader("Authorization");
        UUID userId = jwtService.extractUserIdFromAuthHeader(authHeader);

        int lastPos = request.getLastPositionSeconds() != null ? request.getLastPositionSeconds() : 0;

        UserUnitProgress progress = userProgressService.updateLastPosition(userId, unitId, lastPos);

        Map<String, Object> body = new HashMap<>();
        body.put("unitId", unitId);
        body.put("lastPositionSeconds", progress.getLastPositionSeconds());

        return ResponseEntity.ok(body);
    }
}
所有關鍵邏輯請加上繁體中文註解（原因、用途、注意事項）。

6. BDD 規格（補充）
6.1 後端：更新進度不加 XP
gherkin
複製程式碼
Feature: 單元觀看進度更新

  @R1 @unit @progress
  Scenario: 使用者回報單元觀看進度不應增加 XP
    Given 我已使用 dev 一鍵登入成功
    And 系統中存在一個影片單元 "intro-design-principles"
    And 我目前的 totalXp 為 X
    When 我呼叫 POST /api/user/progress/intro-design-principles
      And Body 帶入 lastPositionSeconds = 120
    Then 伺服器應回傳 200
      And 回傳中的 lastPositionSeconds 應為 120
      And 使用者的 totalXp 仍然等於 X
6.2 前端：播放器定期回報
gherkin
複製程式碼
Feature: 播放器定期回報觀看進度

  @R1 @frontend @unit @progress
  Scenario: 使用者觀看單元時，系統每 5 秒記錄一次進度
    Given 我已登入並打開 "/units/intro-design-principles"
    And 我看到影片播放器 data-testid="unit-video"
    When 影片播放超過 5 秒
    Then 前端應呼叫 POST /api/user/progress/intro-design-principles
      And Body 中應包含 lastPositionSeconds > 0
7. Claude Code 必須做到的事（總結）
新增 Migration：V5__Alter_user_unit_progress_add_progress_fields.sql

更新 UserUnitProgress Entity 加上：

lastPositionSeconds

lastWatchedAt

新增 UserProgressService（或在既有 Service 中加入對應方法）

新增 UserProgressController，實作：

POST /api/user/progress/{unitId}

更新 UnitDto 與 GET /api/units/{unitId}：

回傳 lastPositionSeconds

所有程式碼需加上繁體中文註解，說明欄位用途與注意事項。