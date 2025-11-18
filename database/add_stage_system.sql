-- ============================================================
-- 关卡系统数据库迁移脚本
-- 用途：添加关卡系统相关表结构
-- 说明：执行前请先备份数据库
-- ============================================================

SET NAMES utf8mb4;
SET @@foreign_key_checks = 0;

-- ============================================================
-- 1. 新增 stages 表（关卡模板）
-- ============================================================

CREATE TABLE IF NOT EXISTS stages (
    id                BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    stage_number      INT         NOT NULL COMMENT '关卡编号（1, 2, 3...）',
    chapter_number    INT         NOT NULL COMMENT '章节编号（每5关为一章：1, 2, 3...）',
    dungeon_id        BIGINT      COMMENT '关联的地牢模板ID',
    stage_name        VARCHAR(80) NOT NULL COMMENT '关卡名称',
    difficulty        VARCHAR(20) NOT NULL COMMENT '难度：easy/normal/hard/expert',
    is_boss_stage     TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否为Boss关卡（每章第5关）',
    enemy_pool        JSON        COMMENT '敌人池配置（用于随机生成敌人）',
    event_pool        JSON        COMMENT '事件池配置（用于随机生成事件）',
    reward_pool       JSON        COMMENT '奖励池配置（关卡完成奖励）',
    exploration_map   JSON        COMMENT '探索地图配置（房间、路径、探索点）',
    description       TEXT        COMMENT '关卡描述',
    UNIQUE KEY uk_stage_number (stage_number),
    CONSTRAINT fk_stages_dungeon FOREIGN KEY (dungeon_id) REFERENCES dungeons(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- ============================================================
-- 2. 新增 user_stage_progress 表（用户关卡进度）
-- ============================================================

CREATE TABLE IF NOT EXISTS user_stage_progress (
    id            BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT      NOT NULL COMMENT '用户ID',
    stage_number  INT         NOT NULL COMMENT '关卡编号',
    chapter_number INT        NOT NULL COMMENT '章节编号',
    is_passed     TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否已通过',
    is_unlocked   TINYINT(1)  NOT NULL DEFAULT 1 COMMENT '是否已解锁（通过前一关后解锁）',
    best_result   VARCHAR(20) COMMENT '最佳结果：victory/defeat',
    passed_at     TIMESTAMP   COMMENT '首次通过时间',
    attempt_count INT         NOT NULL DEFAULT 0 COMMENT '尝试次数',
    best_score    INT         COMMENT '最佳评分（可选）',
    UNIQUE KEY uk_user_stage (user_id, stage_number),
    CONSTRAINT fk_user_stage_progress_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- ============================================================
-- 3. 修改 runs 表（添加关卡相关字段）
-- ============================================================

-- 检查并添加 stage_number 列
SET @dbname = DATABASE();
SET @tablename = 'runs';
SET @columnname = 'stage_number';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT COMMENT ''当前关卡编号''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 chapter_number 列
SET @columnname = 'chapter_number';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT COMMENT ''当前章节编号''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 current_stage_progress 列
SET @columnname = 'current_stage_progress';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' JSON COMMENT ''关卡内进度（探索进度、已触发事件、已击败敌人）''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================================
-- 4. 修改 events 表（添加触发条件和选择项）
-- ============================================================

SET @tablename = 'events';

-- 检查并添加 trigger_condition 列
SET @columnname = 'trigger_condition';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' JSON COMMENT ''触发条件（关卡编号范围、章节、房间类型等）''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 trigger_chance 列
SET @columnname = 'trigger_chance';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DECIMAL(3,2) COMMENT ''触发概率（0.00-1.00，地牢事件使用）''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 choices 列
SET @columnname = 'choices';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' JSON COMMENT ''选择项配置（如果有多个选择）''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================================
-- 5. 修改 dungeons 表（添加主题字段）
-- ============================================================

SET @tablename = 'dungeons';
SET @columnname = 'theme';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(40) COMMENT ''地牢主题（如：森林、洞穴、废墟等）''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================================
-- 6. 初始化第1关为解锁状态（为所有现有用户）
-- ============================================================

-- 为所有现有用户解锁第1关
INSERT INTO user_stage_progress (user_id, stage_number, chapter_number, is_unlocked, is_passed)
SELECT id, 1, 1, 1, 0 FROM users
ON DUPLICATE KEY UPDATE is_unlocked = 1;

SET @@foreign_key_checks = 1;

-- ============================================================
-- 迁移完成！
-- ============================================================

