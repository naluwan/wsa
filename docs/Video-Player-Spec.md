# Video Player Spec（YouTube IFrame 已實作）

目的：  
目前 `components/youtube-player.tsx` 已經實作好，內部使用 **YouTube IFrame Player API + 自製控制列**。  
之後所有影片單元一律使用這個元件，不得改回 `react-player` 或直接 `<iframe>`。

Claude Code 在實作 / 修改其他程式碼時，必須遵守以下規則。

---

## 1. 元件約定：`components/youtube-player.tsx`

> ⚠️ **重要**：此檔案內的播放器邏輯（載入 IFrame API、建立 `YT.Player`）已經可以正常播放 YouTube 影片，除非有 bug，否則不要大改或移除。

元件介面（請維持、勿隨意變更）：

```ts
type YoutubePlayerProps = {
  videoId: string;                       // YouTube 影片 ID
  onProgress?: (sec: number, duration: number) => void; // 每秒回報目前秒數與總長度
  onEnded?: () => void;                 // 影片結束時呼叫
};
元件內部必須維持以下行為：

使用 <iframe> 播放影片，網址包含：

enablejsapi=1

正確的 origin（window.location.origin）

載入 https://www.youtube.com/iframe_api 並透過 new YT.Player(...) 建立播放器實例。

每秒呼叫 onProgress(currentTime, duration)。

當狀態為 PlayerState.ENDED 時呼叫 onEnded()。

2. 單元進度紀錄（User Progress）
這部分若尚未實作，請 補齊後端 API + 前端串接；播放器只負責回報時間，不直接打 API。

2.1 後端資料表約定
在 user_unit_progress 中至少要有：

last_position_seconds INTEGER DEFAULT 0 — 上次觀看到的秒數

last_watched_at TIMESTAMP DEFAULT NOW() — 最後觀看時間

2.2 前端呼叫 API 的規則
在使用 YoutubePlayer 的單元頁中：

在 onProgress callback 裡：

更新 React state 中的 currentSeconds / durationSeconds（用於畫進度條）

每 5 秒呼叫一次 API：
POST /api/user/progress/{unitId}
body 至少要帶：

json
複製程式碼
{ "lastPositionSeconds": 123 }
載入單元頁時（Server/Client 端）：

透過 GET /api/units/{unitId} 取得：

videoUrl 或 videoId

lastPositionSeconds

播放器 ready 後，根據 lastPositionSeconds 呼叫 player.seekTo(...) 讓使用者從上次的位置繼續看。

3. 完成邏輯（XP / 交付按鈕）
3.1 進度判斷
使用 currentSeconds / durationSeconds 計算進度百分比。

規則：當進度 >= 95% 時，才允許顯示 / 啟用「交付」按鈕。

3.2 交付按鈕行為
在單元頁 /units/[unitId] 中：

按鈕文案：交付作業 或 標記為完成

必須有 data-testid="complete-unit-button"

點擊時流程：

呼叫 POST /api/units/{unitId}/complete

後端依照 R1-Unit-And-XP-Spec.md：

寫入 / 更新 user_unit_progress

更新 total_xp, weekly_xp, level

前端收到成功回應後：

顯示 toast：「已完成單元，獲得 XXX XP」

重新呼叫 /api/auth/me 更新 Header 上的 XP / Level 顯示

更新本頁 isCompleted 狀態（按鈕可變成「已完成」並 disabled）

4. 使用限制（IMPORTANT）
禁止 在專案其他地方直接使用 <iframe> 嵌入 YouTube 作為課程影片播放器。

所有單元頁（目前與未來）播放課程影片時 必須 使用 YoutubePlayer 元件。

若要新增課程單元相關頁面（例如 journeys route），也要遵守本規格：

影片部分使用 YoutubePlayer

進度 / 完成邏輯走同一套 API（user progress & complete unit）

修改元件時必須保留：

onProgress、onEnded 的行為

data-testid="unit-video"（若有用於 E2E）

Claude Code 的工作重點：

✅ 不要重寫播放器核心，只能 重用 YoutubePlayer。

✅ 若後端尚未有對應 API，請依本規格完成。

✅ 單元頁 /units/[unitId] 要整合：

播放器

進度條 / 上次觀看位置

完成單元 + XP 更新 + Toast