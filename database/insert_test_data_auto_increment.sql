-- ============================================================
-- 测试数据插入脚本（自增主键版本）
-- 用途：插入游戏测试数据，方便开发和测试
-- 说明：执行前请确保已执行 migrate_uuid_to_auto_increment.sql
-- ============================================================

SET NAMES utf8mb4;
SET @@foreign_key_checks = 0;

-- ============================================================
-- 1. 用户数据
-- ============================================================

INSERT INTO users (account_name, email, player_level, player_exp, status) VALUES
('测试玩家1', 'test1@example.com', 5, 4500, 'active'),
('测试玩家2', 'test2@example.com', 3, 2000, 'active');

-- 插入用户钱包（使用LAST_INSERT_ID()获取刚插入的用户ID）
INSERT INTO user_wallets (user_id, currency_type, balance) VALUES
(1, 'gold', 5000),
(1, 'soulstone', 100),
(2, 'gold', 2000);

-- ============================================================
-- 2. 玩家角色模板数据
-- ============================================================

INSERT INTO player_characters (code, name, base_hp, hp_per_level, lore) VALUES
('warden', '守望者', 80, 12, '古老仪式守护者，擅长防御和支援'),
('occultist', '秘术师', 60, 9, '操纵禁忌咒语的学者，精通暗影魔法'),
('ranger', '游侠', 70, 10, '森林中的猎手，擅长远程攻击和追踪'),
('warrior', '战士', 100, 15, '勇猛的近战战士，拥有强大的物理攻击力');

-- ============================================================
-- 3. 玩家角色实例数据
-- ============================================================

-- 为测试用户1创建守望者角色实例（等级5）
INSERT INTO user_player_characters (user_id, player_character_id, max_hp, current_hp, 
                                   max_action_points, current_action_points, current_stress, stress_level, stress_debuffs) 
VALUES
(1, 1, 128, 120, 8, 8, 15, 1, '[]');

-- 为测试用户2创建秘术师角色实例（等级3）
INSERT INTO user_player_characters (user_id, player_character_id, max_hp, current_hp, 
                                   max_action_points, current_action_points, current_stress, stress_level, stress_debuffs) 
VALUES
(2, 2, 78, 70, 6, 6, 30, 2, '[]');

-- ============================================================
-- 4. 压力debuff配置数据
-- ============================================================

INSERT INTO stress_debuff_configs (stress_level, debuff_name, debuff_type, effect_description, 
                                  trigger_chance, effect_payload, is_persistent) VALUES
(1, '轻微焦虑', 'mental', '回合开始时10%概率跳过行动', 0.10, 
 '{"skip_round": true, "duration": 1}', 1),
(2, '惊惧低语', 'mental', '回合开始时30%概率跳过行动', 0.30, 
 '{"skip_round": true, "duration": 2}', 1),
(2, '注意力分散', 'combat', '攻击命中率降低15%', 0.25, 
 '{"hit_rate_reduction": 0.15}', 1),
(3, '血液沸腾', 'combat', '受到攻击时额外承受15%伤害', 0.25, 
 '{"extra_damage_pct": 0.15}', 1),
(3, '恐慌发作', 'behavioral', '无法使用主动技能', 0.20, 
 '{"disable_active_skills": true}', 1),
(4, '精神崩溃', 'mental', '每回合损失5点血量', 0.50, 
 '{"hp_loss_per_round": 5}', 1);

-- ============================================================
-- 5. 卡牌角色模板数据
-- ============================================================

INSERT INTO card_characters (code, name, class, faction, rarity, base_hp, base_attack, 
                            action_point_cost, base_star_level, max_star_level, star_upgrade_payload, 
                            traits, shop_price, lore) VALUES
('priestess', '星辉祭司', 'support', 'human', 'rare', 40, 8, 2, 1, 5,
 '{"stats": {"hp": 10, "attack": 2}, "traits": {"heal_allies": {"2": 1, "3": 2}}}',
 '[{"name": "全体治疗", "value": 2}]', 280, '神圣的治愈者，能够恢复队友的生命'),
('shield_guard', '重甲盾卫', 'protector', 'human', 'common', 55, 6, 1, 1, 5,
 '{"stats": {"hp": 15, "armor": 3}}',
 '[{"name": "护盾", "value": 3}]', 160, '坚不可摧的防御者'),
('fire_mage', '火焰法师', 'mage', 'human', 'epic', 35, 25, 3, 1, 5,
 '{"stats": {"hp": 8, "attack": 5}, "traits": {"fire_damage": {"2": 5, "3": 10}}}',
 '[{"name": "火焰冲击", "value": 25}]', 500, '掌控火焰的毁灭法师'),
('shadow_assassin', '暗影刺客', 'assassin', 'occult', 'epic', 30, 30, 2, 1, 5,
 '{"stats": {"hp": 5, "attack": 8}, "traits": {"critical_chance": {"2": 0.1, "3": 0.2}}}',
 '[{"name": "致命一击", "value": 30}]', 600, '暗影中的致命杀手');

-- ============================================================
-- 6. 卡牌角色特性数据
-- ============================================================

INSERT INTO card_character_traits (card_character_id, name, type, effect_payload, scaling_payload, description) 
VALUES
(1, '星辉祝福', 'positive', '{"heal_allies": 2}', 
 '{"2": {"heal_allies": 3}, "3": {"heal_allies": 4}, "4": {"heal_allies": 5}}',
 '提升全队治疗量'),
(2, '钢铁护盾', 'positive', '{"armor_bonus": 3}', 
 '{"2": {"armor_bonus": 5}, "3": {"armor_bonus": 8}}',
 '提供额外护甲');

-- ============================================================
-- 7. 用户卡牌角色实例数据
-- ============================================================

-- 测试用户1拥有星辉祭司和重甲盾卫
INSERT INTO user_card_characters (user_id, card_character_id, current_hp, current_armor, 
                                 is_deployed, current_star_level)
VALUES
(1, 1, 40, 0, 0, 1),
(1, 2, 55, 0, 0, 1);

-- ============================================================
-- 8. 卡牌（法术/装备）模板数据
-- ============================================================

INSERT INTO cards (code, name, card_type, rarity, slot_type, action_point_cost, 
                  stat_modifiers, effect_payload, camp_unlock_condition, shop_price, description) VALUES
('holy_light', '圣光术', 'spell', 'rare', 'none', 1,
 '{}',
 '{"type": "heal_single", "heal_amount": 12}',
 '{}',
 '{"currency_type": "gold", "amount": 120}',
 '对友军单体治疗12点生命值'),
('fireball', '火球术', 'spell', 'common', 'none', 2,
 '{}',
 '{"type": "damage_single", "damage": 30}',
 '{}',
 '{"currency_type": "gold", "amount": 80}',
 '对单个敌人造成30点火焰伤害'),
('iron_wall', '铁壁护甲', 'equipment', 'common', 'armor', 0,
 '{"armor": 6}',
 '{"type": "buff_armor"}',
 '{}',
 '{"currency_type": "gold", "amount": 90}',
 '强化持有者的护甲，提供6点护甲值'),
('blood_sword', '血刃', 'equipment', 'epic', 'weapon', 0,
 '{"attack": 15, "lifesteal": 0.1}',
 '{"type": "buff_attack", "lifesteal": true}',
 '{}',
 '{"currency_type": "gold", "amount": 300}',
 '强大的武器，攻击力+15，攻击时恢复10%伤害的生命值');

-- ============================================================
-- 9. 用户手牌数据
-- ============================================================

INSERT INTO user_cards (user_id, card_id, quantity, level, acquired_source) 
VALUES
(1, 1, 2, 1, 'camp_shop'),
(1, 3, 1, 1, 'quest_reward');

-- ============================================================
-- 10. 道具模板数据
-- ============================================================

INSERT INTO items (code, name, item_type, rarity, effect_payload, stack_limit, shop_price, description) VALUES
('elixir_focus', '专注药剂', 'consumable', 'rare', 
 '{"restore_action_points": 2}', 5, 150, '立即恢复2点行动点'),
('healing_potion', '治疗药水', 'consumable', 'common', 
 '{"heal_amount": 30}', 10, 50, '恢复30点生命值'),
('soul_fragment', '魂晶碎片', 'material', 'epic', 
 '{"crafting_use": "unlock_trait"}', 50, 0, '用于高级锻造和特性解锁'),
('stress_relief', '镇静剂', 'consumable', 'rare', 
 '{"reduce_stress": 20}', 3, 200, '减少20点压力值');

-- ============================================================
-- 11. 用户背包数据
-- ============================================================

INSERT INTO inventory (user_id, item_id, quantity, bind_status) 
VALUES
(1, 1, 3, 'unbound'),
(1, 2, 5, 'unbound');

-- ============================================================
-- 12. 商城商品数据
-- ============================================================

INSERT INTO shop_offers (offer_type, target_id, price, display_order, refresh_rule) 
VALUES
('card', 1, 180, 1, '{"limit_per_day": 1, "weight": 30}'),
('item', 1, 150, 2, '{"limit_per_day": 2}');

-- ============================================================
-- 13. 地牢模板数据
-- ============================================================

INSERT INTO dungeons (name, difficulty, recommended_cards, description) VALUES
('灰烬回廊', 'normal', 
 '["holy_light", "iron_wall"]', 
 '穿越灰烬骑士防线，小心他们的火焰攻击'),
('虚空裂隙', 'hard', 
 '["holy_light"]', 
 '不断湧出的虚空怪物，需要强大的治疗能力'),
('暗影森林', 'easy', 
 '["fireball"]', 
 '适合新手探索的森林地牢');

-- ============================================================
-- 14. 敌人模板数据
-- ============================================================

INSERT INTO enemies (name, difficulty, base_stats, behavior_script) VALUES
('暗影猎手', 'normal', 
 '{"hp": 120, "attack": 18, "armor": 5}', 
 '{"pattern": "strike_poison", "priority": "low_hp"}'),
('虚空之手', 'boss', 
 '{"hp": 320, "attack": 28, "armor": 10}', 
 '{"pattern": "aoe_then_shield", "priority": "all"}'),
('灰烬骑士', 'normal', 
 '{"hp": 100, "attack": 20, "armor": 8}', 
 '{"pattern": "fire_attack", "priority": "front"}');

-- ============================================================
-- 15. 敌人卡组数据
-- ============================================================

INSERT INTO enemy_cards (enemy_id, card_id, weight) 
VALUES
(1, 2, 2);

-- ============================================================
-- 16. 事件数据
-- ============================================================

INSERT INTO events (name, location_type, description, effect_payload) VALUES
('吟游诗人来访', 'camp', 
 '恢复10点压力并提供随机卡牌折扣', 
 '{"stress_adjust": -10, "discount_pct": 0.2}'),
('暗影伏击', 'dungeon', 
 '随机敌人提前行动', 
 '{"enemy_preemptive": true}'),
('神秘商人', 'camp', 
 '可以购买稀有道具', 
 '{"shop_refresh": true, "rare_items": true}');

SET @@foreign_key_checks = 1;

-- ============================================================
-- 验证数据插入
-- ============================================================

SELECT '用户数量' AS type, COUNT(*) AS count FROM users
UNION ALL
SELECT '玩家角色模板', COUNT(*) FROM player_characters
UNION ALL
SELECT '玩家角色实例', COUNT(*) FROM user_player_characters
UNION ALL
SELECT '卡牌角色模板', COUNT(*) FROM card_characters
UNION ALL
SELECT '卡牌模板', COUNT(*) FROM cards
UNION ALL
SELECT '道具模板', COUNT(*) FROM items
UNION ALL
SELECT '压力debuff配置', COUNT(*) FROM stress_debuff_configs;

