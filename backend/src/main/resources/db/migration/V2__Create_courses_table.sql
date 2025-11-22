-- 建立課程資料表
-- 用於儲存平台上所有的課程資訊
CREATE TABLE courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(100) NOT NULL UNIQUE,       -- 課程代碼，用於路由與識別，例如 BACKEND_JAVA
  title VARCHAR(255) NOT NULL,             -- 課程名稱
  slug VARCHAR(255),                       -- URL Slug（可選）
  description TEXT,                        -- 課程描述
  level_tag VARCHAR(50),                   -- 難度標籤：beginner / intermediate / advanced 等
  total_units INTEGER DEFAULT 0 NOT NULL,  -- 單元總數（R1 可先手動維護）
  cover_icon VARCHAR(100),                 -- 前端可用來決定 icon / 圖片
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 建立索引以提升查詢效能
CREATE INDEX idx_courses_code ON courses(code);

-- 插入種子資料
INSERT INTO courses (code, title, description, level_tag, total_units, cover_icon)
VALUES
('BACKEND_JAVA', '後端工程實戰：Java 與 Spring Boot', '從零開始學習後端開發，實作 REST API 與資料庫整合。', 'intermediate', 10, 'backend_java'),
('FRONTEND_REACT', '前端工程實戰：React 與現代前端', '掌握 React 與現代前端開發流程，打造高互動 UI。', 'intermediate', 12, 'frontend_react'),
('SOFTWARE_DESIGN_PATTERN', '軟體設計模式精通之旅', '透過實作掌握常見設計模式與設計原則，寫出可維護的程式碼。', 'advanced', 15, 'software_design_pattern');
