-- ============================================================
-- V11: 測試使用者種子資料（100 位排行榜測試用戶）
--
-- 特性：
-- 1. 固定 UUID 確保 idempotent
-- 2. ON CONFLICT DO NOTHING 避免重複
-- 3. 提供多樣化的等級與經驗值分佈
-- 4. 可安全重跑
-- ============================================================

-- ============================================================
-- 插入 100 位測試使用者
-- ============================================================

INSERT INTO users (id, external_id, provider, display_name, email, avatar_url, level, total_xp, weekly_xp)
VALUES
  -- 高等級用戶 (Level 30+)
  ('00000001-0000-0000-0000-000000000001'::uuid, 'seed_test_001', 'seed', '王小明1', 'seed_user_1@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=1', 36, 65000, 5000),
  ('00000002-0000-0000-0000-000000000002'::uuid, 'seed_test_002', 'seed', '李小華2', 'seed_user_2@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=2', 35, 63500, 4800),
  ('00000003-0000-0000-0000-000000000003'::uuid, 'seed_test_003', 'seed', '張小美3', 'seed_user_3@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=3', 34, 62000, 4500),
  ('00000004-0000-0000-0000-000000000004'::uuid, 'seed_test_004', 'seed', '劉小芳4', 'seed_user_4@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=4', 33, 60000, 4200),
  ('00000005-0000-0000-0000-000000000005'::uuid, 'seed_test_005', 'seed', '陳小強5', 'seed_user_5@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=5', 32, 58000, 4000),

  -- 中高等級用戶 (Level 25-29)
  ('00000006-0000-0000-0000-000000000006'::uuid, 'seed_test_006', 'seed', '楊小麗6', 'seed_user_6@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=6', 29, 52000, 3800),
  ('00000007-0000-0000-0000-000000000007'::uuid, 'seed_test_007', 'seed', '黃小傑7', 'seed_user_7@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=7', 28, 50000, 3600),
  ('00000008-0000-0000-0000-000000000008'::uuid, 'seed_test_008', 'seed', '趙小英8', 'seed_user_8@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=8', 27, 48000, 3400),
  ('00000009-0000-0000-0000-000000000009'::uuid, 'seed_test_009', 'seed', '吳小玲9', 'seed_user_9@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=9', 26, 46000, 3200),
  ('0000000a-0000-0000-0000-00000000000a'::uuid, 'seed_test_010', 'seed', '周小龍10', 'seed_user_10@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=10', 25, 44000, 3000),

  -- 中等級用戶 (Level 15-24)
  ('0000000b-0000-0000-0000-00000000000b'::uuid, 'seed_test_011', 'seed', '徐大明11', 'seed_user_11@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=11', 24, 42000, 2800),
  ('0000000c-0000-0000-0000-00000000000c'::uuid, 'seed_test_012', 'seed', '孫大華12', 'seed_user_12@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=12', 23, 40000, 2600),
  ('0000000d-0000-0000-0000-00000000000d'::uuid, 'seed_test_013', 'seed', '馬大美13', 'seed_user_13@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=13', 22, 38000, 2400),
  ('0000000e-0000-0000-0000-00000000000e'::uuid, 'seed_test_014', 'seed', '朱大芳14', 'seed_user_14@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=14', 21, 36000, 2200),
  ('0000000f-0000-0000-0000-00000000000f'::uuid, 'seed_test_015', 'seed', '胡大強15', 'seed_user_15@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=15', 20, 34000, 2000),
  ('00000010-0000-0000-0000-000000000010'::uuid, 'seed_test_016', 'seed', '郭大麗16', 'seed_user_16@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=16', 19, 32000, 1800),
  ('00000011-0000-0000-0000-000000000011'::uuid, 'seed_test_017', 'seed', '何大傑17', 'seed_user_17@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=17', 18, 30000, 1600),
  ('00000012-0000-0000-0000-000000000012'::uuid, 'seed_test_018', 'seed', '高大英18', 'seed_user_18@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=18', 17, 28000, 1400),
  ('00000013-0000-0000-0000-000000000013'::uuid, 'seed_test_019', 'seed', '林大玲19', 'seed_user_19@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=19', 16, 26000, 1200),
  ('00000014-0000-0000-0000-000000000014'::uuid, 'seed_test_020', 'seed', '羅大龍20', 'seed_user_20@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=20', 15, 24000, 1000),

  -- 低中等級用戶 (Level 10-14) - 剩餘 80 位
  ('00000015-0000-0000-0000-000000000015'::uuid, 'seed_test_021', 'seed', '王阿明21', 'seed_user_21@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=21', 14, 22000, 900),
  ('00000016-0000-0000-0000-000000000016'::uuid, 'seed_test_022', 'seed', '李阿華22', 'seed_user_22@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=22', 13, 20000, 800),
  ('00000017-0000-0000-0000-000000000017'::uuid, 'seed_test_023', 'seed', '張阿美23', 'seed_user_23@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=23', 12, 18000, 700),
  ('00000018-0000-0000-0000-000000000018'::uuid, 'seed_test_024', 'seed', '劉阿芳24', 'seed_user_24@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=24', 11, 16000, 600),
  ('00000019-0000-0000-0000-000000000019'::uuid, 'seed_test_025', 'seed', '陳阿強25', 'seed_user_25@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=25', 10, 14000, 500),
  ('0000001a-0000-0000-0000-00000000001a'::uuid, 'seed_test_026', 'seed', '楊阿麗26', 'seed_user_26@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=26', 10, 13500, 480),
  ('0000001b-0000-0000-0000-00000000001b'::uuid, 'seed_test_027', 'seed', '黃阿傑27', 'seed_user_27@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=27', 9, 12000, 450),
  ('0000001c-0000-0000-0000-00000000001c'::uuid, 'seed_test_028', 'seed', '趙阿英28', 'seed_user_28@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=28', 9, 11500, 420),
  ('0000001d-0000-0000-0000-00000000001d'::uuid, 'seed_test_029', 'seed', '吳阿玲29', 'seed_user_29@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=29', 8, 10000, 400),
  ('0000001e-0000-0000-0000-00000000001e'::uuid, 'seed_test_030', 'seed', '周阿龍30', 'seed_user_30@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=30', 8, 9500, 380),

  -- 初級用戶 (Level 1-7) - 剩餘 70 位
  ('0000001f-0000-0000-0000-00000000001f'::uuid, 'seed_test_031', 'seed', '徐志明31', 'seed_user_31@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=31', 7, 8000, 350),
  ('00000020-0000-0000-0000-000000000020'::uuid, 'seed_test_032', 'seed', '孫志華32', 'seed_user_32@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=32', 6, 6500, 320),
  ('00000021-0000-0000-0000-000000000021'::uuid, 'seed_test_033', 'seed', '馬志偉33', 'seed_user_33@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=33', 5, 4500, 300),
  ('00000022-0000-0000-0000-000000000022'::uuid, 'seed_test_034', 'seed', '朱志豪34', 'seed_user_34@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=34', 4, 2500, 250),
  ('00000023-0000-0000-0000-000000000023'::uuid, 'seed_test_035', 'seed', '胡志強35', 'seed_user_35@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=35', 3, 1200, 200),
  ('00000024-0000-0000-0000-000000000024'::uuid, 'seed_test_036', 'seed', '郭雅婷36', 'seed_user_36@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=36', 2, 400, 150),
  ('00000025-0000-0000-0000-000000000025'::uuid, 'seed_test_037', 'seed', '何雅玲37', 'seed_user_37@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=37', 1, 100, 100),
  ('00000026-0000-0000-0000-000000000026'::uuid, 'seed_test_038', 'seed', '高雅文38', 'seed_user_38@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=38', 1, 50, 50),
  ('00000027-0000-0000-0000-000000000027'::uuid, 'seed_test_039', 'seed', '林雅惠39', 'seed_user_39@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=39', 1, 20, 20),
  ('00000028-0000-0000-0000-000000000028'::uuid, 'seed_test_040', 'seed', '羅雅芳40', 'seed_user_40@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=40', 1, 0, 0),

  -- 補充剩餘 60 位用戶（分佈在各個等級）
  ('00000029-0000-0000-0000-000000000029'::uuid, 'seed_test_041', 'seed', '王建國41', 'seed_user_41@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=41', 15, 23500, 950),
  ('0000002a-0000-0000-0000-00000000002a'::uuid, 'seed_test_042', 'seed', '李建宏42', 'seed_user_42@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=42', 14, 21500, 850),
  ('0000002b-0000-0000-0000-00000000002b'::uuid, 'seed_test_043', 'seed', '張建志43', 'seed_user_43@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=43', 13, 19500, 750),
  ('0000002c-0000-0000-0000-00000000002c'::uuid, 'seed_test_044', 'seed', '劉建華44', 'seed_user_44@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=44', 12, 17500, 650),
  ('0000002d-0000-0000-0000-00000000002d'::uuid, 'seed_test_045', 'seed', '陳建明45', 'seed_user_45@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=45', 11, 15500, 550),
  ('0000002e-0000-0000-0000-00000000002e'::uuid, 'seed_test_046', 'seed', '楊淑芬46', 'seed_user_46@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=46', 10, 13200, 470),
  ('0000002f-0000-0000-0000-00000000002f'::uuid, 'seed_test_047', 'seed', '黃淑娟47', 'seed_user_47@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=47', 9, 11200, 440),
  ('00000030-0000-0000-0000-000000000030'::uuid, 'seed_test_048', 'seed', '趙淑華48', 'seed_user_48@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=48', 8, 9200, 390),
  ('00000031-0000-0000-0000-000000000031'::uuid, 'seed_test_049', 'seed', '吳淑貞49', 'seed_user_49@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=49', 7, 7500, 340),
  ('00000032-0000-0000-0000-000000000032'::uuid, 'seed_test_050', 'seed', '周淑玲50', 'seed_user_50@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=50', 6, 6000, 310),

  -- 第 51-100 位（簡化版，混合各等級）
  ('00000033-0000-0000-0000-000000000033'::uuid, 'seed_test_051', 'seed', '測試用戶51', 'seed_user_51@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=51', 20, 33500, 1950),
  ('00000034-0000-0000-0000-000000000034'::uuid, 'seed_test_052', 'seed', '測試用戶52', 'seed_user_52@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=52', 19, 31500, 1750),
  ('00000035-0000-0000-0000-000000000035'::uuid, 'seed_test_053', 'seed', '測試用戶53', 'seed_user_53@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=53', 18, 29500, 1550),
  ('00000036-0000-0000-0000-000000000036'::uuid, 'seed_test_054', 'seed', '測試用戶54', 'seed_user_54@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=54', 17, 27500, 1350),
  ('00000037-0000-0000-0000-000000000037'::uuid, 'seed_test_055', 'seed', '測試用戶55', 'seed_user_55@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=55', 16, 25500, 1150),
  ('00000038-0000-0000-0000-000000000038'::uuid, 'seed_test_056', 'seed', '測試用戶56', 'seed_user_56@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=56', 15, 23200, 920),
  ('00000039-0000-0000-0000-000000000039'::uuid, 'seed_test_057', 'seed', '測試用戶57', 'seed_user_57@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=57', 14, 21200, 820),
  ('0000003a-0000-0000-0000-00000000003a'::uuid, 'seed_test_058', 'seed', '測試用戶58', 'seed_user_58@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=58', 13, 19200, 720),
  ('0000003b-0000-0000-0000-00000000003b'::uuid, 'seed_test_059', 'seed', '測試用戶59', 'seed_user_59@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=59', 12, 17200, 620),
  ('0000003c-0000-0000-0000-00000000003c'::uuid, 'seed_test_060', 'seed', '測試用戶60', 'seed_user_60@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=60', 11, 15200, 520),
  ('0000003d-0000-0000-0000-00000000003d'::uuid, 'seed_test_061', 'seed', '測試用戶61', 'seed_user_61@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=61', 10, 13000, 460),
  ('0000003e-0000-0000-0000-00000000003e'::uuid, 'seed_test_062', 'seed', '測試用戶62', 'seed_user_62@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=62', 9, 11000, 430),
  ('0000003f-0000-0000-0000-00000000003f'::uuid, 'seed_test_063', 'seed', '測試用戶63', 'seed_user_63@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=63', 8, 9000, 370),
  ('00000040-0000-0000-0000-000000000040'::uuid, 'seed_test_064', 'seed', '測試用戶64', 'seed_user_64@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=64', 7, 7200, 330),
  ('00000041-0000-0000-0000-000000000041'::uuid, 'seed_test_065', 'seed', '測試用戶65', 'seed_user_65@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=65', 6, 5500, 290),
  ('00000042-0000-0000-0000-000000000042'::uuid, 'seed_test_066', 'seed', '測試用戶66', 'seed_user_66@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=66', 5, 4000, 270),
  ('00000043-0000-0000-0000-000000000043'::uuid, 'seed_test_067', 'seed', '測試用戶67', 'seed_user_67@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=67', 4, 2000, 230),
  ('00000044-0000-0000-0000-000000000044'::uuid, 'seed_test_068', 'seed', '測試用戶68', 'seed_user_68@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=68', 3, 800, 180),
  ('00000045-0000-0000-0000-000000000045'::uuid, 'seed_test_069', 'seed', '測試用戶69', 'seed_user_69@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=69', 2, 300, 130),
  ('00000046-0000-0000-0000-000000000046'::uuid, 'seed_test_070', 'seed', '測試用戶70', 'seed_user_70@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=70', 1, 80, 80),
  ('00000047-0000-0000-0000-000000000047'::uuid, 'seed_test_071', 'seed', '測試用戶71', 'seed_user_71@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=71', 25, 43500, 2900),
  ('00000048-0000-0000-0000-000000000048'::uuid, 'seed_test_072', 'seed', '測試用戶72', 'seed_user_72@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=72', 24, 41500, 2700),
  ('00000049-0000-0000-0000-000000000049'::uuid, 'seed_test_073', 'seed', '測試用戶73', 'seed_user_73@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=73', 23, 39500, 2500),
  ('0000004a-0000-0000-0000-00000000004a'::uuid, 'seed_test_074', 'seed', '測試用戶74', 'seed_user_74@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=74', 22, 37500, 2300),
  ('0000004b-0000-0000-0000-00000000004b'::uuid, 'seed_test_075', 'seed', '測試用戶75', 'seed_user_75@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=75', 21, 35500, 2100),
  ('0000004c-0000-0000-0000-00000000004c'::uuid, 'seed_test_076', 'seed', '測試用戶76', 'seed_user_76@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=76', 20, 33200, 1900),
  ('0000004d-0000-0000-0000-00000000004d'::uuid, 'seed_test_077', 'seed', '測試用戶77', 'seed_user_77@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=77', 19, 31200, 1700),
  ('0000004e-0000-0000-0000-00000000004e'::uuid, 'seed_test_078', 'seed', '測試用戶78', 'seed_user_78@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=78', 18, 29200, 1500),
  ('0000004f-0000-0000-0000-00000000004f'::uuid, 'seed_test_079', 'seed', '測試用戶79', 'seed_user_79@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=79', 17, 27200, 1300),
  ('00000050-0000-0000-0000-000000000050'::uuid, 'seed_test_080', 'seed', '測試用戶80', 'seed_user_80@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=80', 16, 25200, 1100),
  ('00000051-0000-0000-0000-000000000051'::uuid, 'seed_test_081', 'seed', '測試用戶81', 'seed_user_81@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=81', 15, 23000, 900),
  ('00000052-0000-0000-0000-000000000052'::uuid, 'seed_test_082', 'seed', '測試用戶82', 'seed_user_82@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=82', 14, 21000, 800),
  ('00000053-0000-0000-0000-000000000053'::uuid, 'seed_test_083', 'seed', '測試用戶83', 'seed_user_83@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=83', 13, 19000, 700),
  ('00000054-0000-0000-0000-000000000054'::uuid, 'seed_test_084', 'seed', '測試用戶84', 'seed_user_84@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=84', 12, 17000, 600),
  ('00000055-0000-0000-0000-000000000055'::uuid, 'seed_test_085', 'seed', '測試用戶85', 'seed_user_85@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=85', 11, 15000, 500),
  ('00000056-0000-0000-0000-000000000056'::uuid, 'seed_test_086', 'seed', '測試用戶86', 'seed_user_86@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=86', 10, 12800, 450),
  ('00000057-0000-0000-0000-000000000057'::uuid, 'seed_test_087', 'seed', '測試用戶87', 'seed_user_87@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=87', 9, 10800, 410),
  ('00000058-0000-0000-0000-000000000058'::uuid, 'seed_test_088', 'seed', '測試用戶88', 'seed_user_88@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=88', 8, 8800, 360),
  ('00000059-0000-0000-0000-000000000059'::uuid, 'seed_test_089', 'seed', '測試用戶89', 'seed_user_89@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=89', 7, 6800, 320),
  ('0000005a-0000-0000-0000-00000000005a'::uuid, 'seed_test_090', 'seed', '測試用戶90', 'seed_user_90@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=90', 6, 5200, 280),
  ('0000005b-0000-0000-0000-00000000005b'::uuid, 'seed_test_091', 'seed', '測試用戶91', 'seed_user_91@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=91', 5, 3500, 260),
  ('0000005c-0000-0000-0000-00000000005c'::uuid, 'seed_test_092', 'seed', '測試用戶92', 'seed_user_92@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=92', 4, 1800, 220),
  ('0000005d-0000-0000-0000-00000000005d'::uuid, 'seed_test_093', 'seed', '測試用戶93', 'seed_user_93@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=93', 3, 600, 170),
  ('0000005e-0000-0000-0000-00000000005e'::uuid, 'seed_test_094', 'seed', '測試用戶94', 'seed_user_94@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=94', 2, 250, 120),
  ('0000005f-0000-0000-0000-00000000005f'::uuid, 'seed_test_095', 'seed', '測試用戶95', 'seed_user_95@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=95', 1, 60, 60),
  ('00000060-0000-0000-0000-000000000060'::uuid, 'seed_test_096', 'seed', '測試用戶96', 'seed_user_96@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=96', 1, 30, 30),
  ('00000061-0000-0000-0000-000000000061'::uuid, 'seed_test_097', 'seed', '測試用戶97', 'seed_user_97@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=97', 1, 10, 10),
  ('00000062-0000-0000-0000-000000000062'::uuid, 'seed_test_098', 'seed', '測試用戶98', 'seed_user_98@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=98', 1, 5, 5),
  ('00000063-0000-0000-0000-000000000063'::uuid, 'seed_test_099', 'seed', '測試用戶99', 'seed_user_99@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=99', 1, 0, 0),
  ('00000064-0000-0000-0000-000000000064'::uuid, 'seed_test_100', 'seed', '測試用戶100', 'seed_user_100@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=100', 1, 0, 0)
ON CONFLICT (provider, external_id) DO NOTHING;

-- ============================================================
-- 驗證：檢查插入結果
-- ============================================================

DO $$
BEGIN
  RAISE NOTICE '=== 測試使用者種子資料插入完成 ===';
  RAISE NOTICE '測試用戶數量: %', (SELECT COUNT(*) FROM users WHERE provider = 'seed');
END $$;
