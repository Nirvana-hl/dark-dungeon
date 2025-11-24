-- 扩展 runs 表，补齐探索/战斗功能所需字段
-- 执行前请确认数据库版本为 MySQL 8+（支持 IF NOT EXISTS）

ALTER TABLE runs
    ADD COLUMN IF NOT EXISTS user_player_character_id BIGINT NOT NULL DEFAULT 0 AFTER user_id,
    ADD COLUMN IF NOT EXISTS stage_number INT NOT NULL DEFAULT 1 AFTER dungeon_id,
    ADD COLUMN IF NOT EXISTS chapter_number INT NOT NULL DEFAULT 1 AFTER stage_number,
    ADD COLUMN IF NOT EXISTS difficulty VARCHAR(20) NOT NULL DEFAULT 'normal' AFTER chapter_number,
    ADD COLUMN IF NOT EXISTS current_stage_progress JSON NULL AFTER preparation_snapshot,
    ADD COLUMN IF NOT EXISTS reward_snapshot JSON NULL AFTER ended_at;

-- 如果需要，可根据业务把占位默认值更新为真实数据：
-- UPDATE runs SET current_stage_progress = JSON_OBJECT('status','exploring','eventLog',JSON_ARRAY(),'battleLog',JSON_ARRAY()) WHERE current_stage_progress IS NULL;

