# R1 OAuth 實作指南

## 📋 目錄

1. [系統概述](#系統概述)
2. [OAuth 登入流程說明](#oauth-登入流程說明)
3. [後端架構](#後端架構)
4. [前端架構](#前端架構)
5. [資料表設計](#資料表設計)
6. [API 規格](#api-規格)
7. [錯誤處理](#錯誤處理)
8. [安全性考量](#安全性考量)
9. [後續擴充建議](#後續擴充建議)

---

## 系統概述

本系統實作了完整的 OAuth 2.0 登入流程，支援 Google 和 Facebook 兩種 OAuth 提供者。使用者可以透過這些第三方平台進行登入，系統會自動建立或更新使用者帳號，並使用 JWT（JSON Web Token）進行後續的身份驗證。

### 技術棧

- **前端**: Next.js 14 (App Router, TypeScript)
- **後端**: Spring Boot 3.2 (Java 17)
- **資料庫**: PostgreSQL 15
- **認證機制**: JWT (HS256)
- **OAuth 提供者**: Google OAuth 2.0, Facebook Login

### 核心功能

1. **OAuth 登入**: 使用者透過 Google 或 Facebook 登入
2. **JWT 認證**: 使用 JWT token 進行 API 認證
3. **自動帳號管理**: 自動建立新使用者或更新現有使用者資料
4. **httpOnly Cookie**: 安全地儲存 JWT token
5. **使用者狀態顯示**: Navbar 顯示使用者頭像和名稱

---

## OAuth 登入流程說明

### 完整流程圖

```
[使用者] → [登入頁面] → [選擇 OAuth 提供者]
    ↓
[導向 Google/Facebook 授權頁面]
    ↓
[使用者授權]
    ↓
[OAuth 提供者導回應用程式，附帶授權碼 code]
    ↓
[前端 Callback Handler]
    ├─ 使用 code 交換 access token
    ├─ 使用 access token 取得使用者資料
    └─ 將使用者資料 POST 到後端 /api/auth/oauth-login
    ↓
[後端處理]
    ├─ 檢查使用者是否已存在（依 provider + externalId）
    ├─ 若存在：更新使用者資料
    ├─ 若不存在：建立新使用者（初始 level=1, xp=0）
    └─ 產生 JWT token
    ↓
[前端接收回應]
    ├─ 將 JWT token 存入 httpOnly cookie
    └─ 導向首頁
    ↓
[Navbar 顯示]
    └─ 從 cookie 讀取 token，向後端取得使用者資料，顯示頭像和名稱
```

### 詳細步驟說明

#### 步驟 1: 使用者點擊登入按鈕

使用者在登入頁面點擊「使用 Google 登入」或「使用 Facebook 登入」按鈕。

**檔案**: `frontend/app/(auth)/login/page.tsx`

- Google 授權 URL: `https://accounts.google.com/o/oauth2/v2/auth`
- Facebook 授權 URL: `https://www.facebook.com/v18.0/dialog/oauth`

#### 步驟 2: 導向 OAuth 提供者

瀏覽器導向 OAuth 提供者的授權頁面，URL 中包含：
- `client_id`: OAuth 應用程式 ID
- `redirect_uri`: 授權後的回調 URL
- `scope`: 請求的權限範圍
- `response_type`: 固定為 `code`

#### 步驟 3: 使用者授權

使用者在 OAuth 提供者頁面登入並授權應用程式存取基本資料。

#### 步驟 4: OAuth 提供者導回應用程式

授權成功後，OAuth 提供者將使用者導回應用程式的 callback URL，並在 query string 中附帶授權碼 `code`。

**Google 回調 URL**: `http://localhost:3000/api/auth/google/callback?code=xxx`
**Facebook 回調 URL**: `http://localhost:3000/api/auth/facebook/callback?code=xxx`

#### 步驟 5: 前端 Callback Handler 處理

**檔案**:
- `frontend/app/api/auth/google/callback/route.ts`
- `frontend/app/api/auth/facebook/callback/route.ts`

1. **交換 Access Token**: 使用授權碼向 OAuth 提供者的 token endpoint 交換 access token
2. **取得使用者資料**: 使用 access token 向 OAuth 提供者的 userinfo endpoint 取得使用者基本資料
3. **傳送到後端**: 將使用者資料 POST 到後端 `/api/auth/oauth-login`
4. **儲存 Token**: 將後端回傳的 JWT token 存入 httpOnly cookie
5. **導向首頁**: 重新導向使用者到首頁

#### 步驟 6: 後端處理登入

**檔案**: `backend/src/main/java/com/wsa/controller/AuthController.java`

1. **檢查使用者**: 使用 `provider` 和 `externalId` 查詢使用者是否已存在
2. **建立或更新**:
   - 若存在：更新 `displayName`、`email`、`avatarUrl`
   - 若不存在：建立新使用者，初始 `level=1`、`totalXp=0`、`weeklyXp=0`
3. **產生 JWT**: 使用 JwtService 產生 JWT token，包含使用者 ID
4. **回傳資料**: 回傳 token 和使用者基本資料給前端

#### 步驟 7: 顯示使用者資訊

**檔案**: `frontend/components/Navbar.tsx`

1. **讀取 Token**: 從 cookie 讀取 JWT token
2. **請求使用者資料**: 向 `/api/auth/me` 請求當前使用者資料
3. **顯示資訊**: 在 Navbar 顯示使用者頭像和名稱

---

## 後端架構

### 專案結構

```
backend/src/main/java/com/wsa/
├── WsaApplication.java         # Spring Boot 應用程式進入點
├── config/
│   └── SecurityConfig.java     # Spring Security 設定
├── controller/
│   ├── AuthController.java     # 認證相關 API
│   └── UserController.java     # 使用者相關 API
├── dto/
│   ├── OAuthLoginRequest.java  # OAuth 登入請求 DTO
│   ├── OAuthLoginResponse.java # OAuth 登入回應 DTO
│   └── UserDto.java            # 使用者資料 DTO
├── entity/
│   └── User.java               # 使用者實體
├── filter/
│   └── JwtFilter.java          # JWT 認證過濾器
├── repository/
│   └── UserRepository.java     # 使用者資料存取
└── service/
    ├── JwtService.java         # JWT 服務
    └── UserService.java        # 使用者服務
```

### 核心元件說明

#### 1. SecurityConfig（安全設定）

**檔案**: `backend/src/main/java/com/wsa/config/SecurityConfig.java`

**功能**:
- 設定 Spring Security 過濾器鏈
- 啟用 CORS（允許前端 http://localhost:3000 存取）
- 停用 CSRF（使用 JWT 不需要 CSRF 保護）
- 設定請求授權規則：
  - `/api/auth/**`: 允許所有人存取（登入 API）
  - `/api/**`: 需要認證
- 加入 JwtFilter 到過濾器鏈

**關鍵設定**:
- Session 管理: STATELESS（不使用 server-side session）
- CORS: 允許 `http://localhost:3000`
- CSRF: 停用

#### 2. JwtService（JWT 服務）

**檔案**: `backend/src/main/java/com/wsa/service/JwtService.java`

**功能**:
- 產生 JWT token
- 驗證 JWT token
- 從 token 中提取使用者 ID

**關鍵方法**:

```java
// 產生 JWT token，將使用者 ID 存入 subject
public String generateToken(UUID userId)

// 驗證 token 有效性（檢查簽章、過期時間）
public boolean validateToken(String token)

// 從 token 中提取使用者 ID
public UUID getUserIdFromToken(String token)
```

**JWT 設定**:
- 演算法: HS256
- 密鑰: 從 `application.yml` 讀取 `jwt.secret`
- 過期時間: 從 `application.yml` 讀取 `jwt.expiration`（預設 24 小時）
- Payload: subject 欄位儲存使用者 ID

#### 3. JwtFilter（JWT 過濾器）

**檔案**: `backend/src/main/java/com/wsa/filter/JwtFilter.java`

**功能**:
- 攔截所有 HTTP 請求
- 從 `Authorization` header 提取 JWT token
- 驗證 token 有效性
- 若有效，將使用者 ID 設定到 Spring Security Context

**處理流程**:
1. 從 header 讀取 `Authorization: Bearer <token>`
2. 驗證 token
3. 提取使用者 ID
4. 建立 `Authentication` 物件並設定到 `SecurityContextHolder`
5. 後續的 Controller 可透過 `Authentication` 參數取得使用者 ID

#### 4. UserService（使用者服務）

**檔案**: `backend/src/main/java/com/wsa/service/UserService.java`

**功能**:
- 建立或更新使用者

**關鍵方法**:

```java
@Transactional
public User createOrUpdateUser(OAuthLoginRequest request)
```

**業務邏輯**:
1. 根據 `provider` 和 `externalId` 查詢使用者
2. 若存在：更新 `displayName`、`email`、`avatarUrl`
3. 若不存在：建立新使用者
   - 初始 `level = 1`
   - 初始 `totalXp = 0`
   - 初始 `weeklyXp = 0`

**為什麼要更新現有使用者**:
使用者可能更改 Google/Facebook 的顯示名稱或頭像，每次登入時更新可確保資料是最新的。

---

## 前端架構

### 專案結構

```
frontend/
├── app/
│   ├── (auth)/
│   │   └── login/
│   │       └── page.tsx                # 登入頁面
│   ├── api/
│   │   └── auth/
│   │       ├── google/callback/
│   │       │   └── route.ts            # Google callback
│   │       ├── facebook/callback/
│   │       │   └── route.ts            # Facebook callback
│   │       └── me/
│   │           └── route.ts            # 取得當前使用者 API
│   ├── layout.tsx                      # Root Layout
│   ├── page.tsx                        # 首頁
│   └── globals.css                     # 全域樣式
└── components/
    └── Navbar.tsx                      # 導覽列元件
```

### 核心元件說明

#### 1. 登入頁面

**檔案**: `frontend/app/(auth)/login/page.tsx`

**功能**:
- 顯示 Google 和 Facebook 登入按鈕
- 建立 OAuth 授權 URL 並導向

**Google 授權 URL 參數**:
- `client_id`: Google Client ID
- `redirect_uri`: `http://localhost:3000/api/auth/google/callback`
- `response_type`: `code`
- `scope`: `openid email profile`

**Facebook 授權 URL 參數**:
- `client_id`: Facebook App ID
- `redirect_uri`: `http://localhost:3000/api/auth/facebook/callback`
- `scope`: `email,public_profile`

#### 2. OAuth Callback Handlers

**檔案**:
- `frontend/app/api/auth/google/callback/route.ts`
- `frontend/app/api/auth/facebook/callback/route.ts`

**Google Callback 流程**:
1. 接收授權碼 `code`
2. 向 `https://oauth2.googleapis.com/token` 交換 access token
3. 向 `https://www.googleapis.com/oauth2/v2/userinfo` 取得使用者資料
4. POST 到後端 `/api/auth/oauth-login`
5. 將 JWT token 存入 httpOnly cookie
6. 導向首頁

**Facebook Callback 流程**:
1. 接收授權碼 `code`
2. 向 `https://graph.facebook.com/v18.0/oauth/access_token` 交換 access token
3. 向 `https://graph.facebook.com/me` 取得使用者資料
4. POST 到後端 `/api/auth/oauth-login`
5. 將 JWT token 存入 httpOnly cookie
6. 導向首頁

**httpOnly Cookie 設定**:
```typescript
cookieStore.set("token", loginData.token, {
  httpOnly: true,        // 防止 XSS 攻擊
  secure: process.env.NODE_ENV === "production",  // 正式環境使用 HTTPS
  sameSite: "lax",       // 防止 CSRF 攻擊
  maxAge: 60 * 60 * 24 * 7,  // 7 天過期
  path: "/",
});
```

#### 3. Auth Me API

**檔案**: `frontend/app/api/auth/me/route.ts`

**功能**:
- 從 httpOnly cookie 讀取 JWT token
- 向後端 `/api/user/me` 請求當前使用者資料
- 回傳使用者資料給前端

**使用場景**:
- Navbar 元件取得使用者資料
- 其他需要顯示使用者資訊的頁面

**為什麼需要這個 API**:
由於 token 儲存在 httpOnly cookie 中，前端 JavaScript 無法直接讀取。因此需要透過 server-side API（Next.js Route Handler）來讀取 cookie 並向後端請求資料。

#### 4. Navbar 元件

**檔案**: `frontend/components/Navbar.tsx`

**功能**:
- 顯示應用程式標題
- 顯示使用者登入狀態
  - 未登入：顯示「登入」按鈕
  - 已登入：顯示使用者頭像和名稱

**實作細節**:
- 使用 `useEffect` 在元件掛載時取得使用者資料
- 使用 `useState` 管理使用者狀態和載入狀態
- 向 `/api/auth/me` 請求當前使用者資料

---

## 資料表設計

### users 表

| 欄位名稱 | 資料型別 | 限制 | 說明 |
|---------|---------|------|------|
| id | UUID | PRIMARY KEY | 使用者唯一識別碼 |
| external_id | VARCHAR(255) | NOT NULL | OAuth 提供者的使用者 ID |
| provider | VARCHAR(50) | NOT NULL | OAuth 提供者（google 或 facebook） |
| display_name | VARCHAR(255) | NOT NULL | 使用者顯示名稱 |
| email | VARCHAR(255) | NOT NULL | 使用者電子郵件 |
| avatar_url | VARCHAR(500) | NULL | 使用者頭像 URL |
| level | INTEGER | NOT NULL, DEFAULT 1 | 使用者等級 |
| total_xp | INTEGER | NOT NULL, DEFAULT 0 | 使用者總經驗值 |
| weekly_xp | INTEGER | NOT NULL, DEFAULT 0 | 使用者本週經驗值 |
| created_at | TIMESTAMP | NOT NULL | 建立時間（自動產生） |
| updated_at | TIMESTAMP | NOT NULL | 更新時間（自動更新） |

### 索引

```sql
-- 唯一索引：確保同一 OAuth 提供者的使用者不會重複
CREATE UNIQUE INDEX unique_provider_external_id ON users(provider, external_id);

-- 一般索引：加速 email 查詢
CREATE INDEX idx_users_email ON users(email);

-- 一般索引：加速 provider + external_id 查詢
CREATE INDEX idx_users_provider_external_id ON users(provider, external_id);
```

### 重要欄位說明

#### provider 和 external_id

這兩個欄位組合成唯一識別，用於判斷使用者是否已透過特定 OAuth 提供者註冊。

**為什麼需要這兩個欄位**:
- 同一使用者可能使用不同 OAuth 提供者登入（例如同時用 Google 和 Facebook）
- 使用 `(provider, external_id)` 唯一索引可確保不會重複建立帳號

**範例**:
- 使用者用 Google 登入：`provider=google`, `external_id=123456`
- 同一使用者用 Facebook 登入：`provider=facebook`, `external_id=789012`

#### level, total_xp, weekly_xp

這些欄位為後續遊戲化功能預留，目前實作中：
- 新使用者預設 `level=1`, `total_xp=0`, `weekly_xp=0`
- 可在後續開發中加入經驗值累積、等級提升等功能

---

## API 規格

### 1. OAuth 登入 API

**端點**: `POST /api/auth/oauth-login`

**說明**: 處理 OAuth 登入，建立或更新使用者，並回傳 JWT token

**請求 Header**:
```
Content-Type: application/json
```

**請求 Body**:
```json
{
  "provider": "google",
  "externalId": "123456",
  "email": "user@gmail.com",
  "displayName": "使用者名稱",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**欄位說明**:
- `provider` (string, required): OAuth 提供者，可能值: `google`, `facebook`
- `externalId` (string, required): OAuth 提供者的使用者 ID
- `email` (string, required): 使用者電子郵件
- `displayName` (string, required): 使用者顯示名稱
- `avatarUrl` (string, optional): 使用者頭像 URL

**成功回應** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "displayName": "使用者名稱",
    "email": "user@gmail.com",
    "avatarUrl": "https://example.com/avatar.jpg",
    "level": 1,
    "totalXp": 0,
    "weeklyXp": 0
  }
}
```

**錯誤回應**:
- `400 Bad Request`: 請求格式錯誤或缺少必要欄位
- `500 Internal Server Error`: 伺服器內部錯誤

**注意事項**:
- 此 API 不需要認證（任何人都可以呼叫）
- 前端 callback handler 會呼叫此 API
- 若使用者已存在（相同 provider 和 externalId），會更新使用者資料
- 若使用者不存在，會建立新使用者

### 2. 取得當前使用者 API

**端點**: `GET /api/user/me`

**說明**: 取得當前登入使用者的資料

**請求 Header**:
```
Authorization: Bearer <JWT_TOKEN>
```

**成功回應** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "displayName": "使用者名稱",
  "email": "user@gmail.com",
  "avatarUrl": "https://example.com/avatar.jpg",
  "level": 1,
  "totalXp": 0,
  "weeklyXp": 0
}
```

**錯誤回應**:
- `401 Unauthorized`: 未提供 JWT token 或 token 無效/過期
- `404 Not Found`: 使用者不存在
- `500 Internal Server Error`: 伺服器內部錯誤

**注意事項**:
- 此 API 需要認證（必須提供有效的 JWT token）
- JwtFilter 會從 `Authorization` header 提取 token 並驗證
- 使用者 ID 從 token 中提取
- 前端 `/api/auth/me` Route Handler 會呼叫此 API

---

## 錯誤處理

### 前端錯誤處理

#### 1. OAuth Callback 錯誤

**可能錯誤情境**:
- 授權碼無效或過期
- 無法交換 access token
- 無法取得使用者資料
- 後端登入 API 失敗

**處理方式**:
```typescript
try {
  // OAuth 流程處理
} catch (error) {
  console.error('OAuth callback error:', error);
  return NextResponse.redirect(new URL('/login?error=oauth_failed', request.url));
}
```

**使用者體驗**:
- 發生錯誤時，導回登入頁面
- URL 中帶有 `error=oauth_failed` 參數
- 登入頁面可根據此參數顯示錯誤訊息（目前未實作）

#### 2. 未提供授權碼

**情境**: 使用者拒絕授權或 OAuth 提供者未回傳授權碼

**處理方式**:
```typescript
if (!code) {
  return NextResponse.redirect(new URL('/login', request.url));
}
```

#### 3. 取得使用者資料失敗

**情境**: `/api/auth/me` 無法取得使用者資料（token 無效或過期）

**處理方式**:
```typescript
if (!response.ok) {
  return NextResponse.json({ user: null }, { status: 200 });
}
```

**使用者體驗**:
- 回傳 `user: null`
- Navbar 顯示「登入」按鈕（視為未登入）

### 後端錯誤處理

#### 1. JWT Token 驗證錯誤

**情境**: Token 簽章錯誤、過期、格式錯誤

**處理方式**:
```java
public boolean validateToken(String token) {
    try {
        Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

**使用者體驗**:
- JwtFilter 不會設定 Authentication
- 後續的 Controller 會收到 `401 Unauthorized`

#### 2. 使用者不存在

**情境**: Token 有效但使用者已被刪除

**處理方式**:
```java
User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
```

**使用者體驗**:
- 回傳 `500 Internal Server Error`
- 前端視為未登入

**改善建議**:
應該回傳 `404 Not Found` 而非 `500`，並建立自訂例外類別。

#### 3. 資料庫錯誤

**情境**: 資料庫連線失敗、SQL 錯誤

**處理方式**:
- Spring Boot 會自動將資料庫例外轉換為 `500 Internal Server Error`
- 錯誤訊息會記錄在 log 中

---

## 安全性考量

### 1. httpOnly Cookie

**為什麼使用 httpOnly**:
- 前端 JavaScript 無法讀取 cookie，防止 XSS 攻擊
- 即使網站被注入惡意腳本，也無法竊取 JWT token

**設定**:
```typescript
cookieStore.set("token", loginData.token, {
  httpOnly: true,  // 關鍵設定
  secure: process.env.NODE_ENV === "production",
  sameSite: "lax",
  maxAge: 60 * 60 * 24 * 7,
  path: "/",
});
```

### 2. CORS 設定

**目的**: 防止未授權的網域存取後端 API

**設定**:
```java
configuration.setAllowedOrigins(List.of("http://localhost:3000"));
configuration.setAllowCredentials(true);  // 允許傳送 cookies
```

**注意事項**:
- 正式環境需更改為實際的前端網址
- `allowCredentials(true)` 配合 httpOnly cookie 使用

### 3. CSRF 保護

**為什麼停用 CSRF**:
- 使用 JWT 進行認證，不使用 session cookie
- JWT 需要主動在 header 中傳送，不會被瀏覽器自動附加
- `sameSite: "lax"` cookie 設定提供額外保護

**若使用 session cookie**:
必須啟用 CSRF 保護，否則容易受到 CSRF 攻擊。

### 4. JWT 密鑰安全

**目前設定**:
```yaml
jwt:
  secret: your-secret-key-change-this-in-production
```

**正式環境建議**:
- 使用強密碼（至少 32 位元組）
- 使用環境變數或安全的密鑰管理服務
- 定期更換密鑰（需要處理舊 token 失效問題）

### 5. OAuth 憑證安全

**重要提醒**:
- Google Client Secret 和 Facebook App Secret 不應提交到版控系統
- 使用 `.env` 檔案並加入 `.gitignore`
- 正式環境使用環境變數或密鑰管理服務

### 6. Token 過期時間

**目前設定**: 24 小時

**建議**:
- 根據應用程式需求調整
- 敏感應用程式建議縮短過期時間（例如 1 小時）
- 實作 refresh token 機制以提升使用者體驗

---

## 後續擴充建議

### 1. Refresh Token 機制

**目的**: 提升使用者體驗，避免頻繁重新登入

**實作方式**:
1. 登入時同時產生 access token 和 refresh token
2. Access token 設定較短過期時間（例如 15 分鐘）
3. Refresh token 設定較長過期時間（例如 7 天）
4. Access token 過期時，使用 refresh token 換取新的 access token

### 2. 登出功能

**目前狀況**: 無登出功能

**實作方式**:
1. 前端建立登出按鈕
2. 前端呼叫 `/api/auth/logout`
3. 後端清除 httpOnly cookie
4. 前端導向登入頁面

### 3. Token 黑名單

**目的**: 實作登出後立即失效（目前 token 在過期前都有效）

**實作方式**:
1. 建立 `token_blacklist` 表
2. 登出時將 token 加入黑名單
3. JwtFilter 驗證時檢查 token 是否在黑名單中

### 4. 錯誤訊息國際化

**目的**: 提供友善的錯誤訊息

**實作方式**:
1. 定義錯誤代碼
2. 建立錯誤訊息對照表
3. 登入頁面根據 error 參數顯示對應訊息

### 5. 使用者資料同步

**目的**: 定期同步 OAuth 提供者的最新資料

**實作方式**:
1. 建立排程任務
2. 定期更新使用者的 `displayName` 和 `avatarUrl`
3. 處理使用者帳號被刪除的情況

### 6. 多重 OAuth 帳號綁定

**目的**: 允許使用者綁定多個 OAuth 帳號（例如同時綁定 Google 和 Facebook）

**實作方式**:
1. 新增 `user_oauth_providers` 關聯表
2. 一個使用者可以有多個 OAuth provider
3. 登入時檢查 email 是否已存在，若存在則綁定到現有帳號

### 7. 經驗值與等級系統

**目的**: 實作完整的遊戲化功能

**實作方式**:
1. 定義經驗值獲得規則（例如完成任務、每日登入）
2. 定義等級提升規則
3. 建立經驗值累積 API
4. 建立等級排行榜

### 8. Email 登入支援

**目的**: 提供更多登入選項

**實作方式**:
1. 建立密碼欄位（需加密儲存）
2. 建立註冊 API
3. 建立 Email 登入 API
4. 實作忘記密碼功能

### 9. 雙因素認證（2FA）

**目的**: 提升帳號安全性

**實作方式**:
1. 支援 TOTP（Time-based One-Time Password）
2. 使用者啟用 2FA 後，登入時需要輸入驗證碼
3. 提供備用驗證碼

### 10. 完整的例外處理

**目的**: 提供統一且友善的錯誤回應

**實作方式**:
1. 建立自訂例外類別
2. 建立全域例外處理器（`@ControllerAdvice`）
3. 統一錯誤回應格式

**範例錯誤回應格式**:
```json
{
  "error": "USER_NOT_FOUND",
  "message": "使用者不存在",
  "timestamp": "2025-01-21T10:30:00Z"
}
```

---

## 總結

本系統實作了完整的 OAuth 2.0 登入流程，支援 Google 和 Facebook 兩種 OAuth 提供者。使用 JWT 進行身份驗證，並將 token 安全地儲存在 httpOnly cookie 中。系統架構清晰，分為前端（Next.js）和後端（Spring Boot），各司其職。

所有程式碼都加入了繁體中文註解，方便後續維護與擴充。安全性方面已考慮常見的攻擊向量（XSS、CSRF、CORS），並提供了多項後續擴充建議，包括 refresh token、登出功能、多重帳號綁定等。

本文件提供了完整的實作說明，包含系統架構、資料表設計、API 規格、錯誤處理與安全性考量，希望能幫助開發者快速了解系統運作方式。
