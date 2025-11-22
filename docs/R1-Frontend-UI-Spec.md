# 前端 UI 規格（Frontend UI Specification）

本文件定義復刻 水球軟體學院 LMS（https://world.waterballsa.tw）
 的前端 UI 行為、結構、元件規範。
所有前端畫面必須符合以下規格，並使用 Next.js App Router + Tailwind CSS + shadcn/ui 完成。

## 🎨 1. 全站統一規格
技術棧

Next.js 14（App Router）

TypeScript

Tailwind CSS

shadcn/ui（最新版本）

shadcn 要求

所有主要 UI 元件必須來自 shadcn/ui：

NavigationMenu

DropdownMenu

Avatar

AvatarImage

Button

Card

Tabs

Table

Badge

Progress

Separator

Dialog（可選）

Toast

## 🧱 2. 全站 Layout 需求（layout.tsx）

前端整體需使用一致 Layout：

Header（固定在上方）
└─ 左邊：Logo（點擊回首頁）
└─ 中間：導覽列
└─ 右邊：主題切換 + 使用者頭像（未登入顯示「登入」）

Main Content（下方）
Footer（最底部）

導覽列項目（需依登入狀態變化）
✔ 未登入訪客

首頁

課程

排行榜

所有單元

SOP 寶典

登入（按鈕）

✔ 已登入使用者

首頁

課程

排行榜

所有單元

SOP 寶典

右上角頭像（DropdownMenu）

個人檔案

挑戰歷程

獎勵任務

邀請好友

登出

## 🏠 3. 首頁（Home Page）
/app/page.tsx

主要結構
第一區：Hero 區塊

使用 shadcn card + button

顯示兩個主要課程（假資料亦可）

課程名稱

描述

進入課程 button

第二區：資訊卡片（四張）

內容：

卡片	內容
1	排行榜介紹 → 連到 leaderboard
2	道館挑戰介紹（R3）
3	升級系統介紹
4	SOP 寶典

使用 shadcn：

card

button

grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4

第三區：Footer

包含：

Logo

免責說明

技術支援方式

## 📚 4. 課程頁（Courses）
/app/courses/page.tsx

UI 結構
課程卡片：

課程封面（可用 placeholder）

課程名稱

課程摘要

進度條（shadcn Progress）

「進入課程」 button

使用 shadcn：

card

progress

badge

## 🎒 5. 課程詳頁（Journey）
/app/journeys/[courseCode]/page.tsx

UI 區塊
上方：課程概覽

課程標題

描述

進度百分比

顯示用戶當前 XP/Level（可選）

中間：章節／單元（Episode / Unit）

使用 shadcn：

accordion

card

Unit 欄目格式：

單元 icon（video/article/form）

單元名稱

完成狀態 badge

點擊後跳至單元頁

## 🎬 6. 單元頁（Unit Detail Page）
/app/units/[unitId]/page.tsx

UI 結構：
上方（影片播放區）

<video> tag 或 YouTube iframe

單元標題

下方（互動）

影片完成後顯示「完成單元」按鈕

完成後顯示 XP 獎勵 toast

自動更新進度

使用 shadcn：

button

toast

alert

## 🏆 7. 排行榜頁（Leaderboard）
/app/leaderboard/page.tsx


使用 shadcn：

tabs

table

badge

avatar

兩個 Tab：

學習排行榜（依 totalXp 排序）

本週成長榜（依 weeklyXp 排序）

表格欄位
欄位	說明
排名	前三名用金銀銅圖示
頭像	使用 avatar
名稱	使用者 displayName
等級	使用 badge
XP	數字展示
## 👤 8. 個人檔案頁（Profile）
/app/profile/page.tsx

概覽資訊：

頭像

顯示名稱

等級

XP 統計（R1 可先不做圖表）

使用 shadcn：

card

tabs

avatar

button

## 🧭 9. Sidebar / Dropdown / Theme Toggle
必須用 shadcn：

dropdown-menu

navigation-menu

mode-toggle

## 🔄 10. 遷移到 shadcn 的元件規範

所有 UI 元件應建立在：

/frontend/components/ui/


透過：

npx shadcn-ui add button
npx shadcn-ui add card
npx shadcn-ui add tabs
...

## 🧪 11. 前端 UI BDD（自動化可用）
Feature: 首頁

  Scenario: 訪客瀏覽首頁
    Given 我尚未登入
    When 我開啟首頁
    Then 我應看到導覽列：首頁、課程、排行榜、所有單元、SOP 寶典、登入
    And 我應看到 2 張主要課程卡片
    And 我應看到 4 張資訊卡片

Feature: 課程詳頁

  Scenario: 學員查看課程結構
    Given 我已登入
    When 我開啟某課程頁
    Then 我應看到課程標題
    And 我應看到章節列表
    And 每個章節下應包含其單元列表

Feature: 排行榜

  Scenario: 切換排行榜 Tab
    Given 我在排行榜頁
    When 我切換至「本週成長榜」
    Then 表格應顯示 weeklyXp 排序結果

## 📘 12. 前端檔案架構建議
/frontend
  /app
    layout.tsx
    page.tsx（首頁）
    /courses
    /journeys/[courseCode]
    /units/[unitId]
    /leaderboard
    /profile
  /components
    /ui (shadcn)
  /lib
    api.ts
    constants.ts
  /styles
    globals.css

📄（新增）程式碼產出規範：中文註解與中文說明文件

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