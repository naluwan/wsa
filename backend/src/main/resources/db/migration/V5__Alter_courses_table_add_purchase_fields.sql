-- 擴充 courses 表，新增課程購買相關欄位
-- 用於支援課程定價、老師資訊、封面圖片與上架狀態

-- 新增老師名稱欄位
ALTER TABLE courses
  ADD COLUMN teacher_name VARCHAR(255) NOT NULL DEFAULT '水球潘';

-- 新增課程價格欄位（新台幣，單位：元）
ALTER TABLE courses
  ADD COLUMN price_twd INTEGER NOT NULL DEFAULT 0;

-- 新增課程封面圖片 URL 欄位
ALTER TABLE courses
  ADD COLUMN thumbnail_url TEXT;

-- 新增課程上架狀態欄位
ALTER TABLE courses
  ADD COLUMN is_published BOOLEAN NOT NULL DEFAULT TRUE;

-- 更新現有課程的資料，將 cover_icon 資料遷移到 thumbnail_url
UPDATE courses
SET thumbnail_url = CASE
  WHEN cover_icon IS NOT NULL THEN CONCAT('images/', cover_icon, '.png')
  ELSE NULL
END;

-- 為現有課程設定預設價格
UPDATE courses
SET price_twd = 7599
WHERE code IN ('SOFTWARE_DESIGN_PATTERN', 'AI_X_BDD');

UPDATE courses
SET price_twd = 5999
WHERE code NOT IN ('SOFTWARE_DESIGN_PATTERN', 'AI_X_BDD');
