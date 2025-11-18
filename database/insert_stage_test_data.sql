-- ============================================================
-- 关卡系统测试数据插入脚本
-- 用途：插入关卡模板和测试数据
-- 说明：执行前请确保已执行 add_stage_system.sql
-- ============================================================

SET NAMES utf8mb4;
SET @@foreign_key_checks = 0;

-- ============================================================
-- 1. 插入地牢模板数据
-- ============================================================

INSERT INTO dungeons (name, difficulty, recommended_cards, theme, description) VALUES
('初始地牢', 'easy', '["holy_light", "iron_wall"]', '森林', '适合新手探索的初始地牢'),
('黑暗走廊', 'normal', '["fireball"]', '洞穴', '充满危险的黑暗走廊'),
('废弃房间', 'normal', '["holy_light"]', '废墟', '废弃已久的房间'),
('深渊入口', 'hard', '["holy_light", "fireball"]', '深渊', '通往深渊的入口'),
('暗影领主巢穴', 'expert', '["holy_light", "fireball", "iron_wall"]', '暗影', '暗影领主的巢穴（Boss关卡）'),
('新章节地牢', 'normal', '["holy_light"]', '森林', '新章节的开始');

-- ============================================================
-- 2. 插入关卡模板数据（第1章：1-5关）
-- ============================================================

-- 第1关
INSERT INTO stages (stage_number, chapter_number, dungeon_id, stage_name, difficulty, is_boss_stage, 
                   enemy_pool, event_pool, reward_pool, exploration_map, description) 
VALUES
(1, 1, 1, '初始地牢', 'easy', 0,
 '{"enemies": [{"id": 1, "weight": 3}, {"id": 2, "weight": 1}]}',
 '{"events": [{"id": 2, "weight": 2}]}',
 '{"gold": 50, "exp": 100, "items": []}',
 '{"rooms": [{"id": 1, "type": "start"}, {"id": 2, "type": "normal"}, {"id": 3, "type": "end"}], "paths": [[1, 2], [2, 3]]}',
 '新手教程关卡，熟悉游戏机制'),

-- 第2关
(2, 1, 2, '黑暗走廊', 'normal', 0,
 '{"enemies": [{"id": 1, "weight": 2}, {"id": 2, "weight": 2}, {"id": 3, "weight": 1}]}',
 '{"events": [{"id": 2, "weight": 1}, {"id": 3, "weight": 1}]}',
 '{"gold": 80, "exp": 150, "items": [1]}',
 '{"rooms": [{"id": 1, "type": "start"}, {"id": 2, "type": "normal"}, {"id": 3, "type": "event"}, {"id": 4, "type": "end"}], "paths": [[1, 2], [2, 3], [3, 4]]}',
 '探索黑暗的走廊，小心敌人'),

-- 第3关
(3, 1, 3, '废弃房间', 'normal', 0,
 '{"enemies": [{"id": 2, "weight": 3}, {"id": 3, "weight": 2}]}',
 '{"events": [{"id": 2, "weight": 2}, {"id": 3, "weight": 1}]}',
 '{"gold": 100, "exp": 200, "items": []}',
 '{"rooms": [{"id": 1, "type": "start"}, {"id": 2, "type": "normal"}, {"id": 3, "type": "normal"}, {"id": 4, "type": "end"}], "paths": [[1, 2], [2, 3], [3, 4]]}',
 '探索废弃的房间，寻找宝藏'),

-- 第4关
(4, 1, 4, '深渊入口', 'hard', 0,
 '{"enemies": [{"id": 3, "weight": 2}, {"id": 2, "weight": 1}]}',
 '{"events": [{"id": 2, "weight": 1}]}',
 '{"gold": 150, "exp": 300, "items": [2]}',
 '{"rooms": [{"id": 1, "type": "start"}, {"id": 2, "type": "normal"}, {"id": 3, "type": "event"}, {"id": 4, "type": "normal"}, {"id": 5, "type": "end"}], "paths": [[1, 2], [2, 3], [3, 4], [4, 5]]}',
 '接近深渊，危险增加'),

-- 第5关（Boss关）
(5, 1, 5, '暗影领主', 'expert', 1,
 '{"enemies": [{"id": 2, "weight": 1}]}',
 '{"events": []}',
 '{"gold": 300, "exp": 500, "items": [1, 2], "cards": [1]}',
 '{"rooms": [{"id": 1, "type": "start"}, {"id": 2, "type": "boss"}], "paths": [[1, 2]]}',
 '第1章Boss：暗影领主');

-- ============================================================
-- 3. 插入地牢事件数据（增强版）
-- ============================================================

-- 更新现有事件，添加触发条件
UPDATE events SET 
    trigger_condition = '{"stage_range": [1, 10], "chapter": 1}',
    trigger_chance = 0.3,
    choices = '[]'
WHERE location_type = 'dungeon' AND name = '暗影伏击';

-- 添加新的事件
INSERT INTO events (name, location_type, trigger_condition, trigger_chance, description, effect_payload, choices) VALUES
('发现宝箱', 'dungeon', 
 '{"stage_range": [1, 20]}', 0.4,
 '你发现了一个宝箱，里面可能有奖励',
 '{"reward": {"gold": 50, "items": [1]}}',
 '[]'),
('神秘商人', 'dungeon',
 '{"stage_range": [1, 15]}', 0.2,
 '遇到神秘商人，可以购买道具',
 '{"shop": true, "discount": 0.2}',
 '[{"text": "购买", "effect": {"cost": 100, "reward": {"items": [1]}}}, {"text": "离开", "effect": {}}]'),
('陷阱触发', 'dungeon',
 '{"stage_range": [1, 20]}', 0.3,
 '你触发了陷阱，受到伤害',
 '{"damage": 10, "stress": 5}',
 '[{"text": "小心避开", "effect": {"stress": 5}}, {"text": "强行通过", "effect": {"damage": 15}}]'),
('恢复泉水', 'dungeon',
 '{"stage_range": [1, 20]}', 0.25,
 '发现恢复泉水，可以恢复生命值',
 '{"heal": 30, "stress_reduce": 10}',
 '[]');

-- ============================================================
-- 验证数据插入
-- ============================================================

SELECT '关卡数量' AS type, COUNT(*) AS count FROM stages
UNION ALL
SELECT '地牢模板', COUNT(*) FROM dungeons
UNION ALL
SELECT '地牢事件', COUNT(*) FROM events WHERE location_type = 'dungeon';

SET @@foreign_key_checks = 1;

