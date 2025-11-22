# R1 身分整合與個人檔案規格（Identity & Profile Spec）

> 目標：在兩天內完成「學員登入後，整個站內都顯示他真實的個人資料」，去除現有 UI 中與使用者相關的假資料，統一透過後端 API 取得真實資料。  
> 範圍只專注在「使用者身分與個人資訊」，不含課程/單元/XP 流程。

---

## 1. R1 目標與範圍

### 1.1 本次 R1 想達成的體驗（給 PM Demo 用）

- 學員可以使用 **Google 或 Facebook 真實登入**。
- 登入後，頁首的頭像、名稱、等級、經驗值顯示為「真實資料」，不再是假資料。
- 進入「個人檔案」頁面時，看到的也是同一份真實資料。
- 登出後，畫面回到未登入狀態，頭像與個人資訊消失。
- 目前沒有課程購買與上課流程，R1 僅專注在「身份整合」。

### 1.2 本次 R1 不做的事情（刻意延後）

- 不實作課程/單元資料表與相關 API。
- 不實作課程購買流程。
- 不實作 XP 計算規則、升級邏輯（暫時固定為預設值）。
- 不實作排行榜後端邏輯（仍可先用 mock 顯示 demo）。

---

## 2. 目前已完成（供你參考，不要重做）

> ⚠ 以下內容僅為現況描述，**請勿重構或大量修改，除非本規格有明確要求**。

- OAuth 登入流程（Google / Facebook）已完成：
  - 前端 `frontend/app/api/auth/google/callback/route.ts`
  - 前端 `frontend/app/api/auth/facebook/callback/route.ts`
  - 後端 `AuthController` 的 `/api/auth/oauth-login`
  - 後端 `JwtService` / `JwtFilter` / `SecurityConfig`
- users 資料表已完成，欄位包含：
  - id, email, display_name, avatar_url, provider, external_id
  - level, total_xp, weekly_xp, created_at, updated_at
- `/api/user/me` 後端 endpoint 已存在。
- 前端 UI 架構（Header / Sidebar / Layout / Login page / Profile page / Leaderboard 等）已存在，但：
  - 個人檔案頁 `profile/page.tsx` 目前使用假資料。
  - Header dropdown 顯示的等級/XP 目前為手動計算或假資料。

---

## 3. 後端規格（Backend Spec）

### 3.1 使用者資料來源與欄位映射

#### 3.1.1 OAuth Provider → User Entity 映射規則

**Google**

- `email` → `User.email`
- `name` 或 `given_name + family_name` → `User.displayName`
- `picture` → `User.avatarUrl`
- `sub` → `User.externalId`
- provider 固定為 `"google"`

**Facebook**

- `email` → `User.email`（可能為 null，需允許）
- `name` → `User.displayName`
- `picture.data.url` → `User.avatarUrl`
- `id` → `User.externalId`
- provider 固定為 `"facebook"`

> 要求：如果某欄位取得不到（例如 Facebook 沒回 email），後端要用合理的預設值處理（例如 displayName 保留、email 可為 null），不可噴例外而讓登入失敗。

---

### 3.2 `/api/auth/oauth-login` 行為補強

現有功能維持不變，但需要補上：

1. **當使用者首次以某 provider + externalId 登入時：**
   - 建立新 User：
     - `email` / `displayName` / `avatarUrl` 來自 OAuth profile
     - `level = 1`
     - `totalXp = 0`
     - `weeklyXp = 0`
   - 設定 `created_at` / `updated_at`。

2. **當使用者再次以同一 provider + externalId 登入時：**
   - 讀取既有 User
   - 更新：`email` / `displayName` / `avatarUrl`（避免舊資料）
   - 更新 `updated_at`。

3. 回傳 payload（若已有實作，請確認與下方一致或向下相容）：

```json
{
  "token": "JWT_STRING",
  "user": {
    "id": "UUID",
    "displayName": "Ender",
    "email": "xxx@gmail.com",
    "avatarUrl": "https://xxx",
    "level": 1,
    "totalXp": 0,
    "weeklyXp": 0
  }
}
⚠ 注意：此端點仍屬「公開端點」，不需要 JWT 驗證。

3.3 /api/user/me API 最終規格
請將 /api/user/me 統一為以下格式，前端之後一律吃這個 schema。

Endpoint
Method：GET

Path：/api/user/me

認證：需要 JWT，透過 Authorization: Bearer <token> 驗證。

Response 200 OK
json
複製程式碼
{
  "id": "UUID",
  "displayName": "Ender",
  "email": "xxx@gmail.com",
  "avatarUrl": "https://xxx",
  "level": 1,
  "totalXp": 0,
  "weeklyXp": 0,
  "provider": "google"
}
要求：

如無法找到該使用者（token userId 無對應資料），請回 404 或 401，並以繁體中文註解清楚說明。

DTO 應與內部 Entity 分離，避免直接暴露 Entity。

3.4 後端註解與說明文件
註解規範
所有與本規格相關之 Java 檔案（Controller / Service / DTO / Entity）需加入繁體中文註解，說明：

類別用途

方法用途、參數說明、回傳內容

特別是：

OAuth 登入流程

/api/user/me 的資料來源與錯誤處理

User 建立/更新邏輯

說明文件
請新增：

docs/R1-Identity-Implementation-Guide.md

內容包含（全部繁體中文）：

R1 的目標簡述（專注在使用者身分整合）。

OAuth 登入 → 後端 /api/auth/oauth-login → JWT → /api/user/me 的完整流程圖或文字說明。

User 資料表中與本次相關欄位的用途與注意事項。

對 /api/auth/oauth-login 與 /api/user/me 的 API 說明（Request/Response 與可能錯誤狀況）。

4. 前端規格（Frontend Spec）
4.1 資料取得策略
前端一律不要再手動寫假資料（mock），只要牽涉到「目前登入使用者資訊」的地方，都要改成：

從 httpOnly cookie 內的 JWT 透過 server-side/route API 取得 /api/user/me。

若無 token 或後端回傳 401/404 → 視為未登入狀態。

現有已存在的：

frontend/app/api/auth/me/route.ts

請確認或改為：

/app/api/auth/me/route.ts
向後端 GET /api/user/me 發送請求。

從 cookie 讀取 JWT，附加在 Authorization header。

若成功：回傳 user JSON。

若失敗：

若為 401/404 → 回傳 { user: null }

其他錯誤 → log 並回傳 { user: null, error: '...' }。

4.2 Header（Site Header）更新規格
檔案：frontend/components/site-header.tsx（名稱以實際為準）

行為要求
未登入狀態：

顯示「登入」按鈕（連到 /login）。

不顯示頭像與個人資訊。

已登入狀態：

顯示使用者頭像（從 user.avatarUrl）。

Dropdown 中顯示：

顯示名稱：user.displayName

電子郵件：user.email（若為 null 則不顯示）

等級：user.level

總經驗值：user.totalXp

本週經驗值：user.weeklyXp

所有這些數值都來自 /api/auth/me（間接後端真資料），不可再硬編。

狀態同步：

登入成功後（callback redirect 回首頁），應在 header 看到最新資訊。

登出後（清除 cookie）→ header 自動顯示未登入狀態。

若現有 header 中有「手動計算等級 / XP」或「假資料」，請全部移除，改為直接使用 API 回傳的數字。

4.3 個人檔案頁 /profile 更新規格
檔案：frontend/app/profile/page.tsx

UI 要求
使用 shadcn card, avatar, badge, separator 等組件。

顯示欄位：

頭像（avatarUrl）

顯示名稱（displayName）

電子郵件（email）

等級（level）

總經驗值（totalXp）

本週經驗值（weeklyXp）

若未登入，顯示：

一個提示卡片：例如「你目前尚未登入，請先登入以查看個人檔案。」

提供「前往登入」按鈕。

資料來源
一律呼叫 /app/api/auth/me 來取得資料（間接打後端 /api/user/me）。

不得保留任何 mock user object。

4.4 登入頁 /login 的補充
目前登入頁已存在，可能不需大改，但請確認：

Google / Facebook 按鈕按下後確實導向對應 OAuth URL。

若已登入（有 valid user），可選擇：

自動 redirect 到首頁，或

顯示「你已登入為 XXX」的提示。

這個部分不是 R1 核心，但請加上簡單判斷即可。

4.5 前端註解規範
所有與本規格相關檔案（header, profile page, auth/me route）需加入繁體中文註解，說明：

元件用途

主要狀態來源（例如「此處的 user 資料來自 /api/auth/me」）

未登入 / 已登入狀態處理方式

不需要每行都註解，但關鍵邏輯與分支需用中文解釋清楚，方便閱讀與日後 demo。

5. BDD（可執行規格 / E2E 參考）
以下 BDD 僅為行為描述，未要求一定要在本 R1 完成自動化測試，但若有餘裕可以對應撰寫 E2E。

gherkin
複製程式碼
Feature: 使用者身分與個人檔案

  Scenario: 已登入使用者看到正確的 Header 資訊
    Given 我已經透過 Google 登入
    When 我回到首頁
    Then 我應該在右上角看到我的頭像與顯示名稱
    And Dropdown 中應顯示我的等級與經驗值（來自後端真實資料）

  Scenario: 已登入使用者查看個人檔案頁
    Given 我已經登入
    When 我打開「個人檔案」頁
    Then 我應該看到與 Header 相同的顯示名稱與頭像
    And 我應該看到我的等級、總經驗值、本週經驗值

  Scenario: 未登入使用者查看個人檔案頁
    Given 我尚未登入
    When 我直接打開「個人檔案」頁
    Then 我應該看到一個提示告訴我尚未登入
    And 我可以點擊「前往登入」按鈕前往登入頁面
6. 請求 Claude Code 的執行指引（給 AI 的 meta 說明）
當你讀取並實作本規格時，請注意：

不要重構整個專案，只在規格中提到的檔案與邏輯範圍內修改。

不要重新設計 Entity / Schema，除非是本規格中明確提到的欄位補充。

任何與「使用者假資料」有關的程式碼，若可以用 /api/user/me 或 /app/api/auth/me 取得真資料，就應該移除假資料並改用真資料。

程式碼完成後，請更新或撰寫 docs/R1-Identity-Implementation-Guide.md，用繁體中文解釋所有實作內容。

## 程式碼產出規範：中文註解與中文說明文件

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