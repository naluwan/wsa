-- ============================================================
-- V10: 完整課程與單元種子資料（正式版）
--
-- 特性：
-- 1. 使用固定 UUID，確保 idempotent
-- 2. 所有 INSERT 都使用 ON CONFLICT DO NOTHING
-- 3. 不刪除任何資料，純新增/更新
-- 4. 可安全重跑
-- ============================================================

-- ============================================================
-- 步驟 1: 課程資料
-- ============================================================

-- 課程 1: 軟體設計精通模式之旅
INSERT INTO courses (
  id,
  code,
  title,
  description,
  teacher_name,
  level_tag,
  total_units,
  cover_icon,
  price_twd,
  thumbnail_url,
  is_published
)
VALUES (
  '11111111-1111-1111-1111-111111111111'::uuid,
  'SOFTWARE_DESIGN_PATTERN',
  '軟體設計精通模式之旅',
  '用一趟旅程的時間,成為硬核的 Coding 實戰高手',
  '水球潘',
  'advanced',
  4,
  'software_design_pattern',
  7599,
  'images/course_0.png',
  TRUE
)
ON CONFLICT (code) DO UPDATE SET
  title = EXCLUDED.title,
  description = EXCLUDED.description,
  teacher_name = EXCLUDED.teacher_name,
  level_tag = EXCLUDED.level_tag,
  total_units = EXCLUDED.total_units,
  price_twd = EXCLUDED.price_twd,
  thumbnail_url = EXCLUDED.thumbnail_url,
  is_published = EXCLUDED.is_published;

-- 課程 2: AI x BDD
INSERT INTO courses (
  id,
  code,
  title,
  description,
  teacher_name,
  level_tag,
  total_units,
  cover_icon,
  price_twd,
  thumbnail_url,
  is_published
)
VALUES (
  '22222222-2222-2222-2222-222222222222'::uuid,
  'AI_X_BDD',
  'AI x BDD：規格驅動全自動開發術',
  'AI Top 1% 工程師必修課，掌握規格驅動的全自動化開發',
  '水球潘',
  'advanced',
  2,
  'ai_x_bdd',
  7599,
  'images/course_1.png',
  TRUE
)
ON CONFLICT (code) DO UPDATE SET
  title = EXCLUDED.title,
  description = EXCLUDED.description,
  teacher_name = EXCLUDED.teacher_name,
  level_tag = EXCLUDED.level_tag,
  total_units = EXCLUDED.total_units,
  price_twd = EXCLUDED.price_twd,
  thumbnail_url = EXCLUDED.thumbnail_url,
  is_published = EXCLUDED.is_published;

-- ============================================================
-- 步驟 2: SOFTWARE_DESIGN_PATTERN 課程單元
-- ============================================================

-- 章節一：課程介紹 & 視聽(免費試看)

INSERT INTO units (
  id,
  unit_id,
  course_id,
  section_title,
  title,
  type,
  order_index,
  order_in_section,
  video_url,
  xp_reward,
  is_free_preview
)
VALUES (
  'a1111111-1111-1111-1111-111111111111'::uuid,
  'sdp-intro-course-overview',
  '11111111-1111-1111-1111-111111111111'::uuid,
  '課程介紹 & 視聽(免費試看)',
  '課程介紹：這門課手把手帶你成為架構設計的高手',
  'video',
  1,
  1,
  'https://youtu.be/3GxftuDUBXM?si=Ke5fSlV8pmwqqJVD',
  200,
  TRUE
)
ON CONFLICT (unit_id) DO UPDATE SET
  title = EXCLUDED.title,
  section_title = EXCLUDED.section_title,
  video_url = EXCLUDED.video_url,
  order_index = EXCLUDED.order_index,
  order_in_section = EXCLUDED.order_in_section,
  xp_reward = EXCLUDED.xp_reward,
  is_free_preview = EXCLUDED.is_free_preview;

INSERT INTO units (
  id,
  unit_id,
  course_id,
  section_title,
  title,
  type,
  order_index,
  order_in_section,
  video_url,
  xp_reward,
  is_free_preview
)
VALUES (
  'a1111111-1111-1111-1111-111111111112'::uuid,
  'sdp-intro-ai-era',
  '11111111-1111-1111-1111-111111111111'::uuid,
  '課程介紹 & 視聽(免費試看)',
  '你該知道：在 AI 的時代下，只會下 prompt 絕對寫不出好 Code',
  'video',
  2,
  2,
  'https://youtu.be/UslcIlL-1xo?si=DdVMTdriDkEeqqKO',
  200,
  TRUE
)
ON CONFLICT (unit_id) DO UPDATE SET
  title = EXCLUDED.title,
  section_title = EXCLUDED.section_title,
  video_url = EXCLUDED.video_url,
  order_index = EXCLUDED.order_index,
  order_in_section = EXCLUDED.order_in_section,
  xp_reward = EXCLUDED.xp_reward,
  is_free_preview = EXCLUDED.is_free_preview;

-- 章節二：副本零：冒險者指引

INSERT INTO units (
  id,
  unit_id,
  course_id,
  section_title,
  title,
  type,
  order_index,
  order_in_section,
  video_url,
  xp_reward,
  is_free_preview
)
VALUES (
  'a1111111-1111-1111-1111-111111111113'::uuid,
  'sdp-guide-platform-manual',
  '11111111-1111-1111-1111-111111111111'::uuid,
  '副本零：冒險者指引',
  '平台使用手冊',
  'video',
  3,
  1,
  'https://www.youtube.com/watch?v=platform-manual',
  200,
  FALSE
)
ON CONFLICT (unit_id) DO UPDATE SET
  title = EXCLUDED.title,
  section_title = EXCLUDED.section_title,
  video_url = EXCLUDED.video_url,
  order_index = EXCLUDED.order_index,
  order_in_section = EXCLUDED.order_in_section,
  xp_reward = EXCLUDED.xp_reward,
  is_free_preview = EXCLUDED.is_free_preview;

INSERT INTO units (
  id,
  unit_id,
  course_id,
  section_title,
  title,
  type,
  order_index,
  order_in_section,
  video_url,
  xp_reward,
  is_free_preview
)
VALUES (
  'a1111111-1111-1111-1111-111111111114'::uuid,
  'sdp-guide-astah-pro',
  '11111111-1111-1111-1111-111111111111'::uuid,
  '副本零：冒險者指引',
  '如何使用課程贊助給大家的專業 UML Editor — Astah Pro？',
  'video',
  4,
  2,
  'https://www.youtube.com/watch?v=astah-pro-guide',
  200,
  FALSE
)
ON CONFLICT (unit_id) DO UPDATE SET
  title = EXCLUDED.title,
  section_title = EXCLUDED.section_title,
  video_url = EXCLUDED.video_url,
  order_index = EXCLUDED.order_index,
  order_in_section = EXCLUDED.order_in_section,
  xp_reward = EXCLUDED.xp_reward,
  is_free_preview = EXCLUDED.is_free_preview;

-- ============================================================
-- 步驟 3: AI_X_BDD 課程單元
-- ============================================================

-- 章節一：課程介紹 & 視聽

INSERT INTO units (
  id,
  unit_id,
  course_id,
  section_title,
  title,
  type,
  order_index,
  order_in_section,
  video_url,
  xp_reward,
  is_free_preview
)
VALUES (
  'a2222222-2222-2222-2222-222222222221'::uuid,
  'ai-bdd-intro-course-overview',
  '22222222-2222-2222-2222-222222222222'::uuid,
  '課程介紹 & 視聽',
  '課程介紹：一次到位的 AI Coding，全台最高 CP 值且野心最大的規格驅動開發線上課程',
  'video',
  1,
  1,
  'https://youtu.be/W09vydJH6jo?si=WKym-tH448dhsNu1',
  200,
  FALSE
)
ON CONFLICT (unit_id) DO UPDATE SET
  title = EXCLUDED.title,
  section_title = EXCLUDED.section_title,
  video_url = EXCLUDED.video_url,
  order_index = EXCLUDED.order_index,
  order_in_section = EXCLUDED.order_in_section,
  xp_reward = EXCLUDED.xp_reward,
  is_free_preview = EXCLUDED.is_free_preview;

INSERT INTO units (
  id,
  unit_id,
  course_id,
  section_title,
  title,
  type,
  order_index,
  order_in_section,
  video_url,
  xp_reward,
  is_free_preview
)
VALUES (
  'a2222222-2222-2222-2222-222222222222'::uuid,
  'ai-bdd-intro-strategy-tactics',
  '22222222-2222-2222-2222-222222222222'::uuid,
  '課程介紹 & 視聽',
  '你該知道：戰略戰術設計模式到底追求的是什麼？該怎麼理解它們？',
  'video',
  2,
  2,
  'https://youtu.be/mOJzH0U_3EU?si=fLlbrk1413SAtCX-',
  200,
  FALSE
)
ON CONFLICT (unit_id) DO UPDATE SET
  title = EXCLUDED.title,
  section_title = EXCLUDED.section_title,
  video_url = EXCLUDED.video_url,
  order_index = EXCLUDED.order_index,
  order_in_section = EXCLUDED.order_in_section,
  xp_reward = EXCLUDED.xp_reward,
  is_free_preview = EXCLUDED.is_free_preview;

-- ============================================================
-- 驗證：檢查插入結果
-- ============================================================

-- 此段僅用於本地開發驗證，Heroku Flyway 會忽略 SELECT
DO $$
BEGIN
  RAISE NOTICE '=== 種子資料插入完成 ===';
  RAISE NOTICE '課程數量: %', (SELECT COUNT(*) FROM courses);
  RAISE NOTICE '單元數量: %', (SELECT COUNT(*) FROM units);
END $$;
