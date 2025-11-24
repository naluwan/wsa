-- 變更使用者單元進度表：新增觀看進度追蹤欄位
-- 用途：
--   1. 紀錄使用者最後觀看到的影片秒數（last_position_seconds）
--   2. 紀錄最後一次觀看的時間（last_watched_at）
--   3. 將 completed_at 改為可 null（進度紀錄時為 null，完成單元時才設值）

-- 步驟 1：將 completed_at 改為可 null（原本是 NOT NULL）
-- 原因：進度紀錄（觀看中）與完成單元是不同的行為
--       - 觀看進度更新時，completed_at 應為 NULL
--       - 完成單元時，completed_at 才設為目前時間
ALTER TABLE user_unit_progress
  ALTER COLUMN completed_at DROP NOT NULL,
  ALTER COLUMN completed_at DROP DEFAULT;

-- 步驟 2：新增最後觀看秒數欄位
-- 用途：記錄使用者上次觀看影片時的秒數位置，讓使用者下次可以從此位置繼續觀看
-- 預設值：0（表示尚未觀看）
ALTER TABLE user_unit_progress
  ADD COLUMN last_position_seconds INTEGER DEFAULT 0;

-- 步驟 3：新增最後觀看時間欄位
-- 用途：記錄使用者最後一次觀看此單元的時間，可用於分析使用者學習行為
ALTER TABLE user_unit_progress
  ADD COLUMN last_watched_at TIMESTAMP;

-- 步驟 4：更新現有資料
-- 將已存在的完成紀錄（completed_at 不為 null）的 last_watched_at 設為 completed_at
-- 原因：舊資料的完成時間可以視為最後觀看時間
UPDATE user_unit_progress
  SET last_watched_at = completed_at
  WHERE completed_at IS NOT NULL;
