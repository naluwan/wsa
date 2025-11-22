# R1-Journey-Player-Page-Spec

> 目的：  
> 實作「課程學習頁」，左側為單元清單（Accordion），右側為影片播放器或鎖定提示。  
> 支援：試看單元、未購買鎖定提示、完成單元與 XP 更新。  
> Claude Code 必須依照本規格實作前端頁面與必要的 API 串接（不可再用 mock 資料）。

---

## 0. 範圍（Scope）

### 0.1 本規格涵蓋

- 「課程學習頁」主畫面 Layout
- 左側：單元清單（使用 shadcn UI Accordion）
- 右側：影片播放器 / 鎖定提示內容
- 試看邏輯（免費試看單元）
- 已購買 / 未購買的觀看權限判斷（依照 `R1-Course-Unit-Access-And-Ownership-Spec.md`）
- 「交付 / 標記為完成」按鈕 → 呼叫完成單元 API、取得 XP / 等級更新
- 未登入時的 Alert Dialog 提示登入

### 0.2 不在本規格內（可以之後 R1.5 / R2 再做）

- 真正的「影片播放完才算 100%」的嚴格驗證（R1 可以只靠前端時間判斷）
- 影片觀看進度（看到第幾秒）寫入資料庫與續看（可以在 R1.5 依此頁再擴充）
- 多種單元型別（測驗、作業等）

---

## 1. 頁面總覽（Journey Player Page）

### 1.1 路由名稱（抽象）

> 這裡**不強制指定 URL**，但概念上此頁會接收：
>
> - `courseCode`（例如：`SOFTWARE_DESIGN_PATTERN`）
> - `unitId` 或類似識別（例如：`intro-1`）

可能的路由示意（僅供 Claude 參考，可依專案實作）：

- `/journeys/[courseCode]/missions/[unitId]`

### 1.2 版面結構

整體畫面拆成三層：

1. **最外層 Layout（JourneyPlayerLayout）**
   - 左：單元清單區（寬度固定，例如 320px）
   - 右：內容顯示區（剩餘全部寬度）
2. **左側：課程 + 單元清單**
   - 顯示課程名稱
   - 使用 shadcn UI `Accordion` 顯示「章節（Chapter）」與其底下的單元列表
   - 單元前方依狀態顯示 icon：
     - ✅ 可看影片：一般播放 icon
     - 🔓 免費試看：標示「試看」
     - 🔒 未購買且非試看：lock icon
3. **右側：當前單元內容**
   - 若使用者有權限觀看：顯示影片 + 交付按鈕
   - 若無權限：顯示「您無法觀看『XXX』」鎖定訊息

---

## 2. 資料與 API 依賴

本頁面**不得使用假資料**，一律透過後端 API 取得。

### 2.1 依賴的後端 API（已在其他規格定義）

Claude Code 必須串接以下 API（實際 URL 依專案定義）：

1. `GET /api/auth/me`
   - 用來判斷是否登入，取得目前使用者資訊與擁有課程列表
2. `GET /api/courses/{courseCode}/units`
   - 回傳此課程下所有單元清單（包含章節資訊、是否免費試看、是否已完成、是否可看）
3. `GET /api/units/{unitId}`
   - 回傳「單一單元詳情」
   - 必須包含欄位：
     - `title`
     - `courseTitle`
     - `courseCode`
     - `videoUrl`
     - `xpReward`
     - `isCompleted`
     - `isFreePreview`
     - `canAccess`（此使用者是否可看：已購買或免費試看）
4. `POST /api/units/{unitId}/complete`
   - 標記單元完成，並根據 `R1-Unit-And-XP-Spec.md` 更新 XP / Level
   - 回傳新的 user 狀態（`level`, `totalXp`, `weeklyXp`）

---

## 3. 左側：單元清單（Accordion）

### 3.1 UI 行為

- 使用 shadcn UI 的 `Accordion` 組件。
- 每個「章節」對應一個 Accordion item，標題顯示章節名稱，例如：
  - `課程導覽：讓同學手把手帶你深入架構設計的奇妙世界`
  - `副本零：冒險者指引`
- 章節展開時，顯示該章節底下所有單元（Mission）。
- 單元項目呈現：
  - 左側 icon：
    - 免費試看：小「播放三角形」＋「試看」標籤
    - 可觀看（已購買）：播放 icon
    - 鎖定：lock icon
  - 中間：單元標題文字
  - 右側：
    - 若已完成：✅ 或 `已完成` Tag
- 點擊一個單元項目：
  - 若使用者**未登入** → 顯示 Alert Dialog 提示登入（見 5.1）
  - 若登入但 **`unit.canAccess = false`** → 右側顯示鎖定訊息（見 4.2），仍可切換選取狀態
  - 若登入且 `unit.canAccess = true` → 右側切換到該影片單元內容

### 3.2 資料結構（前端）

`GET /api/courses/{courseCode}/units` 建議回傳格式（略）：

```json
{
  "course": {
    "code": "SOFTWARE_DESIGN_PATTERN",
    "title": "軟體設計模式精通之旅"
  },
  "chapters": [
    {
      "id": "chapter-1",
      "title": "課程導覽：讓同學手把手帶你深入架構設計的奇妙世界",
      "units": [
        {
          "unitId": "intro-1",
          "title": "課程介紹：這門課手把手帶你成為架構設計的高手",
          "isFreePreview": true,
          "isCompleted": false,
          "canAccess": true
        },
        {
          "unitId": "intro-2",
          "title": "你該知道：在 AI 的時代下，只會下 prompt 絕對寫不出好 Code",
          "isFreePreview": true,
          "isCompleted": false,
          "canAccess": true
        }
      ]
    },
    {
      "id": "chapter-2",
      "title": "副本零：冒險者指引",
      "units": [
        {
          "unitId": "guide-1",
          "title": "平台使用手冊",
          "isFreePreview": false,
          "isCompleted": false,
          "canAccess": false
        }
      ]
    }
  ]
}
Claude Code 可以依現有 DB 設計做對應，不要求欄位完全一致，但語意要一致。

4. 右側：單元內容區
4.1 有權限觀看（canAccess = true）
畫面構成：

上方提示條（可簡化）

例如復刻站那句話：「將此體驗課程的全部影片看完並交付即可獲得 3000 元課程折價券！」

R1 可以先寫死文案或從課程 API 帶入。

影片播放器區

使用 <video> 元素或你們現有 Player component。

屬性：

controls

src = unit.videoUrl

data-testid="unit-video"

影片寬度限制在中央，例如 max-w-5xl。

R1 不需真的跟 YouTube Player API 整合，可先用 <iframe> or 直接 videoUrl 播放，Claude 視情況實作。

交付 / 標記完成按鈕

放在影片下方右側。

使用 shadcn Button：

文案：交付 或 標記為完成

data-testid="complete-unit-button"

按下行為：

呼叫 POST /api/units/{unitId}/complete

成功後：

顯示 toast：「已完成單元，獲得 XXX XP」

更新 header 上的 XP / 等級顯示（重新呼叫 /api/auth/me）

更新左側單元清單的 isCompleted

R1 簡化：按鈕一開始就可以按，不需要真的等影片播完。

若你想在 R1.5 再加「影片播放到 100% 才顯示按鈕」，可以在這份 spec 裡額外加一段備註，但目前 R1 先用「隨時可按」的模式。

4.2 無權限觀看（canAccess = false）
畫面構成：

右側不顯示影片，只顯示鎖定提示（類似參考站）：

標題：您無法觀看「{unit.title}」

描述：這是課程「{courseTitle}」之後才能享有的內容。

一個 CTA 按鈕：前往課程頁購買 或 查看方案

點擊導往 /courses 或該課程的購買頁。

若未登入，也可以在這裡再顯示一個 登入後繼續 按鈕（視設計而定）。

5. 登入相關行為
5.1 未登入 → Alert Dialog
情境：

使用者在課程列表頁按「試聽課程」

或在課程學習頁的左側單元清單點任何單元

若 GET /api/auth/me 回傳 user = null，則：

不直接跳頁

先顯示 shadcn AlertDialog：

標題：需要先登入

內容：請先登入帳號才能試看課程或觀看單元內容。

按鈕：

取消：稍後再說

主要：前往登入

點「前往登入」：

導向 /login，並在 query string 帶上 redirectTo（目前所在頁 URL）

登入成功後，由登入流程將使用者導回 redirectTo

5.2 已登入但未購買
左側單元清單仍顯示所有單元，但非 free preview 且未擁有的單元顯示鎖 icon。

點擊鎖定單元 → 右側顯示「無法觀看」畫面（4.2）

6. 範例：兩門課的單元結構（對照你提供的需求）
以下只是一份 前端與種子資料對照表，實際 seed 可交給 Claude 依 DB 模型實作。

6.1 課程 1：軟體設計精通模式之旅
課程代碼（示例）：SOFTWARE_DESIGN_PATTERN

老師：水球潘

描述：用一趟旅程的時間，成為硬核的 Coding 實戰高手

價格：7599

圖片：images/course_0.png

章節與單元（左側 Accordion）

章節：課程介紹 & 視聽（免費試看）

單元 1（free, 可看）

unitId：sdp-intro-1

標題：課程介紹：這門課手把手帶你成為架構設計的高手

videoUrl：https://youtu.be/3GxftuDUBXM?si=Ke5fSlV8pmwqqJVD

isFreePreview：true

單元 2（free, 可看）

unitId：sdp-intro-2

標題：你該知道：在 AI 的時代下，只會下 prompt 絕對寫不出好 Code

videoUrl：https://youtu.be/UslcIlL-1xo?si=DdVMTdriDkEeqqKO

isFreePreview：true

章節：副本零：冒險者指引

單元 3（非試看，未購買時鎖定）

unitId：sdp-guide-1

標題：平台使用手冊

單元 4（非試看，未購買時鎖定）

unitId：sdp-guide-2

標題：如何使用課程贊助給大家的專業 UML Editor — Astah Pro？

isFreePreview = false 的單元，在未擁有課程時 canAccess = false，顯示鎖 icon。

6.2 課程 2：AI x BDD：規格驅動全自動開發術
課程代碼（示例）：AI_BDD

老師：水球潘

描述：AI Top 1% 工程師必修課，掌握規格驅動的全自動化開發

價格：7599

章節與單元

章節：課程介紹 & 視聽

單元 1（可設為 free preview，方便體驗）

unitId：ai-bdd-intro-1

標題：課程介紹：一次到位的 AI Coding，全台最高 CP 值且野心最大的規格驅動開發線上課程

videoUrl：https://youtu.be/W09vydJH6jo?si=WKym-tH448dhsNu1

單元 2（可自由決定是否 free）

unitId：ai-bdd-intro-2

標題：你該知道：戰略戰術設計模式到底追求的是什麼？該怎麼理解它們？

videoUrl：https://youtu.be/mOJzH0U_3EU?si=fLlbrk1413SAtCX-

7. BDD（可用於 E2E / Playwright）
7.1 前端：課程學習頁基本流程
gherkin
複製程式碼
Feature: 課程學習頁 - 試看與鎖定邏輯

  @R1 @frontend @journey
  Scenario: 已登入使用者試看免費單元
    Given 我已使用 dev 一鍵登入成功
    And 我擁有課程 "SOFTWARE_DESIGN_PATTERN" 的試看權限
    When 我在課程列表點擊「試聽課程」
    Then 我應該被導向課程學習頁
    And 左側應顯示單元清單 Accordion
    And 右側應顯示影片播放器 data-testid="unit-video"
    And 我應看到「標記為完成」按鈕 data-testid="complete-unit-button"

  @R1 @frontend @journey
  Scenario: 已登入但尚未購買課程，嘗試點擊鎖定單元
    Given 我已使用 dev 一鍵登入成功
    And 我尚未購買課程 "SOFTWARE_DESIGN_PATTERN"
    And 左側存在一個單元 "平台使用手冊" 顯示鎖 icon
    When 我點擊「平台使用手冊」單元
    Then 右側應顯示鎖定訊息
    And 文案包含 "您無法觀看「平台使用手冊」"
7.2 未登入 → Alert Dialog
gherkin
複製程式碼
Feature: 未登入使用者點擊試聽課程

  @R1 @frontend @auth
  Scenario: 未登入點擊試聽課程會被要求登入
    Given 我尚未登入
    And 我在課程列表頁 "/courses"
    When 我點擊「試聽課程」按鈕
    Then 應跳出 Alert Dialog 提示需要登入
    And 當我點擊「前往登入」按鈕時
    Then 應導向 "/login" 並帶有 redirectTo 參數
8. 實作注意事項（給 Claude Code）
不得使用假資料

單元清單、單元權限、課程資訊都要從後端 API 取得。

UI 要使用 shadcn UI + Tailwind

Accordion、Button、AlertDialog、ScrollArea 等都用 shadcn。

權限邏輯以後端欄位為準

canAccess, isFreePreview, isOwned 等由後端決定，前端只負責依狀態顯示。

所有關鍵元件加上 data-testid

unit-video

complete-unit-button

Alert Dialog 的登入按鈕也可加 data-testid，方便 E2E。

程式碼請加繁體中文註解

說明每一段邏輯是在處理什麼（特別是：權限判斷、交付流程）。