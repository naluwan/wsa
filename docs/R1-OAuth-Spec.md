你是一個全端強化工具，請根據以下要求建立 / 修改程式碼。
請你絕對不要自行猜測 domain，不要做我沒有說的功能。

🚧（1）本次任務摘要（自然語言）

目標：建立 R1 用的「真實 Google OAuth Login 流程」，技術棧為：

Frontend：Next.js (App Router, TypeScript)

Backend：Spring Boot

DB：PostgreSQL

Devops：docker-compose

此次任務包含：

建立 Spring Boot /api/auth/oauth-login API

建立 JWT Service、UserService

建立必要的 Entity / Repository

建立 Next.js OAuth callback handler

建立 Login UI 頁面

把 JWT 存入 httpOnly cookie

導覽列顯示登入後的頭像與使用者名稱

所有相關檔案和資料夾請依照最佳實務自動建立

請確保：

維持乾淨分層架構

API 路徑需與我指定的完全一致

DTO 命名需一致且不可自行變動

使用 Flyway 建立 migrations

程式碼要拆分成多個檔案，而不是寫在一個大檔案裡

📐（2）資料表 / Entities（請依照以下 Schema 建立）
users
- id (uuid)
- external_id (varchar)
- provider (varchar) -- 'google' | 'facebook'
- display_name (varchar)
- email (varchar)
- avatar_url (varchar)
- level (int)
- total_xp (int)
- weekly_xp (int)
- created_at (timestamp)
- updated_at (timestamp)


請為以上欄位建立：

Entity

Repository

Flyway migration

🔌（3）API 規格（請完全按照以下格式實作）
POST /api/auth/oauth-login

Request:

{
  "provider": "google",
  "externalId": "123456",
  "email": "xxx@gmail.com",
  "displayName": "Ender",
  "avatarUrl": "https://xxxx"
}


Response:

{
  "token": "JWT_STRING",
  "user": {
    "id": "...",
    "displayName": "...",
    "email": "...",
    "avatarUrl": "...",
    "level": 1,
    "totalXp": 0,
    "weeklyXp": 0
  }
}

🔒（4）JWT 要求

JWT 演算法：HS256

token 內需包含：userId

建立以下類別：

JwtService

JwtFilter

SecurityConfig（設定驗證 /api/**）

後端 API 使用 header：

Authorization: Bearer <token>

🌐（5）Next.js OAuth callback handler

請新增以下檔案：

/app/api/auth/google/callback/route.ts
/app/api/auth/facebook/callback/route.ts


流程需包含：

使用 code 交換 Google/Facebook Token

取得 email/name/avatar（基本用戶資料）

POST 到 /api/auth/oauth-login

後端回傳 JWT → 存到 httpOnly cookie

redirect("/")

🎨（6）Next.js UI 要求

請建立登入頁：

/app/(auth)/login/page.tsx


內容：

顯示「使用 Google 登入」「使用 Facebook 登入」按鈕

點 Google → 導向 Google OAuth URL

請在 layout 中提供 Navbar：
若尚未登入 → 顯示「登入」
若已登入 → 顯示 avatar + 使用者名稱

🧪（7）BDD（繁體中文版本）
Feature: OAuth 登入流程

  Scenario: 使用者以 Google 登入
    Given 我位於登入頁面
    When 我點擊「使用 Google 登入」
    And Google 將我導回平台並附帶授權碼（code）
    Then 系統應使用該授權碼與 Google 交換使用者資料
    And 系統應根據該資料建立或登入使用者帳號，並設定 session cookie
    And 我應該在導覽列看到我的頭像與名稱

🛠（8）請輸出結果格式

請依序輸出：

所有新增檔案清單（含路徑）

每個檔案的程式碼

docker-compose 要如何啟動

如何測試 OAuth 登入流程



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