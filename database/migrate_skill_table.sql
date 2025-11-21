-- ============================================
-- 技能系统表结构优化迁移脚本
-- 将 user_player_character_skills 表从关联角色实例ID改为关联用户ID+职业模板ID
-- ============================================

-- 步骤1：备份现有数据（如果有）
-- CREATE TABLE user_player_character_skills_backup AS SELECT * FROM user_player_character_skills;

-- 步骤2：删除旧表
DROP TABLE IF EXISTS user_player_character_skills;

-- 步骤3：创建新表结构
CREATE TABLE user_player_character_skills (
    id                     BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id                BIGINT    NOT NULL COMMENT '用户ID',
    player_character_id    BIGINT    NOT NULL COMMENT '职业模板ID（关联player_characters.id）',
    skill_id               BIGINT    NOT NULL COMMENT '技能模板ID（关联skills.id）',
    unlocked_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    UNIQUE KEY uk_user_character_skill (user_id, player_character_id, skill_id) COMMENT '用户+职业+技能唯一约束，防止重复解锁',
    INDEX idx_user_character (user_id, player_character_id) COMMENT '用于查询用户某个职业的所有技能',
    INDEX idx_skill (skill_id) COMMENT '用于查询技能被哪些用户解锁',
    CONSTRAINT fk_ups_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ups_character FOREIGN KEY (player_character_id) REFERENCES player_characters(id) ON DELETE CASCADE,
    CONSTRAINT fk_ups_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1 COMMENT='用户职业技能解锁记录表';

-- 步骤4：如果有现有数据，需要迁移
-- 注意：此迁移脚本假设现有数据需要从角色实例ID转换为用户ID+职业模板ID
-- 如果表中已有数据，请先执行以下迁移SQL（取消注释）：
/*
INSERT INTO user_player_character_skills (user_id, player_character_id, skill_id, unlocked_at)
SELECT 
    upc.user_id,
    upc.player_character_id,
    upcs_old.skill_id,
    upcs_old.unlocked_at
FROM user_player_character_skills_backup upcs_old
INNER JOIN user_player_characters upc ON upcs_old.user_player_character_id = upc.id;
*/

