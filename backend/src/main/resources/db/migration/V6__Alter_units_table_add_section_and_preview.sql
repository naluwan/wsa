-- 擴充 units 表，新增章節分組與免費試看相關欄位
-- 用於支援 Accordion 章節顯示與免費試看功能

-- 新增章節標題欄位（用於 Accordion 分組）
ALTER TABLE units
  ADD COLUMN section_title VARCHAR(255) NOT NULL DEFAULT '未分類章節';

-- 新增免費試看標記欄位
ALTER TABLE units
  ADD COLUMN is_free_preview BOOLEAN NOT NULL DEFAULT FALSE;

-- 新增章節內排序欄位
ALTER TABLE units
  ADD COLUMN order_in_section INTEGER NOT NULL DEFAULT 0;

-- 建立索引以提升查詢效能
CREATE INDEX idx_units_section ON units(course_id, section_title, order_in_section);
CREATE INDEX idx_units_free_preview ON units(is_free_preview);
