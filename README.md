# WSA OAuth Application

這是一個使用 Spring Boot 後端和 Next.js 前端的 OAuth 登入應用程式。

## 技術棧

- **Frontend**: Next.js 14 (App Router, TypeScript)
- **Backend**: Spring Boot 3.2
- **Database**: PostgreSQL 15
- **Authentication**: JWT + OAuth 2.0 (Google & Facebook)

## 專案結構

```
wsa/
├── backend/                # Spring Boot 後端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/wsa/
│   │   │   │   ├── config/        # 配置類別
│   │   │   │   ├── controller/    # REST Controllers
│   │   │   │   ├── dto/          # Data Transfer Objects
│   │   │   │   ├── entity/       # JPA Entities
│   │   │   │   ├── filter/       # JWT Filter
│   │   │   │   ├── repository/   # JPA Repositories
│   │   │   │   └── service/      # 業務邏輯
│   │   │   └── resources/
│   │   │       ├── db/migration/ # Flyway migrations
│   │   │       └── application.yml
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/              # Next.js 前端
│   ├── app/
│   │   ├── (auth)/login/         # 登入頁面
│   │   ├── api/auth/             # OAuth callbacks
│   │   ├── layout.tsx
│   │   └── page.tsx
│   ├── components/               # React 元件
│   ├── Dockerfile
│   ├── package.json
│   └── tsconfig.json
├── docker-compose.yml
└── README.md
```

## 快速開始

### 1. 設定環境變數

複製 `.env.example` 並重命名為 `.env`，然後填入您的 OAuth 憑證：

```bash
cp .env.example .env
```

### 2. 取得 OAuth 憑證

#### Google OAuth
1. 前往 [Google Cloud Console](https://console.cloud.google.com/)
2. 建立新專案或選擇現有專案
3. 啟用 Google+ API
4. 建立 OAuth 2.0 憑證
5. 設定授權重新導向 URI：`http://localhost:3000/api/auth/google/callback`
6. 複製 Client ID 和 Client Secret 到 `.env`

#### Facebook OAuth
1. 前往 [Facebook Developers](https://developers.facebook.com/)
2. 建立新應用程式
3. 新增 Facebook Login 產品
4. 設定有效的 OAuth 重新導向 URI：`http://localhost:3000/api/auth/facebook/callback`
5. 複製 App ID 和 App Secret 到 `.env`

### 3. 使用 Docker Compose 啟動

```bash
# 啟動所有服務（PostgreSQL、Backend、Frontend）
docker-compose up -d

# 查看日誌
docker-compose logs -f

# 停止所有服務
docker-compose down
```

### 4. 本地開發模式

#### 啟動 PostgreSQL
```bash
docker-compose up -d postgres
```

#### 啟動後端
```bash
cd backend
mvn spring-boot:run
```

#### 啟動前端
```bash
cd frontend
npm install
npm run dev
```

## 存取應用程式

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Database**: localhost:5432

## API 端點

### 認證相關
- `POST /api/auth/oauth-login` - OAuth 登入
- `GET /api/user/me` - 獲取當前用戶資料

## OAuth 登入流程

1. 使用者點擊「使用 Google 登入」或「使用 Facebook 登入」
2. 重新導向到 OAuth 提供者
3. 使用者授權後，OAuth 提供者將使用者導回應用程式
4. 前端使用授權碼交換 access token
5. 前端使用 access token 獲取使用者資料
6. 前端將使用者資料傳送到後端 `/api/auth/oauth-login`
7. 後端建立或更新使用者，並產生 JWT
8. 前端將 JWT 儲存在 httpOnly cookie 中
9. 使用者被重新導向到首頁，Navbar 顯示使用者頭像和名稱

## 測試 OAuth 登入流程

1. 確保所有服務都在運行
2. 開啟瀏覽器訪問 http://localhost:3000
3. 點擊右上角的「登入」按鈕
4. 選擇「使用 Google 登入」或「使用 Facebook 登入」
5. 完成 OAuth 授權流程
6. 成功登入後，您應該會在 Navbar 看到您的頭像和名稱

## 資料庫結構

### users 表

| 欄位名稱 | 類型 | 說明 |
|---------|------|------|
| id | UUID | 主鍵 |
| external_id | VARCHAR | OAuth 提供者的使用者 ID |
| provider | VARCHAR | OAuth 提供者（google/facebook）|
| display_name | VARCHAR | 顯示名稱 |
| email | VARCHAR | 電子郵件 |
| avatar_url | VARCHAR | 頭像 URL |
| level | INTEGER | 等級 |
| total_xp | INTEGER | 總經驗值 |
| weekly_xp | INTEGER | 每週經驗值 |
| created_at | TIMESTAMP | 建立時間 |
| updated_at | TIMESTAMP | 更新時間 |

## 故障排除

### 後端無法連接到資料庫
- 確認 PostgreSQL 容器正在運行：`docker ps`
- 檢查資料庫連線設定是否正確

### OAuth 登入失敗
- 確認 OAuth 憑證設定正確
- 檢查重新導向 URI 是否與 OAuth 提供者設定一致
- 查看瀏覽器開發者工具的 Console 和 Network 面板

### CORS 錯誤
- 確認後端 SecurityConfig 中的 CORS 設定包含前端 URL

## 授權

MIT
