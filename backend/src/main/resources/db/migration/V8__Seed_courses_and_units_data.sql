-- 種子資料：兩門課程與所有單元
-- 根據 R1-Course-Unit-Access-And-Ownership-Spec.md 建立初始資料

-- ============================================================
-- 步驟 1: 清除舊的單元資料（因為 V3 已建立範例單元）
-- ============================================================
DELETE FROM units WHERE unit_id IN ('intro-design-principles', 'solid-overview', 'single-responsibility');

-- ============================================================
-- 步驟 2: 更新現有課程或新增 AI_X_BDD 課程
-- ============================================================

-- 更新 SOFTWARE_DESIGN_PATTERN 課程資訊
UPDATE courses
SET
  title = '軟體設計精通模式之旅',
  description = '用一趟旅程的時間,成為硬核的 Coding 實戰高手',
  teacher_name = '水球潘',
  price_twd = 7599,
  thumbnail_url = 'images/course_0.png',
  is_published = TRUE,
  total_units = 4
WHERE code = 'SOFTWARE_DESIGN_PATTERN';

-- 新增 AI_X_BDD 課程（若不存在）
INSERT INTO courses (code, title, teacher_name, description, price_twd, thumbnail_url, is_published, total_units)
VALUES (
  'AI_X_BDD',
  'AI x BDD：規格驅動全自動開發術',
  '水球潘',
  'AI Top 1% 工程師必修課，掌握規格驅動的全自動化開發',
  7599,
  'images/course_1.png',
  TRUE,
  2
)
ON CONFLICT (code) DO UPDATE
SET
  title = EXCLUDED.title,
  teacher_name = EXCLUDED.teacher_name,
  description = EXCLUDED.description,
  price_twd = EXCLUDED.price_twd,
  thumbnail_url = EXCLUDED.thumbnail_url,
  is_published = EXCLUDED.is_published,
  total_units = EXCLUDED.total_units;

-- ============================================================
-- 步驟 3: 建立 SOFTWARE_DESIGN_PATTERN 課程的單元
-- ============================================================

-- 章節一：課程介紹 & 視聽(免費試看)
INSERT INTO units (
  unit_id, course_id, section_title, title,
  type, order_index, order_in_section,
  video_url, xp_reward, is_free_preview
)
VALUES
(
  'sdp-intro-course-overview',
  (SELECT id FROM courses WHERE code = 'SOFTWARE_DESIGN_PATTERN'),
  '課程介紹 & 視聽(免費試看)',
  '課程介紹：這門課手把手帶你成為架構設計的高手',
  'video',
  1, 1,
  'https://youtu.be/3GxftuDUBXM?si=Ke5fSlV8pmwqqJVD',
  200,
  TRUE
),
(
  'sdp-intro-ai-era',
  (SELECT id FROM courses WHERE code = 'SOFTWARE_DESIGN_PATTERN'),
  '課程介紹 & 視聽(免費試看)',
  '你該知道：在 AI 的時代下，只會下 prompt 絕對寫不出好 Code',
  'video',
  2, 2,
  'https://youtu.be/UslcIlL-1xo?si=DdVMTdriDkEeqqKO',
  200,
  TRUE
);

-- 章節二：副本零：冒險者指引
INSERT INTO units (
  unit_id, course_id, section_title, title,
  type, order_index, order_in_section,
  video_url, xp_reward, is_free_preview
)
VALUES
(
  'sdp-guide-platform-manual',
  (SELECT id FROM courses WHERE code = 'SOFTWARE_DESIGN_PATTERN'),
  '副本零：冒險者指引',
  '平台使用手冊',
  'video',
  3, 1,
  'https://www.youtube.com/watch?v=platform-manual',
  200,
  FALSE
),
(
  'sdp-guide-astah-pro',
  (SELECT id FROM courses WHERE code = 'SOFTWARE_DESIGN_PATTERN'),
  '副本零：冒險者指引',
  '如何使用課程贊助給大家的專業 UML Editor — Astah Pro？',
  'video',
  4, 2,
  'https://www.youtube.com/watch?v=astah-pro-guide',
  200,
  FALSE
);

-- ============================================================
-- 步驟 4: 建立 AI_X_BDD 課程的單元
-- ============================================================

-- 章節一：課程介紹 & 視聽
INSERT INTO units (
  unit_id, course_id, section_title, title,
  type, order_index, order_in_section,
  video_url, xp_reward, is_free_preview
)
VALUES
(
  'ai-bdd-intro-course-overview',
  (SELECT id FROM courses WHERE code = 'AI_X_BDD'),
  '課程介紹 & 視聽',
  '課程介紹：一次到位的 AI Coding，全台最高 CP 值且野心最大的規格驅動開發線上課程',
  'video',
  1, 1,
  'https://youtu.be/W09vydJH6jo?si=WKym-tH448dhsNu1',
  200,
  FALSE
),
(
  'ai-bdd-intro-strategy-tactics',
  (SELECT id FROM courses WHERE code = 'AI_X_BDD'),
  '課程介紹 & 視聽',
  '你該知道：戰略戰術設計模式到底追求的是什麼？該怎麼理解它們？',
  'video',
  2, 2,
  'https://youtu.be/mOJzH0U_3EU?si=fLlbrk1413SAtCX-',
  200,
  FALSE
);
