# R1 排行榜（Leaderboard）規格書

> 目標：  
> 使用者可以看到：
> - 學習排行榜（總 XP 排名）
> - 本週成長榜（weekly XP 排名）  
> 完成單元後，這兩個榜單的數字會隨之更新。

---

## 1. 範圍與非範圍

### 1.1 本規格涵蓋

- 排行榜查詢 API（後端）
- 排行排行榜前端頁 `/leaderboard` 串接真實 API
- 基本排序規則與欄位定義

### 1.2 不在本規格的內容

- 好友排行榜 / 自訂群組排行榜
- Leaderboard 快取 / Redis
- 精準週期結算排程（可在 R2 透過排程重設 weeklyXp）

---

## 2. 排行榜邏輯

### 2.1 學習排行榜（總 XP）

- 依據 `users.total_xp` 由高到低排序。
- 若 XP 相同，可以用 `created_at` 或 `id` 做次排序（但 R1 不強制）。

### 2.2 本週成長榜（weekly XP）

- 依據 `users.weekly_xp` 由高到低排序。
- 每週一 00:00（台灣時間）理論上應重置，但 R1 可以先假設：
  - 開發階段由後端手動清 weeklyXp
  - 或不實作排程，只保留欄位與排序邏輯。

---

## 3. 後端 API 規格

Base URL：`/api/leaderboard`

### 3.1 總 XP 排行榜

- Method：`GET /api/leaderboard/total`
- 說明：取得依照 `total_xp` 排序的前 N 名使用者
- 權限：需要登入（JWT）

#### 查詢參數（可選）

- `limit`：回傳人數上限（預設 50）

#### Response（200 OK）

```json
[
  {
    "rank": 1,
    "userId": "uuid",
    "displayName": "水球學員 A",
    "avatarUrl": "https://...",
    "level": 10,
    "totalXp": 53000,
    "weeklyXp": 1200
  },
  {
    "rank": 2,
    "userId": "uuid",
    "displayName": "水球學員 B",
    "avatarUrl": "https://...",
    "level": 8,
    "totalXp": 41000,
    "weeklyXp": 900
  }
]
3.2 本週成長榜
Method：GET /api/leaderboard/weekly

說明：依 weekly_xp 排序的前 N 名使用者

Response（200 OK）
json
複製程式碼
[
  {
    "rank": 1,
    "userId": "uuid",
    "displayName": "水球學員 C",
    "avatarUrl": "https://...",
    "level": 5,
    "totalXp": 8000,
    "weeklyXp": 2000
  }
]
排名計算可在 SQL 或 Service 層完成。
SQL 可用 ORDER BY weekly_xp DESC LIMIT :limit。

4. 前端整合：/leaderboard 頁面
檔案位置：frontend/app/(dashboard)/leaderboard/page.tsx

目前已存在頁面與 UI，但使用 mock 資料。

目標：改為呼叫前端 API Route → 後端排行榜 API。

4.1 前端 API Routes
新增兩個 Next.js API routes：

frontend/app/api/leaderboard/total/route.ts

呼叫後端 GET /api/leaderboard/total

轉發 JWT

frontend/app/api/leaderboard/weekly/route.ts

呼叫後端 GET /api/leaderboard/weekly

轉發 JWT

4.2 頁面邏輯
頁面維持兩個 Tab：

學習排行榜

本週成長榜

Tab 切換時呼叫對應 API 或預先載入兩份資料。

渲染時使用：

data-testid="leaderboard-tab-total"

data-testid="leaderboard-tab-weekly"

每列：

data-testid="leaderboard-row"

名稱：data-testid="leaderboard-user-name"

XP：data-testid="leaderboard-user-xp"

排名顯示：

第 1 名：金牌圖示

第 2 名：銀牌

第 3 名：銅牌

其他顯示數字排名即可

5. BDD 規格
Feature: 學習排行榜 API
gherkin
複製程式碼
Feature: 學習排行榜 API

  @R1 @leaderboard @total
  Scenario: 查詢總 XP 排行榜
    Given 系統中存在多位使用者，且各自有 totalXp
    And 我已成功登入並取得 JWT
    When 我呼叫 API GET /api/leaderboard/total
    Then 我應該得到 200 回應
    And 回應應該是一個陣列
    And 陣列中的每一筆資料都應該包含 displayName, level, totalXp
    And 陣列應該依照 totalXp 由大到小排序
Feature: 本週成長榜 API
gherkin
複製程式碼
Feature: 本週成長榜 API

  @R1 @leaderboard @weekly
  Scenario: 查詢本週 XP 排行榜
    Given 系統中存在多位使用者，且各自有 weeklyXp
    And 我已成功登入並取得 JWT
    When 我呼叫 API GET /api/leaderboard/weekly
    Then 我應該得到 200 回應
    And 回應應該是一個陣列
    And 陣列中的每一筆資料都應該包含 displayName, level, weeklyXp
    And 陣列應該依照 weeklyXp 由大到小排序
Feature: 完成單元後排行榜更新（整合 Unit & XP）
gherkin
複製程式碼
Feature: 完成單元後排行榜更新

  @R1 @leaderboard @xp
  Scenario: 完成單元後本週 XP 排行中的數值增加
    Given 我已使用 dev 一鍵登入成功
    And 我目前在本週排行榜中的 weeklyXp 為 W
    When 我完成一個影片單元（呼叫 POST /api/units/{unitId}/complete）
    And 再次呼叫 GET /api/leaderboard/weekly
    Then 回應中我這個使用者的 weeklyXp 應該大於 W
6. 實作備註
請在查詢排行榜時，避免回傳敏感欄位（例如 email）。

若使用者很多，R1 暫時可只取前 50 名，不做分頁。

若無任何使用者有 XP，請回傳空陣列，前端需處理空狀態 UI。

所有計算與查詢程式碼請加上繁體中文註解，方便後續維護。