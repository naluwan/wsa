目的：
本文件定義 R1 階段必須實作的「單元、XP、等級」系統完整規格，Claude Code 必須依照本文件自動產生後端、前端、migration 與 BDD。

0. 目的（Purpose）

本規格定義：

Unit（單元）資料模型

UserUnitProgress（使用者單元進度）

XP 系統

等級系統

完成單元 API

前端單元頁 /units/[unitId]

PostgreSQL migration

Spring Boot Service / Controller

Next.js Pages + API Routes

BDD 流程（AI x BDD）

Claude Code 必須完整遵守本規格。

1. 範圍（Scope）
1.1 本規格涵蓋

Unit 資料表

使用者單元完成紀錄

XP 與等級邏輯

R1 需用到的 API

前端單元頁邏輯

BDD

1.2 不在範圍內

測驗型單元（Quiz）

文章型單元

作業上傳

道館挑戰

獎勵任務

真正的影片播放偵測（R1 只需「標記為完成」按鈕）

2. Domain Model
2.1 Unit（單元）
欄位
欄位	型別	說明
id	UUID	主鍵
unit_id	VARCHAR	對外 ID，用於 URL
course_id	UUID	所屬課程（外鍵）
title	VARCHAR	單元標題
type	VARCHAR	R1 固定為 video
order_index	INT	單元排序
video_url	TEXT	影片網址
xp_reward	INT	完成後獲得 XP（預設：100）
created_at	timestamp	建立時間
updated_at	timestamp	更新時間
2.2 PostgreSQL：units 資料表（Migration）
CREATE TABLE units (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  unit_id VARCHAR(100) NOT NULL UNIQUE,
  course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  title VARCHAR(255) NOT NULL,
  type VARCHAR(50) NOT NULL,
  order_index INTEGER NOT NULL DEFAULT 0,
  video_url TEXT,
  xp_reward INTEGER NOT NULL DEFAULT 100,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);


Migration 檔名建議：

V3__Create_units_table.sql

2.3 UserUnitProgress（使用者單元完成紀錄）

用來記錄使用者完成哪些單元（避免重複加 XP）。

資料表
CREATE TABLE user_unit_progress (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  unit_id UUID NOT NULL REFERENCES units(id) ON DELETE CASCADE,
  completed_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE(user_id, unit_id)
);


Migration 檔名建議：

V4__Create_user_unit_progress_table.sql

3. XP 與等級邏輯
3.1 使用者 XP 欄位（已存在 users 表）

total_xp：總 XP

weekly_xp：本週 XP

level：當前等級

3.2 等級表（必須完全照此表實作）
等級	累積經驗值（>=）	升級所需經驗值
1	0	200
2	200	300
3	500	1000
4	1500	1500
5	3000	2000
6	5000	2000
7	7000	2000
8	9000	2000
9	11000	2000
10	13000	2000
11	15000	2000
12	17000	2000
13	19000	2000
14	21000	2000
15	23000	2000
16	25000	2000
17	27000	2000
18	29000	2000
19	31000	2000
20	33000	2000
21	35000	2000
22	37000	2000
23	39000	2000
24	41000	2000
25	43000	2000
26	45000	2000
27	47000	2000
28	49000	2000
29	51000	2000
30	53000	2000
31	55000	2000
32	57000	2000
33	59000	2000
34	61000	2000
35	63000	2000
36	65000	—
3.3 等級計算策略（backend 必須實作）

給定 total_xp：

找出累積 XP >= total_xp 的最大等級

更新 users.level

例如：

total_xp = 520 → level 3

4. API 規格（後端）
4.1 取得課程單元列表
GET /api/courses/{courseCode}/units
需要登入


回傳：

[
  {
    "id": "uuid",
    "unitId": "intro-design-principles",
    "title": "設計原則導論",
    "type": "video",
    "orderIndex": 1,
    "isCompleted": false
  }
]

4.2 取得單元詳情
GET /api/units/{unitId}
需要登入


回傳：

{
  "id": "uuid",
  "unitId": "intro-design-principles",
  "courseCode": "SOFTWARE_DESIGN_PATTERN",
  "title": "設計原則導論",
  "type": "video",
  "orderIndex": 1,
  "videoUrl": "https://example.com/video.mp4",
  "xpReward": 200,
  "isCompleted": false
}

4.3 完成單元
POST /api/units/{unitId}/complete
需要登入


回傳：

{
  "user": {
    "id": "uuid",
    "level": 2,
    "totalXp": 250,
    "weeklyXp": 250
  },
  "unit": {
    "unitId": "intro-design-principles",
    "isCompleted": true
  }
}

5. 前端需求（Next.js）
5.1 /courses/[courseCode] 單元列表頁

呼叫 /api/courses/{courseCode}/units

顯示單元列表

每個單元連結 /units/[unitId]

5.2 /units/[unitId] 單元頁
需呈現：

單元標題

<video> 播放器（需含 data-testid="unit-video"）

「標記為完成」按鈕（需含 data-testid="complete-unit-button"）

行為：

載入頁面 → 呼叫 /api/units/{unitId}

按下「標記為完成」→ 呼叫 POST 完成 API

顯示 toast：「已完成單元，獲得 XXX XP」

更新 Header 的 XP（重新呼叫 /api/auth/me）

6. BDD（AI x BDD）
6.1 後端 BDD
Feature: 單元完成後獲得 XP

  @R1 @unit @xp
  Scenario: 完成影片單元後 XP 增加
    Given 我已使用 dev 一鍵登入成功
    And 系統中存在一個影片單元 "intro-design-principles" 屬於課程 "SOFTWARE_DESIGN_PATTERN"
    And 我目前的 totalXp 為 X
    When 我呼叫 POST /api/units/intro-design-principles/complete
    Then 伺服器應回傳 200
    And user.totalXp 應大於 X
    And unit.isCompleted 應為 true

6.2 前端 BDD
Feature: 前端完成單元流程

  @R1 @frontend @unit
  Scenario: 使用者按下「標記為完成」按鈕後完成單元
    Given 我已登入系統
    And 我正在瀏覽 "/units/intro-design-principles"
    And 我看到影片播放器 data-testid="unit-video"
    And 我看到按鈕 data-testid="complete-unit-button"
    When 我點擊 data-testid="complete-unit-button"
    Then 系統應呼叫完成單元 API
    And 應顯示「已完成單元」toast

7. 實作備註（Claude Code 必須遵守）

user_unit_progress 必須 UNIQUE(user_id, unit_id)

等級計算 MUST 使用表格

XP/Level 計算 MUST 封裝在 Service

API 必須符合上述欄位

前端不可使用 mock

所有後端程式碼必須加入繁體中文註解