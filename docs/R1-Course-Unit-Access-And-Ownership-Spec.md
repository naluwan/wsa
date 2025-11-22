# R1-Course-Unit-Access-And-Ownership-Spec.md

> 目標（R1.5）：  
> 使用者可以在課程頁完成以下核心體驗：
> - 未登入仍可瀏覽課程清單與課程頁
> - 若想 **試看影片或購買課程** → 會被引導登入
> - 登入後：
>   - 可試看標記為「免費試看」的單元影片
>   - 購買課程後可解鎖所有非免費試看的單元
>   - 點擊單元 → 右側顯示影片或「無法觀看」提示
>   - UI 行為與原平台類似（Accordion＋鎖頭圖示）

> ⚠ 說明  
> - XP / 等級計算仍由 `R1-Unit-And-XP-Spec.md` 負責，本文件只描述 **課程存取權限與擁有狀態**。  
> - 單元完成（交付）時的 XP 邏輯依照 `R1-Unit-And-XP-Spec.md` 實作。

---

## 1. Domain 範圍與用語

### 1.1 範圍

本文件定義：

1. **課程（Course）資料模型與 DB 結構**
2. **單元分組與免費試看（Units + Section 標題 + isFreePreview）**
3. **課程擁有權（Ownership / UserCourse）**
4. **未登入使用者的行為規則**
5. **課程頁 / 單元清單 UI 與 ShadCN Accordion 行為**
6. **Mock 購買流程（不串真金流）**
7. **兩門課程的種子資料（Courses + Units）**

### 1.2 不在本次 R1.5 內（後續可擴充）

- 真實金流串接（藍新、TapPay、信用卡…）
- 優惠碼 / 折扣 / 早鳥方案
- 課程上下架時間、學習期限
- 道館作業、挑戰地圖、獎勵任務
- 完整的課程搜尋與篩選

---

## 2. 資料模型與資料庫結構

> 備註：以下以 PostgreSQL 為主，Migration 以 Flyway 命名。

### 2.1 `courses` – 課程表

> 若已存在 `courses` 表，以下欄位視情況新增 / 對齊。

**資料表名稱**：`courses`  
**Flyway**：`V3__Create_courses_table.sql`（若尚未存在）

```sql
CREATE TABLE courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  course_code VARCHAR(100) NOT NULL UNIQUE,    -- 例如 SOFTWARE_DESIGN_PATTERN, AI_X_BDD
  title VARCHAR(255) NOT NULL,                 -- 課程名稱
  teacher_name VARCHAR(255) NOT NULL,          -- 老師名稱，例如：水球潘
  description TEXT NOT NULL,                   -- 課程描述
  price_twd INTEGER NOT NULL,                  -- 價格（新台幣，單位元），例如 7599
  thumbnail_url TEXT,                          -- 課程封面圖片，例如 images/course_0.png
  is_published BOOLEAN NOT NULL DEFAULT TRUE,  -- 是否已上架
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
2.2 units – 單元表（在原 R1-Unit-And-XP-Spec 基礎上擴充）
在原本 units 表上新增以下欄位：

section_title：用來當 Accordion 的標題（例如「課程介紹 & 視聽(免費試看)」）

is_free_preview：是否為免費試看的單元

order_in_section：在該 section 內的排序

Migration：V4__Alter_units_add_section_and_preview_flags.sql

sql
複製程式碼
ALTER TABLE units
  ADD COLUMN section_title VARCHAR(255) NOT NULL DEFAULT '未分類章節',
  ADD COLUMN is_free_preview BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN order_in_section INTEGER NOT NULL DEFAULT 0;
其他欄位 (course_id, title, video_url, order_index, xp_reward 等) 沿用 R1-Unit-And-XP-Spec.md。

2.3 user_courses – 課程擁有權表
用來紀錄「某位使用者擁有哪些課程」。

資料表名稱：user_courses
Flyway：V5__Create_user_courses_table.sql

sql
複製程式碼
CREATE TABLE user_courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  -- 之後可加 purchase_price, payment_method 等欄位
  purchased_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE(user_id, course_id) -- 一位使用者同一門課只會有一筆擁有紀錄
);
3. 存取權限與行為規則
3.1 課程階段狀態
對於某一 course + 當前登入使用者（可能為未登入），系統會計算：

isOwned：是否已擁有此課程（user_courses 是否有紀錄）

isLoggedIn：是否登入

每一個 unit 的權限：

isFreePreview：units.is_free_preview

canAccess：

若 !isLoggedIn → false（所有單元都需要登入）

若 isOwned → true（所有單元可看）

若 !isOwned && isFreePreview → true（免費試看）

其他情況 → false（顯示鎖頭與「無法觀看」訊息）

3.2 未登入使用者行為
可以看到：

課程列表 /courses

課程詳情頁 /courses/[courseCode]

左側 Accordion 的章節與單元清單

不可以直接：

點擊任何單元觀看影片

點擊「購買課程」按鈕

行為模式：

當未登入使用者點擊 任何單元（包含免費試看）時：

彈出 ShadCN UI AlertDialog：

標題：請先登入

內容：登入後即可試看免費單元與購買課程

按鈕：

取消：關閉 dialog

前往登入：導向 /login?redirect=/courses/[courseCode]?unit=[unitId]

當未登入使用者點擊 購買課程按鈕 時：

同樣彈出上述 AlertDialog

3.3 已登入但尚未購買課程
isLoggedIn = true

isOwned = false

行為：

免費試看的單元：

左側列表 沒有鎖頭 icon

可點擊，點擊後右側會顯示影片播放器與「交付」按鈕（交付邏輯由 Unit & XP 規格處理）

非免費試看的單元：

左側列表顯示鎖頭 icon

仍可被點擊（為了顯示右側提示內容）

點擊後 右側內容顯示（與原站類似）：

您無法觀看「平台使用手冊」
這是購買「軟體設計精通模式之旅」之後才能享有的內容。

此時影片播放器不顯示

交付 按鈕也不顯示

購買課程按鈕：

按鈕文案：購買課程 $7,599

點擊後呼叫 Mock API（見第 4 章）

成功後：

顯示 Toast：購買成功，已解鎖所有單元

重新取得 coursedetail（isOwned = true）

左側所有鎖頭移除

若當前位於被鎖住的單元頁，應自動載入影片播放器

3.4 已登入且已購買課程
isLoggedIn = true

isOwned = true

所有單元 canAccess = true（即使不是免費試看）

左側列表：

不再顯示鎖頭 icon

所有單元皆可點擊

右側內容：

顯示影片播放器

顯示「交付」按鈕（由 Unit & XP 流程控制是否 disable / 顯示）

購買按鈕：

改為狀態顯示，例如：

你已擁有這門課程

按鈕可隱藏或 disabled（由 UI 決定）

4. 後端 API 規格
所有 API 以 /api 開頭，由 Spring Boot 實作。

4.1 取得課程列表
Method：GET /api/courses
權限：公共（不需登入）

Response 範例：

json
複製程式碼
[
  {
    "courseCode": "SOFTWARE_DESIGN_PATTERN",
    "title": "軟體設計精通模式之旅",
    "teacherName": "水球潘",
    "description": "用一趟旅程的時間，成為硬核的 Coding 實戰高手",
    "priceTwd": 7599,
    "thumbnailUrl": "images/course_0.png",
    "isOwned": false
  },
  {
    "courseCode": "AI_X_BDD",
    "title": "AI x BDD：規格驅動全自動開發術",
    "teacherName": "水球潘",
    "description": "AI Top 1% 工程師必修課，掌握規格驅動的全自動化開發",
    "priceTwd": 7599,
    "thumbnailUrl": "images/course_1.png",
    "isOwned": false
  }
]
若使用者已登入，isOwned 依當前 user 計算；未登入則一律 false。

4.2 取得課程詳情＋單元清單＋存取權限
Method：GET /api/courses/{courseCode}
權限：公共（未登入也可呼叫，但 isOwned = false，所有單元 canAccess = false）

Response 範例（已登入使用者，尚未購買課程）：

json
複製程式碼
{
  "course": {
    "courseCode": "SOFTWARE_DESIGN_PATTERN",
    "title": "軟體設計精通模式之旅",
    "teacherName": "水球潘",
    "description": "用一趟旅程的時間，成為硬核的 Coding 實戰高手",
    "priceTwd": 7599,
    "thumbnailUrl": "images/course_0.png",
    "isOwned": false
  },
  "sections": [
    {
      "sectionTitle": "課程介紹 & 視聽(免費試看)",
      "units": [
        {
          "unitId": "sdp-intro-course-overview",
          "title": "課程介紹：這門課手把手帶你成為架構設計的高手",
          "orderInSection": 1,
          "isFreePreview": true,
          "canAccess": true
        },
        {
          "unitId": "sdp-intro-ai-era",
          "title": "你該知道：在 AI 的時代下，只會下 prompt 絕對寫不出好 Code",
          "orderInSection": 2,
          "isFreePreview": true,
          "canAccess": true
        }
      ]
    },
    {
      "sectionTitle": "副本零：冒險者指引",
      "units": [
        {
          "unitId": "sdp-guide-platform-manual",
          "title": "平台使用手冊",
          "orderInSection": 1,
          "isFreePreview": false,
          "canAccess": false
        },
        {
          "unitId": "sdp-guide-astah-pro",
          "title": "如何使用課程贊助給大家的專業 UML Editor — Astah Pro？",
          "orderInSection": 2,
          "isFreePreview": false,
          "canAccess": false
        }
      ]
    }
  ]
}
canAccess 由後端根據登入狀態、擁有狀態、is_free_preview 計算。
前端無須再自行判斷，只需依 canAccess 決定是否顯示鎖頭與影片。

4.3 取得單一單元詳情（含 access 判斷結果）
Method：GET /api/units/{unitId}
權限：可公共呼叫，但若 canAccess = false，不回傳 videoUrl。

Response（可觀看時）：

json
複製程式碼
{
  "unitId": "ai-bdd-intro-course-overview",
  "courseCode": "AI_X_BDD",
  "courseTitle": "AI x BDD：規格驅動全自動開發術",
  "sectionTitle": "課程介紹 & 視聽",
  "title": "課程介紹：一次到位的 AI Coding，全台最高 CP 值且野心最大的規格驅動開發線上課程",
  "type": "video",
  "videoUrl": "https://youtu.be/W09vydJH6jo?si=WKym-tH448dhsNu1",
  "xpReward": 200,
  "isFreePreview": true,
  "canAccess": true
}
Response（不可觀看時）：

json
複製程式碼
{
  "unitId": "sdp-guide-platform-manual",
  "courseCode": "SOFTWARE_DESIGN_PATTERN",
  "courseTitle": "軟體設計精通模式之旅",
  "sectionTitle": "副本零：冒險者指引",
  "title": "平台使用手冊",
  "type": "video",
  "isFreePreview": false,
  "canAccess": false
}
當 canAccess = false 時，videoUrl 不回傳，前端只顯示鎖定訊息。

4.4 Mock 購買課程 API
Method：POST /api/courses/{courseCode}/purchase/mock
權限：必須登入

行為：

從 JWT 取得 userId

以 courseCode 查詢 courses 取得 courseId

檢查 user_courses 是否已有 (userId, courseId)：

若已存在：直接回傳 200 與 isOwned = true

若尚未存在：新增一筆 user_courses 記錄

回傳最新的課程擁有狀態與課程資訊

Response：

json
複製程式碼
{
  "course": {
    "courseCode": "SOFTWARE_DESIGN_PATTERN",
    "title": "軟體設計精通模式之旅",
    "isOwned": true
  }
}
5. 前端行為與 UI 規格
前端為 Next.js App Router + ShadCN UI。

5.1 課程詳細頁 /courses/[courseCode]
檔案：frontend/app/(dashboard)/courses/[courseCode]/page.tsx

5.1.1 資料取得
以 Server Component / Route Handler 呼叫：

GET /api/courses/{courseCode}

若 URL 中帶有 ?unit={unitId}：

前端在渲染時將該 unit 設為「目前選取的單元」

5.1.2 版面布局
左側：寬度約 w-80 的欄位，使用 Accordion 顯示章節與單元

右側：主要內容區，顯示：

若 currentUnit.canAccess = true：

單元標題

所屬課程名稱

<video> 或嵌入 YouTube player（可先用 <iframe>）

「交付」按鈕（由 Unit & XP 規格負責行為）

若 currentUnit.canAccess = false：

文字：

您無法觀看「{unitTitle}」
這是購買「{courseTitle}」之後才能享有的內容。

5.1.3 ShadCN Accordion 行為
左側清單使用：

tsx
複製程式碼
import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from "@/components/ui/accordion"
結構：

每一個 section 對應一個 AccordionItem

AccordionTrigger 顯示 sectionTitle

AccordionContent 內是該 section 的 units 列表

每個 unit 行顯示：

標題文字

若 !unit.isFreePreview && !isOwned → 在標題左側顯示鎖頭 Icon（lucide-react Lock）

點擊整行觸發 onSelectUnit(unit.unitId)

5.1.4 單元點擊行為
若 !isLoggedIn：

彈出 ShadCN AlertDialog：

標題：請先登入

內容：登入後即可試看免費單元與購買課程

確認按鈕：前往登入

導到 /login?redirect=/courses/[courseCode]?unit=[unitId]

取消按鈕：關閉 dialog

若 isLoggedIn：

先更新 URL query ?unit={unitId}

呼叫 GET /api/units/{unitId} 取得單元詳情

若 canAccess = true：

右側顯示影片播放器與交付按鈕

若 canAccess = false：

右側顯示鎖定訊息

5.2 購買課程按鈕行為
出現在課程頁右上或課程摘要區域。

未登入：

點擊時與單元點擊相同，彈出「請先登入」AlertDialog

已登入且未購買：

點擊 → 呼叫 POST /api/courses/{courseCode}/purchase/mock

成功：

顯示 Toast：購買成功，已解鎖所有單元

重新 fetch GET /api/courses/{courseCode}

更新 isOwned = true，鎖頭 icon 移除

若當前單元 canAccess 由 false → true，應重新載入單元內容

已登入且已購買：

按鈕顯示灰色狀態，例如：你已擁有這門課程

點擊不觸發任何動作

6. BDD 規格（AI x BDD）
6.1 未登入點擊單元被要求登入
gherkin
複製程式碼
Feature: 未登入使用者點擊課程單元需要先登入

  @R1_5 @course @access
  Scenario: 未登入使用者在課程頁點擊單元
    Given 我尚未登入
    And 我在瀏覽 "/courses/SOFTWARE_DESIGN_PATTERN" 課程頁
    When 我點擊左側「課程介紹 & 視聽(免費試看)」下的單元「課程介紹：這門課手把手帶你成為架構設計的高手」
    Then 應顯示一個提示框要求我先登入
    And 提示框中應有「前往登入」按鈕
    And 當我按下「前往登入」時
    Then 應導向到 "/login?redirect=/courses/SOFTWARE_DESIGN_PATTERN?unit=sdp-intro-course-overview"
6.2 已登入但尚未購買時，免費試看單元可觀看
gherkin
複製程式碼
Feature: 已登入但尚未購買課程時可以觀看免費試看單元

  @R1_5 @course @preview
  Scenario: 查看免費試看單元
    Given 我已使用 dev 一鍵登入成功
    And 我尚未購買課程 "SOFTWARE_DESIGN_PATTERN"
    And 我在瀏覽 "/courses/SOFTWARE_DESIGN_PATTERN" 課程頁
    When 我點擊「課程介紹 & 視聽(免費試看)」下的單元「課程介紹：這門課手把手帶你成為架構設計的高手」
    Then 右側應顯示影片播放器
    And 該單元的 "canAccess" 應為 true
6.3 已登入但尚未購買時，非免費單元被鎖住
gherkin
複製程式碼
Feature: 已登入但尚未購買課程時無法觀看鎖住的單元

  @R1_5 @course @locked
  Scenario: 嘗試觀看非免費試看單元
    Given 我已使用 dev 一鍵登入成功
    And 我尚未購買課程 "SOFTWARE_DESIGN_PATTERN"
    And 我在瀏覽 "/courses/SOFTWARE_DESIGN_PATTERN" 課程頁
    When 我點擊「副本零：冒險者指引」下的單元「平台使用手冊」
    Then 右側應顯示文字：
      """
      您無法觀看「平台使用手冊」
      這是購買「軟體設計精通模式之旅」之後才能享有的內容。
      """
    And 不應顯示影片播放器
6.4 購買課程後解鎖所有單元
gherkin
複製程式碼
Feature: 購買課程後解鎖所有單元

  @R1_5 @course @purchase
  Scenario: 使用 mock 購買 API 解鎖課程
    Given 我已使用 dev 一鍵登入成功
    And 我尚未購買課程 "AI_X_BDD"
    When 我在課程頁上點擊「購買課程」按鈕
    Then 系統應呼叫 POST /api/courses/AI_X_BDD/purchase/mock
    And 回傳中的 course.isOwned 應為 true
    And 再次載入課程資料時，所有單元的 canAccess 應為 true
7. 種子資料（Seed Data）
建議使用 Flyway V6__Seed_courses_and_units.sql 來建立初始課程與單元。

7.1 課程種子 – courses
sql
複製程式碼
-- 課程 1：軟體設計精通模式之旅
INSERT INTO courses (course_code, title, teacher_name, description, price_twd, thumbnail_url)
VALUES (
  'SOFTWARE_DESIGN_PATTERN',
  '軟體設計精通模式之旅',
  '水球潘',
  '用一趟旅程的時間，成為硬核的 Coding 實戰高手',
  7599,
  'images/course_0.png'
);

-- 課程 2：AI x BDD：規格驅動全自動開發術
INSERT INTO courses (course_code, title, teacher_name, description, price_twd, thumbnail_url)
VALUES (
  'AI_X_BDD',
  'AI x BDD：規格驅動全自動開發術',
  '水球潘',
  'AI Top 1% 工程師必修課，掌握規格驅動的全自動化開發',
  7599,
  'images/course_1.png'
);
若 thumbnail_url 尚未準備，可暫時為 NULL 或使用相同圖片。

7.2 單元種子 – units
透過子查詢取得對應 course_id。
xp_reward 先統一給 200，方便測試升級邏輯。

sql
複製程式碼
-- 課程 1：軟體設計精通模式之旅
-- 章節一：課程介紹 & 視聽(免費試看)

INSERT INTO units (
  unit_id, course_id, section_title, title,
  type, order_index, order_in_section,
  video_url, xp_reward, is_free_preview
)
VALUES
(
  'sdp-intro-course-overview',
  (SELECT id FROM courses WHERE course_code = 'SOFTWARE_DESIGN_PATTERN'),
  '課程介紹 & 視聽(免費試看)',
  '課程介紹：這門課手把手帶你成為架構設計的高手',
  'video',
  1, 1,
  'https://youtu.be/3GxftuDUBXM?si=Ke5fSlV8pmwqqJVD',
  200,
  TRUE
),
(
  'sdp-intro-ai-era',
  (SELECT id FROM courses WHERE course_code = 'SOFTWARE_DESIGN_PATTERN'),
  '課程介紹 & 視聽(免費試看)',
  '你該知道：在 AI 的時代下，只會下 prompt 絕對寫不出好 Code',
  'video',
  2, 2,
  'https://youtu.be/UslcIlL-1xo?si=DdVMTdriDkEeqqKO',
  200,
  TRUE
);

-- 章節二：副本零：冒險者指引

INSERT INTO units (
  unit_id, course_id, section_title, title,
  type, order_index, order_in_section,
  video_url, xp_reward, is_free_preview
)
VALUES
(
  'sdp-guide-platform-manual',
  (SELECT id FROM courses WHERE course_code = 'SOFTWARE_DESIGN_PATTERN'),
  '副本零：冒險者指引',
  '平台使用手冊',
  'video',
  3, 1,
  NULL,          -- 之後可補上真實影片連結
  200,
  FALSE          -- 非免費試看，需購買課程才可觀看
),
(
  'sdp-guide-astah-pro',
  (SELECT id FROM courses WHERE course_code = 'SOFTWARE_DESIGN_PATTERN'),
  '副本零：冒險者指引',
  '如何使用課程贊助給大家的專業 UML Editor — Astah Pro？',
  'video',
  4, 2,
  NULL,
  200,
  FALSE
);

-- 課程 2：AI x BDD：規格驅動全自動開發術
-- 章節一：課程介紹 & 視聽

INSERT INTO units (
  unit_id, course_id, section_title, title,
  type, order_index, order_in_section,
  video_url, xp_reward, is_free_preview
)
VALUES
(
  'ai-bdd-intro-course-overview',
  (SELECT id FROM courses WHERE course_code = 'AI_X_BDD'),
  '課程介紹 & 視聽',
  '課程介紹：一次到位的 AI Coding，全台最高 CP 值且野心最大的規格驅動開發線上課程',
  'video',
  1, 1,
  'https://youtu.be/W09vydJH6jo?si=WKym-tH448dhsNu1',
  200,
  FALSE
),
(
  'ai-bdd-intro-strategy-tactics',
  (SELECT id FROM courses WHERE course_code = 'AI_X_BDD'),
  '課程介紹 & 視聽',
  '你該知道：戰略戰術設計模式到底追求的是什麼？該怎麼理解它們？',
  'video',
  2, 2,
  'https://youtu.be/mOJzH0U_3EU?si=fLlbrk1413SAtCX-',
  200,
  FALSE
);
8. 實作備註
後端：

建議建立 CourseService、CourseAccessService，負責 canAccess 判斷與 isOwned 計算。

API 回傳結果務必避免洩漏 videoUrl 給 canAccess = false 的使用者。

前端：

所有關於「能不能看」的判斷，優先以後端回傳的 canAccess 為準。

AlertDialog、Accordion 等請完全使用 ShadCN 官方組件。

文件與註解：

後端 service 與 controller 中，對於存取邏輯請用 繁體中文註解 說明。

這一份 spec 檔案放在 docs/R1-Course-Unit-Access-And-Ownership-Spec.md，讓 Claude Code 持續參考。

程式碼產出規範：中文註解與中文說明文件

請在本次任務中所有產出的程式碼遵守以下要求：

✅ 1. 所有 Java（Spring Boot）程式碼都要加入繁體中文註解

包含：

class 上方加入用途說明（繁體中文）

function 上方加入用途、參數說明、回傳值說明（繁體中文）

重要邏輯（JWT、OAuth callback、DB transaction）要加「解釋性的繁體中文註解」

若有例外處理，也需加中文說明

若有商業邏輯（例如 XP 計算、等級更新），需加中文註解

✅ 2. 所有 TypeScript（Next.js）程式碼，要用繁體中文註解解釋邏輯

包含：

page.tsx → 頁面功能中文解釋

route.ts → API handler 功能中文解釋

hooks → 清楚中文說明

API client → 每個 function 加中文解釋

❗ 註解風格要求：

不要加過度冗長的註解（保持清晰易讀）

避免逐行翻譯，要解釋「邏輯與目的」

📄（新增）說明文件產出規範

請在本次任務的最後輸出一份 Markdown 文件：

docs/R1-OAuth-Implementation-Guide.md


文件內容包含：

必含內容：

系統整體 OAuth 流程說明（繁體中文）

後端 API 說明（每個 API 的用途、參數、回傳格式、注意事項）

後端 Services 說明（包含 JwtService、UserService、AuthService 等）

前端流程說明（login → OAuth → callback → 後端 → cookie → redirect）

資料表（Entity）說明

錯誤處理與例外狀況說明（像 provider 錯誤、無效 code 等）

後續擴充建議（可選）

文件語言要求：

全部使用 繁體中文

技術名詞如 JWT、OAuth、middleware 可保留英文