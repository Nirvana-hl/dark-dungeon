-- ============================================================
-- UUID 转 自增主键 迁移脚本
-- 用途：将数据库从UUID主键改为自增主键（BIGINT）
-- 警告：此操作会删除所有现有数据！请先备份！
-- ============================================================

SET NAMES utf8mb4;
SET @@foreign_key_checks = 0;

-- ============================================================
-- 重要提示：执行此脚本前请先备份数据库！
-- ============================================================

-- 备份命令示例：
-- mysqldump -u root -p dark_dungeon > backup_before_migration.sql

-- ============================================================
-- 1. 删除所有表（会清空数据）
-- ============================================================

DROP TABLE IF EXISTS game_metrics;
DROP TABLE IF EXISTS achievements;
DROP TABLE IF EXISTS player_actions;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS enemy_cards;
DROP TABLE IF EXISTS enemies;
DROP TABLE IF EXISTS runs;
DROP TABLE IF EXISTS dungeons;
DROP TABLE IF EXISTS shop_offers;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS user_cards;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS card_character_traits;
DROP TABLE IF EXISTS user_card_characters;
DROP TABLE IF EXISTS card_characters;
DROP TABLE IF EXISTS user_player_character_skills;
DROP TABLE IF EXISTS skills;
DROP TABLE IF EXISTS stress_debuff_configs;
DROP TABLE IF EXISTS user_player_characters;
DROP TABLE IF EXISTS player_characters;
DROP TABLE IF EXISTS user_wallets;
DROP TABLE IF EXISTS users;

-- ============================================================
-- 2. 重新创建表（使用自增主键）
-- ============================================================

-- 用户表
CREATE TABLE users (
    id              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_name    VARCHAR(40) NOT NULL,
    email           VARCHAR(120),
    password        VARCHAR(255),
    player_level    INT         NOT NULL DEFAULT 1,
    player_exp      BIGINT      NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 用户钱包
CREATE TABLE user_wallets (
    id            BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    currency_type VARCHAR(24) NOT NULL,
    balance       BIGINT      NOT NULL DEFAULT 0,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wallet_user_currency (user_id, currency_type),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 玩家角色模板
CREATE TABLE player_characters (
    id           BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code         VARCHAR(32) NOT NULL,
    name         VARCHAR(60) NOT NULL,
    base_hp      INT         NOT NULL,
    hp_per_level INT         NOT NULL,
    lore         TEXT,
    UNIQUE KEY uk_player_characters_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 玩家角色实例
CREATE TABLE user_player_characters (
    id                     BIGINT  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id                BIGINT  NOT NULL,
    player_character_id    BIGINT  NOT NULL,
    max_hp                 INT     NOT NULL,
    current_hp             INT     NOT NULL,
    max_action_points      INT     NOT NULL,
    current_action_points  INT     NOT NULL,
    current_stress         INT     NOT NULL DEFAULT 0,
    stress_level           INT     NOT NULL DEFAULT 1,
    stress_debuffs         JSON    NOT NULL,
    UNIQUE KEY uk_user_player (user_id, player_character_id),
    CONSTRAINT fk_upc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_upc_template FOREIGN KEY (player_character_id) REFERENCES player_characters(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 压力debuff配置
CREATE TABLE stress_debuff_configs (
    id                BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    stress_level      INT         NOT NULL,
    debuff_name       VARCHAR(80) NOT NULL,
    debuff_type       VARCHAR(20) NOT NULL,
    effect_description TEXT,
    trigger_chance    DECIMAL(3,2) NOT NULL,
    effect_payload    JSON        NOT NULL,
    is_persistent     TINYINT(1)  NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 卡牌角色模板
CREATE TABLE card_characters (
    id                   BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code                 VARCHAR(32) NOT NULL,
    name                 VARCHAR(80) NOT NULL,
    class                VARCHAR(24) NOT NULL,
    faction              VARCHAR(24) NOT NULL,
    rarity               VARCHAR(20) NOT NULL,
    base_hp              INT         NOT NULL,
    base_attack          INT         NOT NULL,
    action_point_cost    INT         NOT NULL,
    base_star_level      TINYINT     NOT NULL DEFAULT 1,
    max_star_level       TINYINT     NOT NULL DEFAULT 5,
    star_upgrade_payload JSON        NOT NULL,
    traits               JSON        NOT NULL,
    shop_price           INT         NOT NULL DEFAULT 0,
    lore                 TEXT,
    UNIQUE KEY uk_card_characters_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 卡牌角色特性
CREATE TABLE card_character_traits (
    id                BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    card_character_id BIGINT      NOT NULL,
    name              VARCHAR(80) NOT NULL,
    type              VARCHAR(20) NOT NULL,
    effect_payload    JSON        NOT NULL,
    scaling_payload   JSON        NOT NULL,
    description       TEXT,
    CONSTRAINT fk_traits_card_character FOREIGN KEY (card_character_id) REFERENCES card_characters(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 用户卡牌角色实例
CREATE TABLE user_card_characters (
    id                BIGINT  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT  NOT NULL,
    card_character_id BIGINT  NOT NULL,
    current_hp        INT     NOT NULL,
    current_armor     INT     NOT NULL DEFAULT 0,
    is_deployed       TINYINT(1) NOT NULL DEFAULT 0,
    deployed_round    INT     NOT NULL DEFAULT 0,
    current_star_level TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_ucc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ucc_card_character FOREIGN KEY (card_character_id) REFERENCES card_characters(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 技能模板
CREATE TABLE skills (
    id                    BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code                  VARCHAR(32) NOT NULL,
    player_character_code VARCHAR(32) NOT NULL,
    name                  VARCHAR(80) NOT NULL,
    description           TEXT,
    effect_payload        JSON        NOT NULL,
    required_level        INT         NOT NULL,
    position_in_tree      JSON        NOT NULL,
    unlock_path           JSON        NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 用户技能实例
-- 设计说明：技能解锁关联到"用户+职业模板"，而不是"角色实例"
-- 这样即使删除重建角色实例，技能解锁也不会丢失
CREATE TABLE user_player_character_skills (
    id                     BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id                BIGINT    NOT NULL COMMENT '用户ID',
    player_character_id     BIGINT    NOT NULL COMMENT '职业模板ID（关联player_characters.id）',
    skill_id               BIGINT    NOT NULL COMMENT '技能模板ID（关联skills.id）',
    unlocked_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    UNIQUE KEY uk_user_character_skill (user_id, player_character_id, skill_id) COMMENT '用户+职业+技能唯一约束，防止重复解锁',
    INDEX idx_user_character (user_id, player_character_id) COMMENT '用于查询用户某个职业的所有技能',
    INDEX idx_skill (skill_id) COMMENT '用于查询技能被哪些用户解锁',
    CONSTRAINT fk_ups_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ups_character FOREIGN KEY (player_character_id) REFERENCES player_characters(id) ON DELETE CASCADE,
    CONSTRAINT fk_ups_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1 COMMENT='用户职业技能解锁记录表';

-- 卡牌模板（法术/装备）
CREATE TABLE cards (
    id                      BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code                    VARCHAR(32) NOT NULL,
    name                    VARCHAR(80) NOT NULL,
    card_type               VARCHAR(20) NOT NULL,
    rarity                  VARCHAR(20) NOT NULL,
    slot_type               VARCHAR(20) NOT NULL,
    action_point_cost       INT         NOT NULL,
    stat_modifiers          JSON        NOT NULL,
    effect_payload          JSON        NOT NULL,
    camp_unlock_condition   JSON        NOT NULL,
    shop_price              JSON        NOT NULL,
    description             TEXT,
    UNIQUE KEY uk_cards_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 用户手牌
CREATE TABLE user_cards (
    id                              BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id                         BIGINT    NOT NULL,
    card_id                         BIGINT    NOT NULL,
    quantity                        INT       NOT NULL DEFAULT 1,
    level                           INT       NOT NULL DEFAULT 1,
    loadout_id                      BIGINT,
    equipped_to_user_card_character_id BIGINT,
    acquired_at                     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acquired_source                 VARCHAR(64),
    last_used_at                    TIMESTAMP,
    CONSTRAINT fk_user_cards_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_cards_card FOREIGN KEY (card_id) REFERENCES cards(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 道具模板
CREATE TABLE items (
    id            BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(32) NOT NULL,
    name          VARCHAR(80) NOT NULL,
    item_type     VARCHAR(20) NOT NULL,
    rarity        VARCHAR(20) NOT NULL,
    effect_payload JSON       NOT NULL,
    stack_limit   INT         NOT NULL DEFAULT 99,
    shop_price    INT         NOT NULL DEFAULT 0,
    description   TEXT,
    UNIQUE KEY uk_items_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 用户背包
CREATE TABLE inventory (
    id              BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT    NOT NULL,
    item_id         BIGINT    NOT NULL,
    quantity        INT       NOT NULL DEFAULT 0,
    bind_status     VARCHAR(20) NOT NULL DEFAULT 'unbound',
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_inventory_user_item (user_id, item_id),
    CONSTRAINT fk_inventory_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_item FOREIGN KEY (item_id) REFERENCES items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 商城商品
CREATE TABLE shop_offers (
    id            BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    offer_type    VARCHAR(20) NOT NULL,
    target_id     BIGINT      NOT NULL,
    price         BIGINT      NOT NULL,
    display_order INT         NOT NULL DEFAULT 0,
    refresh_rule  JSON        NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 地牢模板
CREATE TABLE dungeons (
    id                BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(80) NOT NULL,
    difficulty        VARCHAR(20) NOT NULL,
    recommended_cards JSON        NOT NULL,
    description       TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 探索记录
CREATE TABLE runs (
    id                  BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT    NOT NULL,
    dungeon_id          BIGINT    NOT NULL,
    preparation_snapshot JSON     NOT NULL,
    result              VARCHAR(20),
    started_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at            TIMESTAMP,
    CONSTRAINT fk_runs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_runs_dungeon FOREIGN KEY (dungeon_id) REFERENCES dungeons(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 敌人模板
CREATE TABLE enemies (
    id              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(80) NOT NULL,
    difficulty      VARCHAR(20) NOT NULL,
    base_stats      JSON        NOT NULL,
    behavior_script JSON        NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 敌人卡组
CREATE TABLE enemy_cards (
    id       BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    enemy_id BIGINT NOT NULL,
    card_id  BIGINT NOT NULL,
    weight   INT    NOT NULL DEFAULT 1,
    CONSTRAINT fk_enemy_cards_enemy FOREIGN KEY (enemy_id) REFERENCES enemies(id) ON DELETE CASCADE,
    CONSTRAINT fk_enemy_cards_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 事件配置
CREATE TABLE events (
    id            BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(80) NOT NULL,
    location_type VARCHAR(20) NOT NULL,
    description   TEXT,
    effect_payload JSON       NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 玩家行为日志
CREATE TABLE player_actions (
    id           BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    action_type  VARCHAR(64) NOT NULL,
    source_scene VARCHAR(20) NOT NULL,
    metadata     JSON        NOT NULL,
    occurred_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_player_actions_user (user_id),
    CONSTRAINT fk_player_actions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 成就模板
CREATE TABLE achievements (
    id           BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(80) NOT NULL,
    category     VARCHAR(20) NOT NULL,
    description  TEXT,
    requirements JSON        NOT NULL,
    UNIQUE KEY uk_achievements_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

-- 游戏指标
CREATE TABLE game_metrics (
    id                BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    metric_type       VARCHAR(48) NOT NULL,
    date              DATE        NOT NULL,
    value             BIGINT      NOT NULL,
    dimension_payload JSON        NOT NULL,
    UNIQUE KEY uk_metrics_unique (metric_type, date, dimension_payload(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;

SET @@foreign_key_checks = 1;

-- ============================================================
-- 迁移完成！
-- 现在所有表都使用自增主键（BIGINT）
-- ============================================================

