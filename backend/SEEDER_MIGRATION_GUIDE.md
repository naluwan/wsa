# 🔄 Seeder 遷移指引

## 📋 問題摘要

原有的 Seeder 實作有以下問題：
1. **UserSeeder.java** 使用 `CommandLineRunner`，每次啟動都執行
2. **V8 Migration** 使用 DELETE 語句，不夠 idempotent
3. **V2, V3 Migration** 的種子資料混在 CREATE TABLE 中，缺乏 ON CONFLICT 保護

## ✅ 解決方案

已建立兩個新的 Flyway Migration：
- **V10__Seed_courses_and_units_comprehensive.sql** - 課程與單元種子資料（固定 UUID）
- **V11__Seed_test_users.sql** - 100 位測試使用者（固定 UUID）

## 🚀 部署步驟

### 步驟 1：停用舊的 UserSeeder（本地開發）

**方法 A：註解掉 @Component（推薦）**

編輯 `src/main/java/com/wsa/seeder/UserSeeder.java`：

```java
// @Component  // ← 註解掉這一行
@RequiredArgsConstructor
@Slf4j
public class UserSeeder implements CommandLineRunner {
    // ...
}
```

**方法 B：完全刪除檔案**

```bash
rm src/main/java/com/wsa/seeder/UserSeeder.java
```

### 步驟 2：清空 Heroku 上的舊資料

**登入 Heroku Postgres Console：**

```bash
# 方法 1: 使用 Heroku CLI
heroku pg:psql -a your-app-name

# 方法 2: 從 Heroku Dashboard
# Resources → Heroku Postgres → Settings → View Credentials → Dataclips
```

**執行清空 SQL：**

```sql
BEGIN;

-- 刪除所有課程相關資料
DELETE FROM user_unit_progress;
DELETE FROM user_courses;
DELETE FROM units;
DELETE FROM courses;

-- 刪除測試用 seed 使用者（可選）
DELETE FROM users WHERE provider = 'seed';

COMMIT;

-- 驗證清空結果
SELECT 'courses' AS table_name, COUNT(*) AS row_count FROM courses
UNION ALL
SELECT 'units', COUNT(*) FROM units
UNION ALL
SELECT 'user_courses', COUNT(*) FROM user_courses
UNION ALL
SELECT 'user_unit_progress', COUNT(*) FROM user_unit_progress
UNION ALL
SELECT 'seed_users', COUNT(*) FROM users WHERE provider = 'seed';
```

### 步驟 3：重新部署到 Heroku

**Commit 新的 Migrations：**

```bash
git add backend/src/main/resources/db/migration/V10__Seed_courses_and_units_comprehensive.sql
git add backend/src/main/resources/db/migration/V11__Seed_test_users.sql
git add backend/src/main/java/com/wsa/seeder/UserSeeder.java  # 如果修改了
git commit -m "refactor: migrate seeders to Flyway migrations (V10, V11)"
git push heroku main
```

**Flyway 會自動執行：**
- ✅ V10: 插入 2 門課程 + 6 個單元（固定 UUID）
- ✅ V11: 插入 100 位測試使用者（固定 UUID）

### 步驟 4：驗證結果

**檢查 Heroku 日誌：**

```bash
heroku logs --tail -a your-app-name | grep Flyway
```

**應該看到：**
```
Flyway: Migrating schema ... to version 10 - Seed courses and units comprehensive
Flyway: Migrating schema ... to version 11 - Seed test users
```

**檢查資料庫：**

```sql
SELECT * FROM courses;  -- 應該有 2 門課程
SELECT * FROM units;    -- 應該有 6 個單元
SELECT COUNT(*) FROM users WHERE provider = 'seed';  -- 應該有 100 位測試用戶
```

## 📊 新舊 Seeder 對比

| 特性 | 舊做法 (UserSeeder.java) | 新做法 (Flyway V11) |
|------|-------------------------|-------------------|
| **執行時機** | 每次啟動 | Flyway 首次執行 |
| **UUID** | 動態生成 (timestamp) | 固定 UUID |
| **Idempotent** | ⚠️ 有檢查但不完美 | ✅ ON CONFLICT DO NOTHING |
| **效能** | ❌ SELECT * FROM users | ✅ 純 SQL INSERT |
| **可重跑** | ⚠️ 可能產生重複 | ✅ 完全安全 |
| **版本控制** | ❌ 在 Java Code 中 | ✅ 在 Flyway Migration 中 |

## 🔍 常見問題

### Q1: 如果 Heroku 已經執行過 V8，會怎樣？

A: V10 使用 `ON CONFLICT DO UPDATE`，會更新現有資料，不會產生重複。

### Q2: 本地開發如何測試？

```bash
# 1. 清空本地資料庫
docker-compose down -v
docker-compose up -d

# 2. 重新編譯並啟動
mvn clean package -DskipTests
java -jar target/wsa-backend-1.0.0.jar

# 3. 檢查日誌
# 應該看到 Flyway 執行 V10, V11
```

### Q3: 如果想修改種子資料怎麼辦？

**不要修改 V10, V11！** 應該建立新的 Migration：

```bash
# 例如：V12__Update_course_descriptions.sql
UPDATE courses
SET description = '新的描述'
WHERE code = 'SOFTWARE_DESIGN_PATTERN';
```

## ⚠️ 注意事項

1. **V10, V11 只會執行一次**：Flyway 會記錄在 `flyway_schema_history` 表中
2. **固定 UUID 確保 idempotent**：即使重跑也不會產生新資料
3. **ON CONFLICT 保護**：確保不會違反 UNIQUE constraint
4. **不刪除舊的 V2, V3, V8**：Flyway 需要完整的 migration history

## ✨ 最佳實踐

### ✅ 建議做法
- 所有種子資料都使用 Flyway Migration
- 使用固定 UUID（不要用 gen_random_uuid()）
- 使用 ON CONFLICT DO NOTHING 或 DO UPDATE
- 不要在 Migration 中使用 DELETE（除非真的需要）

### ❌ 不建議做法
- ~~使用 CommandLineRunner 塞資料~~
- ~~使用動態生成的 ID~~
- ~~在 CREATE TABLE migration 中混入種子資料~~
- ~~使用 DELETE 然後 INSERT~~

## 📚 相關文件

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL ON CONFLICT](https://www.postgresql.org/docs/current/sql-insert.html#SQL-ON-CONFLICT)
- [UUID Best Practices](https://www.postgresql.org/docs/current/datatype-uuid.html)

---

**完成日期**: 2025-11-24
**負責人**: Claude Code
**狀態**: ✅ 已完成
