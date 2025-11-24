-- 用户成就关联表
-- 用于记录每个用户的成就完成状态
-- 遵循项目的"模板/实例分离"设计原则

CREATE TABLE IF NOT EXISTS user_achievements (
    id              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT      NOT NULL COMMENT '用户ID（关联users.id）',
    achievement_id  BIGINT      NOT NULL COMMENT '成就ID（关联achievements.id）',
    is_completed    TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否已完成（0-未完成，1-已完成）',
    progress        INT         NOT NULL DEFAULT 0 COMMENT '完成进度（0-100）',
    completed_at    TIMESTAMP   NULL COMMENT '完成时间（NULL表示未完成）',
    reward_claimed  TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否已领取奖励（0-未领取，1-已领取）',
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（首次触发该成就时）',
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_achievement (user_id, achievement_id) COMMENT '确保每个用户每个成就只有一条记录',
    KEY idx_user_achievements_user (user_id) COMMENT '按用户查询索引',
    KEY idx_user_achievements_achievement (achievement_id) COMMENT '按成就查询索引',
    KEY idx_user_achievements_completed (user_id, is_completed) COMMENT '按用户和完成状态查询索引',
    CONSTRAINT fk_user_achievements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_achievements_achievement FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1 COMMENT='用户成就实例表';

