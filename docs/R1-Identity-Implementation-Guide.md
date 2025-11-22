# R1 身分整合與個人檔案 - 實作指南

> Work Skills Academy (WSA) 學習平台 - 使用者身分認證系統實作文件

## 目錄

1. [R1 目標與範圍](#r1-目標與範圍)
2. [系統架構概覽](#系統架構概覽)
3. [OAuth 登入完整流程](#oauth-登入完整流程)
4. [資料表設計](#資料表設計)
5. [後端 API 說明](#後端-api-說明)
6. [前端實作說明](#前端實作說明)
7. [錯誤處理與例外狀況](#錯誤處理與例外狀況)
8. [測試與驗證](#測試與驗證)

---

## R1 目標與範圍

### 主要目標

**R1（Release 1）專注於「使用者身分整合」**，確保學員登入後，整個站內都顯示其真實的個人資料，去除所有假資料（mock data）。

### 已完成功能

✅ **OAuth 登入整合**
- Google OAuth 2.0 登入
- Facebook OAuth 登入
- JWT Token 身分認證機制

✅ **使用者資料管理**
- 使用者資料表建立與初始化
- 首次登入自動建立使用者資料
- 再次登入自動更新使用者資料

✅ **前端顯示整合**
- Header 顯示真實使用者資訊（頭像、名稱、等級、XP）
- Profile 頁面顯示完整個人資料
- 登入頁面自動判斷已登入狀態

### R1 不包含的功能

❌ 課程/單元資料表與 API
❌ 課程購買流程
❌ XP 計算與升級邏輯（目前使用預設值）
❌ 排行榜後端邏輯

---

## 系統架構概覽

### 技術棧

**後端 (Backend)**
- Spring Boot 3.2.0
- Spring Security 6.x
- Spring Data JPA
- PostgreSQL 15
- JWT (HS256)

**前端 (Frontend)**
- Next.js 14 (App Router)
- React 18
- TypeScript
- Tailwind CSS + shadcn/ui
- httpOnly Cookie 儲存 JWT

**基礎設施**
- Docker + Docker Compose
- PostgreSQL Container
- 容器間網路通訊

### 資料流向

```
使用者 → 前端 Next.js → 後端 Spring Boot → PostgreSQL
         ↑                    ↓
         ← JWT Cookie ←────────
```

---

## OAuth 登入完整流程

### 流程圖

```
[使用者] → [1. 點擊登入按鈕]
              ↓
[前端] → [2. 導向 OAuth Provider]
              ↓
[Google/FB] → [3. 使用者授權]
              ↓
[前端] ← [4. Callback with Code]
              ↓
[前端] → [5. 呼叫後端 /api/auth/oauth-login]
              ↓
[後端] → [6. 向 OAuth Provider 換取 Access Token]
              ↓
[後端] → [7. 取得使用者資料]
              ↓
[後端] → [8. 建立/更新 User]
              ↓
[後端] → [9. 產生 JWT Token]
              ↓
[前端] ← [10. 設定 httpOnly Cookie + 回傳使用者資料]
              ↓
[前端] → [11. 導回首頁]
              ↓
[Header] → [12. 自動顯示使用者資訊]
```

### 詳細步驟說明

#### 步驟 1-3：前端發起 OAuth 授權

**檔案位置：** `frontend/app/(auth)/login/page.tsx`

```typescript
const handleGoogleLogin = () => {
  // 建立 Google OAuth 授權 URL
  const googleAuthUrl = new URL("https://accounts.google.com/o/oauth2/v2/auth");
  googleAuthUrl.searchParams.append("client_id", process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID);
  googleAuthUrl.searchParams.append("redirect_uri", "http://localhost:3000/api/auth/google/callback");
  googleAuthUrl.searchParams.append("response_type", "code");
  googleAuthUrl.searchParams.append("scope", "openid email profile");

  // 導向 Google 授權頁面
  window.location.href = googleAuthUrl.toString();
};
```

**說明：**
- 使用者點擊「使用 Google 登入」按鈕
- 導向 Google OAuth 授權頁面
- 使用者在 Google 頁面輸入帳號密碼並授權
- Google 導回 `redirect_uri` 並附上 `code` 參數

#### 步驟 4-5：Callback 處理並呼叫後端

**檔案位置：** `frontend/app/api/auth/google/callback/route.ts`

```typescript
export async function GET(request: NextRequest) {
  const code = searchParams.get("code");

  // 1. 使用 code 向 Google 換取 access token
  const tokenResponse = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    body: JSON.stringify({
      code,
      client_id: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID,
      client_secret: process.env.GOOGLE_CLIENT_SECRET,
      redirect_uri,
      grant_type: "authorization_code",
    }),
  });

  const { access_token } = await tokenResponse.json();

  // 2. 使用 access token 取得使用者資料
  const userInfoResponse = await fetch(
    "https://www.googleapis.com/oauth2/v2/userinfo",
    { headers: { Authorization: `Bearer ${access_token}` } }
  );

  const userInfo = await userInfoResponse.json();

  // 3. 呼叫後端 OAuth 登入 API
  const loginResponse = await fetch(`${backendUrl}/api/auth/oauth-login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      provider: "google",
      externalId: userInfo.sub,
      email: userInfo.email,
      displayName: userInfo.name,
      avatarUrl: userInfo.picture,
    }),
  });

  const { token } = await loginResponse.json();

  // 4. 設定 httpOnly cookie
  const response = NextResponse.redirect(new URL("/", request.url), {
    status: 303,
  });
  response.cookies.set("token", token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    maxAge: 60 * 60 * 24 * 7, // 7 天
    path: "/",
  });

  return response;
}
```

**說明：**
- 前端 API Route 接收 Google 回傳的 `code`
- 使用 `code` 向 Google 換取 `access_token`
- 使用 `access_token` 取得使用者資料
- 呼叫後端 `/api/auth/oauth-login` 建立/更新使用者
- 將後端回傳的 JWT token 設定到 httpOnly cookie
- 導回首頁

#### 步驟 6-9：後端處理 OAuth 登入

**檔案位置：** `backend/src/main/java/com/wsa/controller/AuthController.java`

```java
@PostMapping("/oauth-login")
public ResponseEntity<OAuthLoginResponse> oauthLogin(@RequestBody OAuthLoginRequest request) {
    // 建立或更新使用者資料
    User user = userService.createOrUpdateUser(request);

    // 為使用者產生 JWT token
    String token = jwtService.generateToken(user.getId());

    // 建立回應物件
    OAuthLoginResponse response = OAuthLoginResponse.builder()
            .token(token)
            .user(UserDto.from(user))
            .build();

    return ResponseEntity.ok(response);
}
```

**檔案位置：** `backend/src/main/java/com/wsa/service/UserService.java`

```java
@Transactional
public User createOrUpdateUser(OAuthLoginRequest request) {
    // 查詢使用者是否已存在
    Optional<User> existingUser = userRepository.findByProviderAndExternalId(
            request.getProvider(),
            request.getExternalId()
    );

    if (existingUser.isPresent()) {
        // 使用者已存在，更新資料
        User user = existingUser.get();
        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());
        user.setAvatarUrl(request.getAvatarUrl());
        return userRepository.save(user);
    } else {
        // 使用者不存在，建立新使用者
        User newUser = User.builder()
                .externalId(request.getExternalId())
                .provider(request.getProvider())
                .displayName(request.getDisplayName())
                .email(request.getEmail())
                .avatarUrl(request.getAvatarUrl())
                .level(1)      // 初始等級為 1
                .totalXp(0)    // 初始總經驗值為 0
                .weeklyXp(0)   // 初始本週經驗值為 0
                .build();
        return userRepository.save(newUser);
    }
}
```

**說明：**
- 接收前端傳來的 OAuth 使用者資料
- 根據 `provider` + `externalId` 查詢使用者是否已存在
- 若存在：更新 `displayName`、`email`、`avatarUrl`
- 若不存在：建立新使用者，設定初始值（level=1, totalXp=0, weeklyXp=0）
- 產生 JWT token（包含 userId）
- 回傳 token 和使用者資料給前端

#### 步驟 10-12：前端顯示使用者資訊

**檔案位置：** `frontend/components/site-header.tsx`

```typescript
React.useEffect(() => {
  const fetchUser = async () => {
    const response = await fetch("/api/auth/me", {
      credentials: "include", // 傳送 cookie
    });

    if (response.ok) {
      const data = await response.json();
      setUser(data.user); // 設定使用者資料
    }
  };

  fetchUser();
}, [pathname]); // 每次路由變更都重新取得
```

**說明：**
- Header 元件載入時自動呼叫 `/api/auth/me`
- `/api/auth/me` 從 cookie 讀取 JWT，向後端取得使用者資料
- 設定 user 狀態後，Header 自動顯示使用者頭像、名稱、等級、XP

---

## 資料表設計

### users 資料表

**檔案位置：** `backend/src/main/java/com/wsa/entity/User.java`

| 欄位名稱 | 型別 | 說明 | 預設值 | 備註 |
|---------|------|------|--------|------|
| `id` | UUID | 使用者唯一識別碼 | 自動產生 | Primary Key |
| `external_id` | VARCHAR | OAuth 提供者的使用者 ID | - | Google sub 或 Facebook id |
| `provider` | VARCHAR | OAuth 提供者 | - | "google" 或 "facebook" |
| `display_name` | VARCHAR | 使用者顯示名稱 | - | 來自 OAuth profile |
| `email` | VARCHAR | 使用者電子郵件 | - | 來自 OAuth profile |
| `avatar_url` | VARCHAR | 使用者頭像 URL | NULL | 來自 OAuth profile |
| `level` | INTEGER | 使用者等級 | 1 | R1 固定為 1 |
| `total_xp` | INTEGER | 總經驗值 | 0 | R1 固定為 0 |
| `weekly_xp` | INTEGER | 本週經驗值 | 0 | R1 固定為 0 |
| `created_at` | TIMESTAMP | 建立時間 | 自動產生 | 不可修改 |
| `updated_at` | TIMESTAMP | 更新時間 | 自動更新 | 每次 save 自動更新 |

**Unique Constraint:**
- (`provider`, `external_id`) - 同一個 OAuth 提供者的同一個使用者只能有一筆記錄

**索引建議：**
- Primary Key: `id`
- Unique Index: (`provider`, `external_id`)

**資料範例：**

```sql
INSERT INTO users (id, external_id, provider, display_name, email, avatar_url, level, total_xp, weekly_xp)
VALUES (
  '550e8400-e29b-41d4-a716-446655440000',
  '1234567890',
  'google',
  '王小明',
  'user@example.com',
  'https://lh3.googleusercontent.com/a/ACg8ocI...',
  1,
  0,
  0
);
```

**欄位說明**

#### OAuth 相關欄位

- **external_id**:
  - Google: 來自 `userInfo.sub`
  - Facebook: 來自 `userInfo.id`
  - 用途：識別使用者在 OAuth 提供者的唯一 ID

- **provider**:
  - 可能值：`"google"` 或 `"facebook"`
  - 用途：區分使用者使用哪個 OAuth 提供者登入

#### 等級與經驗值

- **level**:
  - R1 階段：固定為 1
  - 未來：會根據 totalXp 計算等級

- **total_xp**:
  - R1 階段：固定為 0
  - 未來：完成課程單元時累積 XP

- **weekly_xp**:
  - R1 階段：固定為 0
  - 未來：每週重置，用於週排行榜

---

## 後端 API 說明

### 1. `/api/auth/oauth-login` - OAuth 登入

**Endpoint:** `POST /api/auth/oauth-login`

**認證：** 無需認證（公開端點）

**用途：** 接收前端傳來的 OAuth 使用者資料，建立或更新使用者，並產生 JWT token

#### Request Body

```json
{
  "provider": "google",
  "externalId": "1234567890",
  "email": "user@example.com",
  "displayName": "王小明",
  "avatarUrl": "https://lh3.googleusercontent.com/a/..."
}
```

**欄位說明：**

| 欄位 | 型別 | 必填 | 說明 |
|------|------|------|------|
| `provider` | String | ✅ | OAuth 提供者："google" 或 "facebook" |
| `externalId` | String | ✅ | OAuth 提供者的使用者 ID |
| `email` | String | ⚠️ | 使用者電子郵件（Facebook 可能為 null） |
| `displayName` | String | ✅ | 使用者顯示名稱 |
| `avatarUrl` | String | ❌ | 使用者頭像 URL（可選） |

#### Response 200 OK

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "displayName": "王小明",
    "email": "user@example.com",
    "avatarUrl": "https://lh3.googleusercontent.com/a/...",
    "provider": "google",
    "level": 1,
    "totalXp": 0,
    "weeklyXp": 0
  }
}
```

#### 處理邏輯

1. **查詢使用者是否存在**
   ```java
   Optional<User> existingUser = userRepository.findByProviderAndExternalId(
       request.getProvider(),
       request.getExternalId()
   );
   ```

2. **若使用者已存在（再次登入）**
   - 更新 `displayName`、`email`、`avatarUrl`
   - 更新 `updated_at`
   - 保留原有的 `level`、`totalXp`、`weeklyXp`

3. **若使用者不存在（首次登入）**
   - 建立新 User
   - 設定初始值：`level=1`, `totalXp=0`, `weeklyXp=0`
   - 設定 `created_at` 和 `updated_at`

4. **產生 JWT Token**
   ```java
   String token = jwtService.generateToken(user.getId());
   ```

5. **回傳 Token 和使用者資料**

#### 錯誤處理

| 狀態碼 | 說明 | 處理方式 |
|--------|------|----------|
| 400 | Request Body 缺少必填欄位 | 回傳錯誤訊息，說明缺少哪個欄位 |
| 500 | 資料庫錯誤或其他伺服器錯誤 | 記錄錯誤日誌，回傳通用錯誤訊息 |

---

### 2. `/api/user/me` - 取得當前使用者資料

**Endpoint:** `GET /api/user/me`

**認證：** 需要 JWT Token（透過 `Authorization: Bearer <token>` header）

**用途：** 取得當前登入使用者的完整資料

#### Request

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "displayName": "王小明",
  "email": "user@example.com",
  "avatarUrl": "https://lh3.googleusercontent.com/a/...",
  "provider": "google",
  "level": 1,
  "totalXp": 0,
  "weeklyXp": 0
}
```

#### 處理邏輯

1. **驗證 JWT Token**
   - JwtFilter 攔截請求
   - 驗證 token 是否有效
   - 從 token 中提取 userId
   - 設定到 SecurityContext

2. **查詢使用者資料**
   ```java
   UUID userId = (UUID) authentication.getPrincipal();
   User user = userRepository.findById(userId)
           .orElseThrow(() -> new RuntimeException("User not found"));
   ```

3. **轉換為 DTO 並回傳**
   ```java
   return ResponseEntity.ok(UserDto.from(user));
   ```

#### 錯誤處理

| 狀態碼 | 說明 | 處理方式 |
|--------|------|----------|
| 401 | Token 無效或過期 | 前端視為未登入，清除狀態 |
| 404 | 使用者不存在（token 中的 userId 找不到對應使用者） | 前端視為未登入，清除狀態 |
| 500 | 資料庫錯誤或其他伺服器錯誤 | 記錄錯誤日誌，前端視為未登入 |

---

## 前端實作說明

### 1. `/api/auth/me` - Next.js API Route

**檔案位置：** `frontend/app/api/auth/me/route.ts`

**用途：** Server-side API Route，從 cookie 讀取 JWT，向後端取得使用者資料

#### 流程

1. **從 cookie 讀取 token**
   ```typescript
   const cookieStore = await cookies();
   const token = cookieStore.get("token")?.value;
   ```

2. **若無 token，回傳 null**
   ```typescript
   if (!token) {
     return NextResponse.json({ user: null }, { status: 200 });
   }
   ```

3. **向後端 `/api/user/me` 發送請求**
   ```typescript
   const response = await fetch(`${apiUrl}/api/user/me`, {
     headers: {
       Authorization: `Bearer ${token}`,
     },
   });
   ```

4. **若成功，回傳使用者資料**
   ```typescript
   if (response.ok) {
     const user = await response.json();
     return NextResponse.json({ user }, { status: 200 });
   }
   ```

5. **若失敗（401/404），回傳 null**
   ```typescript
   if (!response.ok) {
     return NextResponse.json({ user: null }, { status: 200 });
   }
   ```

#### 為什麼需要這個 API Route？

- **httpOnly cookie 安全性**: httpOnly cookie 無法從 client-side JavaScript 讀取
- **Server-side 讀取**: 只有 server-side 可以讀取 httpOnly cookie
- **統一錯誤處理**: 將後端的 401/404 統一轉換為 `{ user: null }`，簡化前端處理

#### 環境變數設定

```bash
# 容器內部使用（docker-compose）
API_URL=http://backend:8080

# 本地開發使用
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

### 2. Header 元件 - 顯示使用者資訊

**檔案位置：** `frontend/components/site-header.tsx`

**用途：** 顯示使用者頭像、名稱、等級、XP，並提供登出功能

#### 資料來源

```typescript
React.useEffect(() => {
  const fetchUser = async () => {
    const response = await fetch("/api/auth/me", {
      credentials: "include",
    });

    if (response.ok) {
      const data = await response.json();
      setUser(data.user);
    } else {
      setUser(null);
    }
  };

  fetchUser();
}, [pathname]); // 每次路由變更都重新取得
```

**特點：**
- 使用真實 API 資料，不使用假資料
- 監聽 `pathname` 變更，確保登入後立即更新
- 失敗時自動設為未登入狀態

#### 登出功能

```typescript
const handleLogout = async () => {
  // 呼叫後端 API 清除 httpOnly cookie
  const response = await fetch("/api/auth/logout", {
    method: "POST",
    credentials: "include",
  });

  if (response.ok) {
    setUser(null);
    window.location.href = "/"; // 強制重新載入頁面
  }
};
```

**重點：**
- httpOnly cookie 無法從前端 JavaScript 直接刪除
- 必須呼叫後端 API（`/api/auth/logout`）清除 cookie
- 使用 `window.location.href` 強制重新載入，確保所有狀態都被清除

---

### 3. Profile 頁面 - 顯示個人檔案

**檔案位置：** `frontend/app/(dashboard)/profile/page.tsx`

**用途：** 顯示使用者完整個人資料，包含頭像、名稱、email、等級、XP

#### 資料來源

```typescript
useEffect(() => {
  const fetchUser = async () => {
    const response = await fetch("/api/auth/me", {
      credentials: "include",
    });

    if (response.ok) {
      const data = await response.json();
      setUser(data.user);
    } else {
      setUser(null);
    }
  };

  fetchUser();
}, []);
```

#### 未登入處理

```typescript
if (!user) {
  return (
    <Card className="w-full max-w-md">
      <CardHeader>
        <CardTitle>請先登入</CardTitle>
        <CardDescription>
          你目前尚未登入，請先登入以查看個人檔案
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Button asChild className="w-full">
          <Link href="/login">前往登入</Link>
        </Button>
      </CardContent>
    </Card>
  );
}
```

**特點：**
- 移除所有假資料（mock data）
- 未登入時顯示提示卡片，提供「前往登入」按鈕
- 使用 shadcn/ui 元件（Card, Avatar, Badge, Progress）

---

### 4. Login 頁面 - OAuth 登入

**檔案位置：** `frontend/app/(auth)/login/page.tsx`

**用途：** 提供 Google / Facebook 登入選項，並檢查是否已登入

#### 已登入狀態判斷

```typescript
useEffect(() => {
  const checkLoginStatus = async () => {
    const response = await fetch("/api/auth/me", {
      credentials: "include",
    });

    if (response.ok) {
      const data = await response.json();
      setUser(data.user);
    }
  };

  checkLoginStatus();
}, []);
```

**若已登入：**
- 顯示「你已登入」訊息
- 顯示使用者資訊卡片
- 提供「前往首頁」和「查看個人檔案」按鈕

**若未登入：**
- 顯示 Google / Facebook 登入按鈕

---

## 錯誤處理與例外狀況

### 後端錯誤處理

#### 1. OAuth Provider 錯誤

**情境：** Google / Facebook API 回傳錯誤（例如 code 無效、網路錯誤）

**處理方式：**
- 前端 callback route 捕捉錯誤
- 導回登入頁面並顯示錯誤訊息
- 記錄錯誤日誌供後續追蹤

**範例：**
```typescript
try {
  const tokenResponse = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    body: JSON.stringify({ code, client_id, client_secret, redirect_uri, grant_type }),
  });

  if (!tokenResponse.ok) {
    throw new Error("Failed to get access token");
  }
} catch (error) {
  console.error("[Google Callback] 錯誤:", error);
  return NextResponse.redirect(new URL("/login?error=oauth_failed", request.url));
}
```

#### 2. JWT Token 過期或無效

**情境：** 使用者的 JWT token 已過期或被竄改

**處理方式：**
- JwtFilter 驗證失敗
- 不設定 Authentication 到 SecurityContext
- Controller 檢查到未認證，回傳 401
- 前端收到 401，視為未登入，清除狀態

**範例：**
```java
// JwtFilter
if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)) {
    UUID userId = jwtService.getUserIdFromToken(jwt);
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        userId, null, new ArrayList<>()
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
} else {
    // Token 無效，不設定 Authentication
}
```

```java
// UserController
if (authentication == null || authentication.getPrincipal() == null) {
    return ResponseEntity.status(401).build(); // 未認證
}
```

#### 3. 使用者不存在

**情境：** Token 中的 userId 在資料庫中找不到（例如使用者被刪除）

**處理方式：**
- UserRepository.findById() 回傳空的 Optional
- 拋出 RuntimeException 或回傳 404
- 前端視為未登入

**範例：**
```java
User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
```

### 前端錯誤處理

#### 1. 網路錯誤

**情境：** 無法連線到後端 API

**處理方式：**
```typescript
try {
  const response = await fetch("/api/auth/me", {
    credentials: "include",
  });

  if (response.ok) {
    const data = await response.json();
    setUser(data.user);
  }
} catch (error) {
  console.error("[SiteHeader] 取得使用者資訊失敗:", error);
  setUser(null); // 視為未登入
}
```

#### 2. API 回傳錯誤

**情境：** 後端回傳 4xx 或 5xx 錯誤

**處理方式：**
```typescript
if (response.ok) {
  // 成功
  const data = await response.json();
  setUser(data.user);
} else {
  // 失敗（包含 401, 404, 500 等）
  console.log("[SiteHeader] 未登入或認證失敗");
  setUser(null);
}
```

**統一處理原則：** 任何錯誤都視為未登入，避免前端顯示錯誤訊息或報錯

---

## 測試與驗證

### 功能測試清單

#### ✅ OAuth 登入流程

**Google 登入：**
1. [ ] 點擊「使用 Google 登入」按鈕
2. [ ] 導向 Google 授權頁面
3. [ ] 輸入 Google 帳號密碼並授權
4. [ ] 導回首頁
5. [ ] Header 顯示 Google 帳號的頭像和名稱
6. [ ] 檢查 DevTools → Application → Cookies，確認 token cookie 存在

**Facebook 登入：**
1. [ ] 點擊「使用 Facebook 登入」按鈕
2. [ ] 導向 Facebook 授權頁面
3. [ ] 輸入 Facebook 帳號密碼並授權
4. [ ] 導回首頁
5. [ ] Header 顯示 Facebook 帳號的頭像和名稱

#### ✅ 使用者資訊顯示

**Header 顯示：**
1. [ ] 顯示使用者頭像
2. [ ] Hover 顯示 Dropdown
3. [ ] Dropdown 顯示顯示名稱
4. [ ] Dropdown 顯示電子郵件
5. [ ] Dropdown 顯示等級（Level 1）
6. [ ] Dropdown 顯示總經驗值（0 XP）
7. [ ] Dropdown 顯示本週經驗值（0 XP）

**Profile 頁面：**
1. [ ] 顯示使用者頭像
2. [ ] 顯示顯示名稱
3. [ ] 顯示電子郵件
4. [ ] 顯示等級（Level 1）
5. [ ] 顯示 XP 進度條
6. [ ] 顯示總經驗值和本週經驗值卡片

#### ✅ 登出功能

1. [ ] 點擊 Header Dropdown 中的「登出」按鈕
2. [ ] 導回首頁
3. [ ] Header 顯示「登入」按鈕（不顯示使用者頭像）
4. [ ] 檢查 DevTools → Application → Cookies，確認 token cookie 已被刪除

#### ✅ 未登入狀態

**訪問 Profile 頁面：**
1. [ ] 未登入時訪問 `/profile`
2. [ ] 顯示「請先登入」提示卡片
3. [ ] 點擊「前往登入」按鈕，導向 `/login`

**訪問 Login 頁面：**
1. [ ] 已登入時訪問 `/login`
2. [ ] 顯示「你已登入」訊息
3. [ ] 顯示使用者資訊卡片
4. [ ] 提供「前往首頁」和「查看個人檔案」按鈕

#### ✅ 再次登入（更新資料）

1. [ ] 使用 Google 登入
2. [ ] 前往 Google 帳號設定，修改顯示名稱
3. [ ] 登出 WSA
4. [ ] 重新使用 Google 登入
5. [ ] 確認 WSA 顯示的名稱已更新

### 資料庫驗證

**檢查使用者資料：**
```sql
SELECT * FROM users ORDER BY created_at DESC LIMIT 10;
```

**驗證項目：**
1. [ ] `external_id` 正確（Google sub 或 Facebook id）
2. [ ] `provider` 正確（"google" 或 "facebook"）
3. [ ] `display_name` 正確
4. [ ] `email` 正確
5. [ ] `avatar_url` 正確
6. [ ] `level` = 1
7. [ ] `total_xp` = 0
8. [ ] `weekly_xp` = 0
9. [ ] `created_at` 有值
10. [ ] `updated_at` 有值

**檢查 Unique Constraint：**
```sql
-- 嘗試插入重複的 provider + external_id（應該失敗）
INSERT INTO users (external_id, provider, display_name, email, level, total_xp, weekly_xp)
VALUES ('1234567890', 'google', 'Test User', 'test@example.com', 1, 0, 0);
-- 應該回傳: ERROR: duplicate key value violates unique constraint
```

### API 測試

**測試 `/api/auth/oauth-login`：**
```bash
curl -X POST http://localhost:8080/api/auth/oauth-login \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "externalId": "test-external-id",
    "email": "test@example.com",
    "displayName": "測試使用者",
    "avatarUrl": "https://example.com/avatar.jpg"
  }'
```

**預期回應：**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "...",
    "displayName": "測試使用者",
    "email": "test@example.com",
    "avatarUrl": "https://example.com/avatar.jpg",
    "provider": "google",
    "level": 1,
    "totalXp": 0,
    "weeklyXp": 0
  }
}
```

**測試 `/api/user/me`：**
```bash
# 先取得 token（從上一步）
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 使用 token 取得使用者資料
curl http://localhost:8080/api/user/me \
  -H "Authorization: Bearer $TOKEN"
```

**預期回應：**
```json
{
  "id": "...",
  "displayName": "測試使用者",
  "email": "test@example.com",
  "avatarUrl": "https://example.com/avatar.jpg",
  "provider": "google",
  "level": 1,
  "totalXp": 0,
  "weeklyXp": 0
}
```

---

## 常見問題 (FAQ)

### Q1: 為什麼要使用 httpOnly cookie 儲存 JWT？

**A:** 安全性考量。httpOnly cookie 無法從 client-side JavaScript 讀取，可以防止 XSS 攻擊竊取 token。

**對比：**
- **localStorage**: 可以從 JavaScript 讀取，容易受 XSS 攻擊
- **httpOnly cookie**: 無法從 JavaScript 讀取，只有 server-side 可以讀取

### Q2: 為什麼需要 `/api/auth/me` Next.js API Route？

**A:** 因為 httpOnly cookie 無法從 client-side JavaScript 讀取，必須透過 server-side API Route 讀取 cookie 並向後端發送請求。

**流程：**
1. Client Component 呼叫 `/api/auth/me`
2. Next.js API Route（server-side）讀取 httpOnly cookie
3. API Route 向後端 `/api/user/me` 發送請求（附上 token）
4. 後端驗證 token 並回傳使用者資料
5. API Route 將資料回傳給 Client Component

### Q3: 如何在本地開發時測試 OAuth 登入？

**A:** 確保以下設定正確：

1. **Google Cloud Console**:
   - 新增 Authorized redirect URI: `http://localhost:3000/api/auth/google/callback`

2. **Facebook Developers**:
   - 新增 Valid OAuth Redirect URIs: `http://localhost:3000/api/auth/facebook/callback`

3. **環境變數**:
   ```bash
   NEXT_PUBLIC_GOOGLE_CLIENT_ID=your-google-client-id
   GOOGLE_CLIENT_SECRET=your-google-client-secret
   NEXT_PUBLIC_FACEBOOK_APP_ID=your-facebook-app-id
   FACEBOOK_APP_SECRET=your-facebook-app-secret
   NEXT_PUBLIC_APP_URL=http://localhost:3000
   ```

### Q4: 登出後為什麼還是顯示登入狀態？

**A:** 可能原因：

1. **前端未正確呼叫登出 API**: 確認 `handleLogout` 有呼叫 `/api/auth/logout`
2. **Cookie 未被刪除**: 檢查 DevTools → Application → Cookies，確認 token 是否存在
3. **快取問題**: 使用 `window.location.href = "/"` 強制重新載入頁面

### Q5: 為什麼 level、totalXp、weeklyXp 都是預設值？

**A:** R1 階段專注在身分整合，不包含課程系統和 XP 計算邏輯。這些欄位會在後續版本實作。

---

## 後續擴充建議

### R2: 課程與單元系統

**建議功能：**
- 課程資料表（courses）
- 單元資料表（units）
- 使用者課程進度（user_course_progress）
- 課程購買記錄

**API 建議：**
- `GET /api/courses` - 取得課程列表
- `GET /api/courses/{code}` - 取得課程詳情
- `GET /api/courses/{code}/units` - 取得課程單元
- `POST /api/units/{id}/complete` - 完成單元（獲得 XP）

### R3: XP 與等級系統

**建議功能：**
- XP 計算規則
- 等級升級邏輯
- 週排行榜（重置 weeklyXp）
- 成就系統

**API 建議：**
- `GET /api/leaderboard/weekly` - 取得週排行榜
- `GET /api/leaderboard/all-time` - 取得總排行榜
- `GET /api/achievements` - 取得成就列表

### R4: 社群功能

**建議功能：**
- 討論區
- 留言系統
- 好友系統
- 邀請碼

---

## 總結

R1 成功實現了以下目標：

✅ **完整的 OAuth 登入流程**（Google + Facebook）
✅ **JWT 身分認證機制**
✅ **使用者資料管理**（建立/更新）
✅ **前端真實資料顯示**（Header + Profile）
✅ **登出功能**
✅ **繁體中文註解**（前後端完整註解）

這為後續的課程系統、XP 系統、排行榜等功能奠定了堅實的基礎。

---

**文件版本：** v1.0
**最後更新：** 2025-11-21
**維護者：** WSA Development Team
