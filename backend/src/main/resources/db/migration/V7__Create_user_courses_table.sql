-- 建立使用者課程擁有權資料表
-- 用於記錄使用者購買/擁有的課程，實現課程存取權限控制

CREATE TABLE user_courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  purchased_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

  -- 確保同一使用者不會重複購買同一門課程
  UNIQUE(user_id, course_id)
);

-- 建立索引以提升查詢效能
CREATE INDEX idx_user_courses_user_id ON user_courses(user_id);
CREATE INDEX idx_user_courses_course_id ON user_courses(course_id);
CREATE INDEX idx_user_courses_purchased_at ON user_courses(purchased_at);

-- 新增註解說明
COMMENT ON TABLE user_courses IS '使用者課程擁有權表，記錄使用者購買的課程';
COMMENT ON COLUMN user_courses.user_id IS '使用者 ID（外鍵關聯 users 表）';
COMMENT ON COLUMN user_courses.course_id IS '課程 ID（外鍵關聯 courses 表）';
COMMENT ON COLUMN user_courses.purchased_at IS '購買時間';
