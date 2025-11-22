# R1 課程模組規格書（Courses）

> 目標：  
> 在 R1 階段提供「可被前端讀取的課程列表與課程詳情」，  
> 讓使用者可以：登入 → 看到課程列表 → 點進某門課程 → 看到該課程底下的單元列表（影片單元由 Unit 規格處理）。

---

## 1. 範圍與非範圍（Scope / Out of Scope）

### 1.1 本規格涵蓋內容

- 課程資料模型（Course）
- 課程資料表 migration（PostgreSQL）
- 基本種子資料（至少一門課）
- 後端課程 API：
  - 取得課程列表
  - 取得單一課程詳情（包含單元摘要）
- 前端整合：
  - `/courses` 列表頁改用真實 API（移除 mock）
  - 點擊「進入課程」導向 `/courses/[courseCode]` 單元列表頁

### 1.2 本規格不涵蓋（後續 R2+ or R1.5）

- 課程購買 / 訂單 / 金流（R1.5 再加入）
- 課程可見性（免費 / 付費 / 權限控管）
- 老師後台建立 / 編輯課程
- 道館挑戰（Challenge Journeys）頁面 `/journeys/...`

---

## 2. Domain Model：Course

### 2.1 Course 欄位定義

一門課程（Course）至少包含：

- `id`：UUID
- `code`：課程代碼（例如：`BACKEND_JAVA`, `FRONTEND_REACT`, `SOFTWARE_DESIGN_PATTERN`）
- `title`：課程名稱
- `slug`：路由用代稱（可選，若不需要可略）
- `description`：課程簡介
- `level_tag`：難度標籤（e.g. `beginner`, `intermediate`, `advanced`）
- `total_units`：該課程總單元數（可根據 units 表計算，但 R1 可先存在 course 上）
- `cover_icon`：前端顯示用 icon 名稱或 URL（可先簡化成 enum / string）

### 2.2 PostgreSQL 資料表設計

建立 `courses` 資料表：

```sql
CREATE TABLE courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(100) NOT NULL UNIQUE,       -- 用於路由與識別，例如 BACKEND_JAVA
  title VARCHAR(255) NOT NULL,             -- 課程名稱
  slug VARCHAR(255),                       -- URL Slug（可選）
  description TEXT,                        -- 課程描述
  level_tag VARCHAR(50),                   -- beginner / intermediate / advanced 等
  total_units INTEGER DEFAULT 0 NOT NULL,  -- 單元總數（R1 可先手動維護）
  cover_icon VARCHAR(100),                 -- 前端可用來決定 icon / 圖片
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
實作時請以 Flyway migration 檔建立，檔名例如：
V2__Create_courses_table.sql

2.3 種子資料（Seed）
為了讓前端 /courses 頁面可以展示真實資料，請在 migration 或啟動時加入最少以下課程：

BACKEND_JAVA - Java 後端課程

FRONTEND_REACT - React 前端課程

SOFTWARE_DESIGN_PATTERN - 軟體設計模式精通之旅（對應官方題目）

可用簡單 INSERT：

sql
複製程式碼
INSERT INTO courses (code, title, description, level_tag, total_units, cover_icon)
VALUES
('BACKEND_JAVA', '後端工程實戰：Java 與 Spring Boot', '從零開始學習後端開發，實作 REST API 與資料庫整合。', 'intermediate', 10, 'backend_java'),
('FRONTEND_REACT', '前端工程實戰：React 與現代前端', '掌握 React 與現代前端開發流程，打造高互動 UI。', 'intermediate', 12, 'frontend_react'),
('SOFTWARE_DESIGN_PATTERN', '軟體設計模式精通之旅', '透過實作掌握常見設計模式與設計原則，寫出可維護的程式碼。', 'advanced', 15, 'software_design_pattern');
3. 後端 API 規格（Spring Boot）
所有課程 API 都應使用 JWT 驗證（Authorization Bearer token），
沿用已完成的身份系統。

Base URL 假設為：/api/courses

3.1 取得課程列表
Method：GET /api/courses

說明：取得所有可見課程列表（R1 不分權限，全都可見）

權限：需要登入（JWT）

Request
Header：

Authorization: Bearer <jwt-token>

Response（200 OK）
json
複製程式碼
[
  {
    "id": "uuid",
    "code": "BACKEND_JAVA",
    "title": "後端工程實戰：Java 與 Spring Boot",
    "description": "從零開始學習後端開發，實作 REST API 與資料庫整合。",
    "levelTag": "intermediate",
    "totalUnits": 10,
    "coverIcon": "backend_java"
  },
  {
    "id": "uuid",
    "code": "SOFTWARE_DESIGN_PATTERN",
    "title": "軟體設計模式精通之旅",
    "description": "透過實作掌握常見設計模式與設計原則，寫出可維護的程式碼。",
    "levelTag": "advanced",
    "totalUnits": 15,
    "coverIcon": "software_design_pattern"
  }
]
實作：

建議建立 Course entity / CourseRepository / CourseService / CourseController

使用 DTO 回傳前端需要的欄位。

3.2 取得單一課程詳情（含單元摘要）
Method：GET /api/courses/{courseCode}

說明：取得指定課程的詳細資訊與其單元列表概況（單元詳細在 Unit 規格中定義）

權限：需要登入（JWT）

Request
URL Path：

courseCode 例如：SOFTWARE_DESIGN_PATTERN

Header：

Authorization: Bearer <jwt-token>

Response（200 OK）
json
複製程式碼
{
  "id": "uuid",
  "code": "SOFTWARE_DESIGN_PATTERN",
  "title": "軟體設計模式精通之旅",
  "description": "透過實作掌握常見設計模式與設計原則，寫出可維護的程式碼。",
  "levelTag": "advanced",
  "totalUnits": 15,
  "coverIcon": "software_design_pattern",
  "units": [
    {
      "id": "uuid",
      "unitId": "intro-design-principles",
      "title": "設計原則導論",
      "type": "video",
      "orderIndex": 1
    },
    {
      "id": "uuid",
      "unitId": "solid-overview",
      "title": "SOLID 原則總覽",
      "type": "video",
      "orderIndex": 2
    }
  ]
}
units 的欄位定義請依 R1-Unit-And-XP-Spec.md。
若還沒實作單元表，可以先回傳空陣列，讓前端不會炸掉。

4. 前端整合（Next.js）
4.1 /courses 列表頁（取代 mock 資料）
檔案位置：frontend/app/(dashboard)/courses/page.tsx

目標：

不再使用硬編碼 courses 陣列

改為在 Server Component 中呼叫 /api/courses（Next API Route → 後端）

建議作法
建立前端 API Route：

frontend/app/api/courses/route.ts

用 server-side fetch 呼叫後端 GET /api/courses

從 cookie 中讀取 JWT → 放到 Authorization header

在 courses/page.tsx：

使用 export const dynamic = 'force-dynamic' 避免快取

直接 const res = await fetch("/api/courses") 取得課程列表

渲染 course-card（保持原本 shadcn UI 風格）

每張卡片按鈕的 href 改為：

/courses/${course.code}

仍保留 data-testid：

data-testid="course-card"

data-testid="course-title"

data-testid="enter-course-button"

4.2 /courses/[courseCode] 單元列表頁
新增動態路由：

檔案位置：frontend/app/(dashboard)/courses/[courseCode]/page.tsx

行為：

根據 URL params.courseCode 呼叫前端 API Route：/api/courses/[courseCode]

顯示課程標題、描述

顯示底下的單元列表（Unit 卡片）

每個單元卡片上有「進入單元」按鈕 → 導到 /units/[unitId]

data-testid：

data-testid="unit-card"

data-testid="unit-title"

data-testid="enter-unit-button"

單元頁 /units/[unitId] 的實作請依 R1-Unit-And-XP-Spec.md。

5. BDD（AI x BDD 規格）
Feature: 課程列表
gherkin
複製程式碼
Feature: 課程列表

  @R1 @courses @list
  Scenario: 已登入使用者查詢課程列表
    Given 我已成功登入並取得 JWT
    When 我呼叫 API GET /api/courses
    Then 我應該得到 200 回應
    And 回應內容應該是一個課程陣列
    And 至少包含一門課程 "軟體設計模式精通之旅"
Feature: 課程詳情與單元摘要
gherkin
複製程式碼
Feature: 課程詳情與單元摘要

  @R1 @courses @details
  Scenario: 已登入使用者查詢單一課程詳情
    Given 我已成功登入並取得 JWT
    And 系統中存在課程代碼 "SOFTWARE_DESIGN_PATTERN"
    When 我呼叫 API GET /api/courses/SOFTWARE_DESIGN_PATTERN
    Then 我應該得到 200 回應
    And 回應中的課程名稱應該為 "軟體設計模式精通之旅"
    And 回應中應包含 units 陣列欄位（即使目前為空陣列也可）
6. 實作備註（給 AI / 工程師）
所有程式碼請加上繁體中文註解，說明每個欄位與 API 的用途。

請勿在 R1 實作課程建立 / 更新 / 刪除 API（後台留給 R2）。

courses 資料請以 migration 或啟動 seed 建立，避免前端畫面空白。

課程進度與 XP 不在本文件處理，請交由 R1-Unit-And-XP-Spec.md。