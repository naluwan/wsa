-- 建立使用者單元完成紀錄資料表
-- 用於追蹤使用者完成了哪些單元，避免重複加 XP
CREATE TABLE user_unit_progress (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,  -- 使用者 ID（外鍵）
  unit_id UUID NOT NULL REFERENCES units(id) ON DELETE CASCADE,  -- 單元 ID（外鍵）
  completed_at TIMESTAMP NOT NULL DEFAULT NOW(),                 -- 完成時間
  UNIQUE(user_id, unit_id)                                       -- 確保每個使用者對每個單元只能有一筆完成紀錄
);

-- 建立索引以提升查詢效能
CREATE INDEX idx_user_unit_progress_user_id ON user_unit_progress(user_id);
CREATE INDEX idx_user_unit_progress_unit_id ON user_unit_progress(unit_id);
