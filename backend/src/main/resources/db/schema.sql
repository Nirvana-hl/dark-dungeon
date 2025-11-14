-- 暗黑地牢肉鸽游戏数据库表结构
-- 使用 H2 数据库（兼容 MySQL 语法）

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 角色表
CREATE TABLE IF NOT EXISTS characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    class VARCHAR(20) NOT NULL COMMENT '职业：战士/法师/游侠',
    level INT DEFAULT 1,
    stars INT DEFAULT 1 COMMENT '星级 1-5',
    hp INT DEFAULT 60,
    mp INT DEFAULT 30,
    stress INT DEFAULT 0 COMMENT '压力值',
    attrs TEXT COMMENT '额外属性 JSON',
    status VARCHAR(20) DEFAULT 'idle' COMMENT '状态：idle/exploring/battling',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 用户钱包表
CREATE TABLE IF NOT EXISTS user_wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    gold INT DEFAULT 500,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 用户卡牌表
CREATE TABLE IF NOT EXISTS user_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT '类型：character/spell/equipment',
    quantity INT DEFAULT 1,
    attack INT DEFAULT 0,
    health INT DEFAULT 0,
    effect VARCHAR(50) COMMENT '效果：fireball3/teamBuffAtk1等',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 角色特性表
CREATE TABLE IF NOT EXISTS character_traits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称',
    trait_key VARCHAR(50) NOT NULL COMMENT '特性键：priest_bless/shield_guard等',
    base_power INT DEFAULT 1,
    power_per_star INT DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 敌方卡牌表
CREATE TABLE IF NOT EXISTS enemy_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stage INT NOT NULL COMMENT '关卡数',
    difficulty VARCHAR(20) NOT NULL COMMENT '难度：普通/困难/噩梦',
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    attack INT DEFAULT 0,
    health INT DEFAULT 0,
    effect VARCHAR(50),
    unique_play TINYINT DEFAULT 0 COMMENT '是否唯一出牌',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 地牢探索记录表
CREATE TABLE IF NOT EXISTS runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    character_id BIGINT,
    dungeon_id INT,
    difficulty VARCHAR(20),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    result VARCHAR(20) COMMENT '结果：victory/defeat/abandon',
    monsters_defeated INT DEFAULT 0,
    duration_min INT DEFAULT 0,
    damage_done INT DEFAULT 0,
    damage_taken INT DEFAULT 0,
    reward_gold INT DEFAULT 0,
    reward_exp INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_characters_user_id ON characters(user_id);
CREATE INDEX IF NOT EXISTS idx_user_cards_user_id ON user_cards(user_id);
CREATE INDEX IF NOT EXISTS idx_enemy_cards_stage_diff ON enemy_cards(stage, difficulty);
CREATE INDEX IF NOT EXISTS idx_runs_user_id ON runs(user_id);

-- 初始化角色特性数据
INSERT INTO character_traits (name, trait_key, base_power, power_per_star, description) VALUES
('祭司', 'priest_bless', 5, 2, '每回合开始时为友方全体恢复生命'),
('盾卫', 'shield_guard', 3, 1, '每回合开始时为一个队友提供护盾')
ON CONFLICT DO NOTHING;

-- 初始化敌方卡牌数据（示例）
INSERT INTO enemy_cards (stage, difficulty, name, type, attack, health, effect, unique_play) VALUES
(1, '普通', '暗影猎手', 'character', 3, 20, NULL, 0),
(1, '普通', '雷电之球', 'spell', 0, 0, 'lightning3', 0),
(1, '困难', '腐化巨龙', 'character', 6, 30, NULL, 0),
(1, '噩梦', '上古恶龙', 'character', 8, 40, NULL, 0)
ON CONFLICT DO NOTHING;

