-- ============================================================
-- 暗黑地牢肉鸽（v1.1）数据库初始化脚本 - MySQL 8.0+
-- 用途：在 Navicat / MySQL 环境快速创建核心表结构与示例数据
-- 说明：
--   1. 所有主键使用 CHAR(36) 存储 UUID，默认值为 UUID()
--   2. JSONB 字段改为 MySQL JSON
--   3. PostgreSQL 专属语法（如扩展、::jsonb、ON CONFLICT）
--      已替换为 MySQL 风格
-- ============================================================

SET NAMES utf8mb4;
SET @@foreign_key_checks = 0;

-- ============================================================
-- 1. 通用表 & 账号进度
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id              CHAR(36)      NOT NULL PRIMARY KEY DEFAULT (UUID()),
    account_name    VARCHAR(40)   NOT NULL,
    email           VARCHAR(120),
    player_level    INT           NOT NULL DEFAULT 1,
    player_exp      BIGINT        NOT NULL DEFAULT 0,
    status          ENUM('active','banned','dormant') NOT NULL DEFAULT 'active',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_wallets (
    id            CHAR(36)    NOT NULL PRIMARY KEY DEFAULT (UUID()),
    user_id       CHAR(36)    NOT NULL,
    currency_type VARCHAR(24) NOT NULL,
    balance       BIGINT      NOT NULL DEFAULT 0,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wallet_user_currency (user_id, currency_type),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 2. 玩家角色与压力系统
-- ============================================================

CREATE TABLE IF NOT EXISTS player_characters (
    id           CHAR(36)   NOT NULL PRIMARY KEY DEFAULT (UUID()),
    code         VARCHAR(32) NOT NULL,
    name         VARCHAR(60) NOT NULL,
    base_hp      INT        NOT NULL,
    hp_per_level INT        NOT NULL,
    lore         TEXT,
    UNIQUE KEY uk_player_characters_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_player_characters (
    id                     CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    user_id                CHAR(36) NOT NULL,
    player_character_id    CHAR(36) NOT NULL,
    max_hp                 INT NOT NULL,
    current_hp             INT NOT NULL,
    max_action_points      INT NOT NULL,
    current_action_points  INT NOT NULL,
    current_stress         INT NOT NULL DEFAULT 0,
    stress_level           INT NOT NULL DEFAULT 1,
    stress_debuffs         JSON NOT NULL,
    UNIQUE KEY uk_user_player (user_id, player_character_id),
    CONSTRAINT fk_upc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_upc_template FOREIGN KEY (player_character_id) REFERENCES player_characters(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS stress_debuff_configs (
    id                CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    stress_level      INT NOT NULL,
    debuff_name       VARCHAR(80) NOT NULL,
    debuff_type       ENUM('mental','combat','behavioral') NOT NULL,
    effect_description TEXT,
    trigger_chance    DECIMAL(3,2) NOT NULL,
    effect_payload    JSON NOT NULL,
    is_persistent     TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 3. 卡牌角色、特性与实例
-- ============================================================

CREATE TABLE IF NOT EXISTS card_characters (
    id                   CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    code                 VARCHAR(32) NOT NULL,
    name                 VARCHAR(80) NOT NULL,
    class                VARCHAR(24) NOT NULL,
    faction              VARCHAR(24) NOT NULL,
    rarity               ENUM('common','rare','epic','legendary') NOT NULL,
    base_hp              INT NOT NULL,
    base_attack          INT NOT NULL,
    action_point_cost    INT NOT NULL,
    base_star_level      TINYINT NOT NULL DEFAULT 1,
    max_star_level       TINYINT NOT NULL DEFAULT 5,
    star_upgrade_payload JSON NOT NULL,
    traits               JSON NOT NULL,
    shop_price           INT NOT NULL DEFAULT 0,
    lore                 TEXT,
    UNIQUE KEY uk_card_characters_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_card_characters (
    id                 CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    user_id            CHAR(36) NOT NULL,
    card_character_id  CHAR(36) NOT NULL,
    current_hp         INT NOT NULL,
    current_armor      INT NOT NULL,
    is_deployed        TINYINT(1) NOT NULL DEFAULT 0,
    deployed_round     INT,
    current_star_level TINYINT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_user_card_character (user_id, card_character_id),
    CONSTRAINT fk_ucc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ucc_template FOREIGN KEY (card_character_id) REFERENCES card_characters(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS card_character_traits (
    id                CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    card_character_id CHAR(36) NOT NULL,
    name              VARCHAR(80) NOT NULL,
    type              ENUM('positive','negative','neutral') NOT NULL,
    effect_payload    JSON NOT NULL,
    scaling_payload   JSON NOT NULL,
    description       TEXT,
    KEY idx_traits_card (card_character_id),
    CONSTRAINT fk_traits_card FOREIGN KEY (card_character_id) REFERENCES card_characters(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 4. 玩家技能系统
-- ============================================================

CREATE TABLE IF NOT EXISTS skills (
    id               CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    code             VARCHAR(32) NOT NULL,
    player_character_code VARCHAR(32) NOT NULL,
    name             VARCHAR(80) NOT NULL,
    description      TEXT,
    effect_payload   JSON NOT NULL,
    required_level   INT NOT NULL DEFAULT 1,
    position_in_tree JSON NOT NULL,
    unlock_path      JSON NOT NULL,
    UNIQUE KEY uk_skills_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_player_character_skills (
    id                       CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    user_player_character_id CHAR(36) NOT NULL,
    skill_id                 CHAR(36) NOT NULL,
    unlocked_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_pc_skill (user_player_character_id, skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 5. 统一卡牌池 & 玩家手牌
-- ============================================================

CREATE TABLE IF NOT EXISTS cards (
    id                    CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    code                  VARCHAR(32) NOT NULL,
    name                  VARCHAR(80) NOT NULL,
    card_type             ENUM('spell','equipment') NOT NULL,
    rarity                ENUM('common','rare','epic','legendary') NOT NULL,
    slot_type             ENUM('weapon','armor','trinket','none') NOT NULL,
    action_point_cost     INT NOT NULL DEFAULT 0,
    stat_modifiers        JSON NOT NULL,
    effect_payload        JSON NOT NULL,
    camp_unlock_condition JSON NOT NULL,
    shop_price            JSON NOT NULL,
    description           TEXT,
    UNIQUE KEY uk_cards_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_cards (
    id                              CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    user_id                         CHAR(36) NOT NULL,
    card_id                         CHAR(36) NOT NULL,
    quantity                        INT NOT NULL DEFAULT 1,
    level                           INT NOT NULL DEFAULT 1,
    loadout_id                      CHAR(36),
    equipped_to_user_card_character_id CHAR(36),
    acquired_at                     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acquired_source                 VARCHAR(80),
    last_used_at                    TIMESTAMP NULL,
    UNIQUE KEY uk_user_card_loadout (user_id, card_id, loadout_id),
    CONSTRAINT fk_user_cards_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_cards_card FOREIGN KEY (card_id) REFERENCES cards(id),
    CONSTRAINT fk_user_cards_ucc FOREIGN KEY (equipped_to_user_card_character_id) REFERENCES user_card_characters(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 6. 道具、背包、商城
-- ============================================================

CREATE TABLE IF NOT EXISTS items (
    id            CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    code          VARCHAR(32) NOT NULL,
    name          VARCHAR(80) NOT NULL,
    item_type     ENUM('consumable','material','blueprint','currency_bundle','cosmetic') NOT NULL,
    rarity        ENUM('common','rare','epic','legendary') NOT NULL,
    effect_payload JSON NOT NULL,
    stack_limit   INT NOT NULL DEFAULT 99,
    shop_price    INT NOT NULL DEFAULT 0,
    description   TEXT,
    UNIQUE KEY uk_items_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory (
    id              CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    user_id         CHAR(36) NOT NULL,
    item_id         CHAR(36) NOT NULL,
    quantity        INT NOT NULL DEFAULT 0,
    bind_status     ENUM('unbound','bound') NOT NULL DEFAULT 'unbound',
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_inventory_user_item (user_id, item_id),
    CONSTRAINT fk_inventory_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_item FOREIGN KEY (item_id) REFERENCES items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS shop_offers (
    id            CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    offer_type    ENUM('card','item','bundle') NOT NULL,
    target_id     CHAR(36) NOT NULL,
    price         BIGINT NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    refresh_rule  JSON NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 7. 地牢、敌人、事件与探索
-- ============================================================

CREATE TABLE IF NOT EXISTS dungeons (
    id                CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    name              VARCHAR(80) NOT NULL,
    difficulty        ENUM('easy','normal','hard','expert') NOT NULL,
    recommended_cards JSON NOT NULL,
    description       TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS runs (
    id                        CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    user_id                   CHAR(36) NOT NULL,
    user_player_character_id  CHAR(36),
    dungeon_id                CHAR(36),
    difficulty                VARCHAR(16),
    preparation_snapshot      JSON NOT NULL,
    result                    ENUM('victory','defeat','abandon') NOT NULL,
    started_at                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at                  TIMESTAMP NULL,
    reward_snapshot           JSON NOT NULL,
    KEY idx_runs_user (user_id),
    CONSTRAINT fk_runs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_runs_upc FOREIGN KEY (user_player_character_id) REFERENCES user_player_characters(id),
    CONSTRAINT fk_runs_dungeon FOREIGN KEY (dungeon_id) REFERENCES dungeons(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS enemies (
    id               CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    name             VARCHAR(80) NOT NULL,
    difficulty       ENUM('easy','normal','hard','boss') NOT NULL,
    base_stats       JSON NOT NULL,
    behavior_script  JSON NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS enemy_cards (
    id        CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    enemy_id  CHAR(36) NOT NULL,
    card_id   CHAR(36) NOT NULL,
    weight    INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_enemy_cards_enemy FOREIGN KEY (enemy_id) REFERENCES enemies(id) ON DELETE CASCADE,
    CONSTRAINT fk_enemy_cards_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS events (
    id            CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    name          VARCHAR(80) NOT NULL,
    location_type ENUM('camp','dungeon') NOT NULL,
    description   TEXT,
    effect_payload JSON NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS player_actions (
    id           CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    user_id      CHAR(36) NOT NULL,
    action_type  VARCHAR(64) NOT NULL,
    source_scene ENUM('camp','dungeon','battle') NOT NULL,
    metadata     JSON NOT NULL,
    occurred_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_player_actions_user (user_id),
    CONSTRAINT fk_player_actions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 8. 成就与指标
-- ============================================================

CREATE TABLE IF NOT EXISTS achievements (
    id           CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    name         VARCHAR(80) NOT NULL,
    category     ENUM('progression','mastery','collection','social') NOT NULL,
    description  TEXT,
    requirements JSON NOT NULL,
    UNIQUE KEY uk_achievements_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS game_metrics (
    id                CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    metric_type       VARCHAR(48) NOT NULL,
    date              DATE NOT NULL,
    value             BIGINT NOT NULL,
    dimension_payload JSON NOT NULL,
    UNIQUE KEY uk_metrics_unique (metric_type, date, dimension_payload(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @@foreign_key_checks = 1;

-- ============================================================
-- 9. 示例数据（固定 UUID 便于脚本引用）
-- ============================================================

SET @user_a      = '11111111-1111-1111-1111-111111111111';
SET @user_b      = '22222222-2222-2222-2222-222222222222';
SET @pc_warden   = '33333333-3333-3333-3333-333333333333';
SET @pc_occult   = '44444444-4444-4444-4444-444444444444';
SET @card_priest = '55555555-5555-5555-5555-555555555555';
SET @card_guard  = '66666666-6666-6666-6666-666666666666';
SET @spell_holy  = '77777777-7777-7777-7777-777777777777';
SET @equip_wall  = '88888888-8888-8888-8888-888888888888';
SET @item_elixir = '99999999-9999-9999-9999-999999999999';
SET @item_soul   = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
SET @dun_ashes   = 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb';
SET @dun_void    = 'cccccccc-cccc-cccc-cccc-cccccccccccc';

INSERT INTO users (id, account_name, email, player_level, player_exp)
VALUES
    (@user_a, '灰烬旅者', 'ash@example.com', 12, 14500),
    (@user_b, '夜行者', 'night@example.com', 5, 1200)
ON DUPLICATE KEY UPDATE player_level = VALUES(player_level);

INSERT INTO user_wallets (user_id, currency_type, balance)
VALUES
    (@user_a, 'gold', 3200),
    (@user_a, 'soulstone', 75),
    (@user_b, 'gold', 900)
ON DUPLICATE KEY UPDATE balance = VALUES(balance);

INSERT INTO player_characters (id, code, name, base_hp, hp_per_level, lore)
VALUES
    (@pc_warden, 'warden', '守望者', 80, 12, '古老仪式守护者'),
    (@pc_occult, 'occultist', '秘术师', 60, 9, '操纵禁忌咒语的学者')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO user_player_characters (user_id, player_character_id, max_hp, current_hp, max_action_points, current_action_points, current_stress, stress_level, stress_debuffs)
VALUES
    (@user_a, @pc_warden, 140, 120, 6, 6, 22, 2, JSON_ARRAY())
ON DUPLICATE KEY UPDATE current_hp = VALUES(current_hp);

INSERT INTO stress_debuff_configs (stress_level, debuff_name, debuff_type, effect_description, trigger_chance, effect_payload, is_persistent)
VALUES
    (2, '惊惧低语', 'mental', '回合开始时 30% 概率跳过行动', 0.30, JSON_OBJECT('skip_round', true), 1),
    (3, '血液沸腾', 'combat', '受到攻击时额外承受 15% 伤害', 0.25, JSON_OBJECT('extra_damage_pct', 0.15), 1)
ON DUPLICATE KEY UPDATE trigger_chance = VALUES(trigger_chance);

INSERT INTO card_characters (id, code, name, class, faction, rarity, base_hp, base_attack, action_point_cost, shop_price, traits)
VALUES
    (@card_priest, 'priestess', '星辉祭司', 'support', 'human', 'rare', 40, 8, 2, 280, JSON_ARRAY(JSON_OBJECT('name','全体治疗','value',2))),
    (@card_guard, 'shield_guard', '重甲盾卫', 'protector', 'human', 'common', 55, 6, 1, 160, JSON_ARRAY(JSON_OBJECT('name','护盾','value',3)))
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO card_character_traits (card_character_id, name, type, effect_payload, scaling_payload, description)
VALUES
    (@card_priest, '星辉祝福', 'positive', JSON_OBJECT('heal_allies',2), JSON_OBJECT('2', JSON_OBJECT('heal_allies',3), '3', JSON_OBJECT('heal_allies',4)), '提升全队治疗')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO user_card_characters (user_id, card_character_id, current_hp, current_armor, is_deployed, current_star_level)
VALUES
    (@user_a, @card_priest, 40, 5, 0, 1)
ON DUPLICATE KEY UPDATE current_hp = VALUES(current_hp);

INSERT INTO skills (code, player_character_code, name, description, effect_payload, required_level, position_in_tree, unlock_path)
VALUES
    ('battle_focus', 'warden', '战斗专注', '提升过载上限', JSON_OBJECT('stress_cap_bonus',10), 3, JSON_OBJECT('row',1,'column',2), JSON_ARRAY()),
    ('spirit_shelter', 'occultist', '灵魂庇护', '减免压力获取', JSON_OBJECT('stress_reduction_pct',0.15), 5, JSON_OBJECT('row',2,'column',1), JSON_ARRAY())
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO user_player_character_skills (user_player_character_id, skill_id)
SELECT upc.id, s.id
FROM user_player_characters upc
JOIN player_characters pc ON pc.id = upc.player_character_id AND pc.code = 'warden'
JOIN skills s ON s.code = 'battle_focus'
WHERE upc.user_id = @user_a
ON DUPLICATE KEY UPDATE unlocked_at = unlocked_at;

INSERT INTO cards (id, code, name, card_type, rarity, slot_type, action_point_cost, stat_modifiers, effect_payload, camp_unlock_condition, shop_price, description)
VALUES
    (@spell_holy, 'holy_light', '圣光术', 'spell', 'rare', 'none', 1, JSON_OBJECT('heal',12), JSON_OBJECT('type','heal_single'), JSON_OBJECT(), JSON_OBJECT('currency_type','gold','amount',120), '对友军单体治疗'),
    (@equip_wall, 'iron_wall', '铁壁护甲', 'equipment', 'common', 'armor', 0, JSON_OBJECT('armor',6), JSON_OBJECT('type','buff_armor'), JSON_OBJECT(), JSON_OBJECT('currency_type','gold','amount',90), '强化持有者的护甲')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO user_cards (user_id, card_id, quantity, level, acquired_source)
VALUES
    (@user_a, @spell_holy, 2, 1, 'camp_shop'),
    (@user_a, @equip_wall, 1, 1, 'quest_reward')
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

INSERT INTO items (id, code, name, item_type, rarity, effect_payload, stack_limit, shop_price, description)
VALUES
    (@item_elixir, 'elixir_focus', '专注药剂', 'consumable', 'rare', JSON_OBJECT('restore_action_points',2), 5, 150, '立即恢复 2 点行动点'),
    (@item_soul, 'soul_fragment', '魂晶碎片', 'material', 'epic', JSON_OBJECT('crafting_use','unlock_trait'), 50, 0, '用于高级锻造')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO inventory (user_id, item_id, quantity, bind_status)
VALUES
    (@user_a, @item_elixir, 3, 'unbound')
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

INSERT INTO shop_offers (offer_type, target_id, price, display_order, refresh_rule)
VALUES
    ('card', @spell_holy, 180, 1, JSON_OBJECT('limit_per_day',1,'weight',30)),
    ('item', @item_elixir, 150, 2, JSON_OBJECT('limit_per_day',2))
ON DUPLICATE KEY UPDATE price = VALUES(price);

INSERT INTO dungeons (id, name, difficulty, recommended_cards, description)
VALUES
    (@dun_ashes, '灰烬回廊', 'normal', JSON_ARRAY('holy_light','iron_wall'), '穿越灰烬骑士防线'),
    (@dun_void, '虚空裂隙', 'hard', JSON_ARRAY('holy_light'), '不断湧出的虚空怪物')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO runs (user_id, user_player_character_id, dungeon_id, difficulty, preparation_snapshot, result, reward_snapshot)
SELECT
    @user_a,
    upc.id,
    @dun_ashes,
    'normal',
    JSON_OBJECT('hand', JSON_ARRAY('holy_light','iron_wall'), 'items', JSON_ARRAY('elixir_focus')),
    'victory',
    JSON_OBJECT('gold',260,'items',JSON_ARRAY('soul_fragment'))
FROM user_player_characters upc
WHERE upc.user_id = @user_a
ON DUPLICATE KEY UPDATE result = VALUES(result);

INSERT INTO enemies (name, difficulty, base_stats, behavior_script)
VALUES
    ('暗影猎手', 'normal', JSON_OBJECT('hp',120,'attack',18), JSON_OBJECT('pattern','strike_poison')),
    ('虚空之手', 'boss', JSON_OBJECT('hp',320,'attack',28), JSON_OBJECT('pattern','aoe_then_shield'))
ON DUPLICATE KEY UPDATE difficulty = VALUES(difficulty);

INSERT INTO enemy_cards (enemy_id, card_id, weight)
SELECT e.id, @spell_holy, 2 FROM enemies e WHERE e.name = '暗影猎手'
UNION ALL
SELECT e.id, @equip_wall, 1 FROM enemies e WHERE e.name = '虚空之手';

INSERT INTO events (name, location_type, description, effect_payload)
VALUES
    ('吟游诗人来访', 'camp', '恢复 10 点压力并提供随机卡牌折扣', JSON_OBJECT('stress_adjust',-10,'discount_pct',0.2)),
    ('暗影伏击', 'dungeon', '随机敌人提前行动', JSON_OBJECT('enemy_preemptive',true))
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO player_actions (user_id, action_type, source_scene, metadata)
VALUES
    (@user_a, 'buy_card', 'camp', JSON_OBJECT('card_code','holy_light','cost',180)),
    (@user_a, 'use_item', 'dungeon', JSON_OBJECT('item_code','elixir_focus'))
ON DUPLICATE KEY UPDATE occurred_at = occurred_at;

INSERT INTO achievements (name, category, description, requirements)
VALUES
    ('初入地牢', 'progression', '完成任意一次地牢探索', JSON_OBJECT('runs_completed',1)),
    ('金手指', 'collection', '持有金币超过 3000', JSON_OBJECT('wallet', JSON_OBJECT('currency','gold','min_balance',3000)))
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO game_metrics (metric_type, date, value, dimension_payload)
VALUES
    ('daily_active_users', CURRENT_DATE(), 128, JSON_OBJECT('platform','pc')),
    ('camp_shop_conversion', CURRENT_DATE(), 42, JSON_OBJECT('offer_type','card'))
ON DUPLICATE KEY UPDATE value = VALUES(value);

-- ============================================================
-- 脚本结束
-- ============================================================

