-- 建立單元資料表
-- 用於儲存課程中的各個學習單元（影片、測驗等）
CREATE TABLE units (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  unit_id VARCHAR(100) NOT NULL UNIQUE,      -- 對外 ID，用於 URL
  course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,  -- 所屬課程（外鍵）
  title VARCHAR(255) NOT NULL,               -- 單元標題
  type VARCHAR(50) NOT NULL,                 -- 單元類型：R1 固定為 video
  order_index INTEGER NOT NULL DEFAULT 0,    -- 單元排序
  video_url TEXT,                            -- 影片網址
  xp_reward INTEGER NOT NULL DEFAULT 100,    -- 完成後獲得的 XP（預設：100）
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 建立索引以提升查詢效能
CREATE INDEX idx_units_unit_id ON units(unit_id);
CREATE INDEX idx_units_course_id ON units(course_id);
CREATE INDEX idx_units_order ON units(course_id, order_index);

-- 插入種子資料（軟體設計模式精通之旅的示例單元）
INSERT INTO units (unit_id, course_id, title, type, order_index, video_url, xp_reward)
SELECT
  'intro-design-principles',
  (SELECT id FROM courses WHERE code = 'SOFTWARE_DESIGN_PATTERN'),
  '設計原則導論',
  'video',
  1,
  'https://www.youtube.com/watch?v=example1',
  200
WHERE EXISTS (SELECT 1 FROM courses WHERE code = 'SOFTWARE_DESIGN_PATTERN');

INSERT INTO units (unit_id, course_id, title, type, order_index, video_url, xp_reward)
SELECT
  'solid-overview',
  (SELECT id FROM courses WHERE code = 'SOFTWARE_DESIGN_PATTERN'),
  'SOLID 原則總覽',
  'video',
  2,
  'https://www.youtube.com/watch?v=example2',
  200
WHERE EXISTS (SELECT 1 FROM courses WHERE code = 'SOFTWARE_DESIGN_PATTERN');

INSERT INTO units (unit_id, course_id, title, type, order_index, video_url, xp_reward)
SELECT
  'single-responsibility',
  (SELECT id FROM courses WHERE code = 'SOFTWARE_DESIGN_PATTERN'),
  '單一職責原則（SRP）',
  'video',
  3,
  'https://www.youtube.com/watch?v=example3',
  150
WHERE EXISTS (SELECT 1 FROM courses WHERE code = 'SOFTWARE_DESIGN_PATTERN');
