-- ============================================
-- 商城测试数据SQL（修复版 - 转义JSON字符串）
-- 包含：角色、法术、装备、道具等商品
-- ============================================

-- 1. 插入卡牌角色（card_characters）
-- ============================================
INSERT INTO card_characters (code, name, class, faction, rarity, base_hp, base_attack, action_point_cost, base_star_level, max_star_level, star_upgrade_payload, traits, shop_price, lore) VALUES
-- 普通角色
('warrior_common', '新兵战士', 'warrior', 'human', 'common', 80, 15, 2, 1, 5, '{\"stats\":{\"hp\":10,\"attack\":3}}', '[]', 300, '一名训练有素的新兵，擅长近战。'),
('ranger_common', '见习游侠', 'ranger', 'human', 'common', 70, 18, 2, 1, 5, '{\"stats\":{\"hp\":8,\"attack\":4}}', '[]', 300, '年轻的游侠，擅长远程攻击。'),
('priest_common', '初级祭司', 'priest', 'human', 'common', 60, 10, 2, 1, 5, '{\"stats\":{\"hp\":6,\"attack\":2}}', '[]', 300, '初出茅庐的祭司，能够治疗队友。'),

-- 稀有角色
('warrior_rare', '精英战士', 'warrior', 'human', 'rare', 100, 20, 2, 2, 5, '{\"stats\":{\"hp\":12,\"attack\":4},\"traits\":{\"shield_bash\":{\"2\":1,\"3\":2}}}', '[{\"name\":\"盾击\",\"type\":\"positive\",\"effect_payload\":{\"stun_chance\":0.2}}]', 800, '经验丰富的战士，能够使用盾击。'),
('occultist_rare', '神秘学者', 'occultist', 'occult', 'rare', 65, 25, 3, 2, 5, '{\"stats\":{\"hp\":7,\"attack\":5},\"traits\":{\"dark_bolt\":{\"2\":2,\"3\":3}}}', '[{\"name\":\"暗影箭\",\"type\":\"positive\",\"effect_payload\":{\"damage_multiplier\":1.2}}]', 800, '掌握黑暗魔法的学者。'),
('ranger_rare', '资深游侠', 'ranger', 'human', 'rare', 85, 22, 2, 2, 5, '{\"stats\":{\"hp\":9,\"attack\":5},\"traits\":{\"multi_shot\":{\"2\":1,\"3\":2}}}', '[{\"name\":\"多重射击\",\"type\":\"positive\",\"effect_payload\":{\"targets\":2}}]', 800, '技艺精湛的游侠，能够同时攻击多个目标。'),

-- 史诗角色
('warrior_epic', '传奇战士', 'warrior', 'human', 'epic', 120, 25, 2, 3, 5, '{\"stats\":{\"hp\":15,\"attack\":6},\"traits\":{\"berserker_rage\":{\"2\":1,\"3\":2,\"4\":3}}}', '[{\"name\":\"狂战士之怒\",\"type\":\"positive\",\"effect_payload\":{\"attack_bonus\":10,\"duration\":3}}]', 2000, '传说中的战士，拥有强大的战斗意志。'),
('priest_epic', '大祭司', 'priest', 'divine', 'epic', 90, 15, 2, 3, 5, '{\"stats\":{\"hp\":10,\"attack\":3},\"traits\":{\"divine_heal\":{\"2\":2,\"3\":3,\"4\":4}}}', '[{\"name\":\"神圣治疗\",\"type\":\"positive\",\"effect_payload\":{\"heal_all\":30}}]', 2000, '受到神祇祝福的大祭司，能够治愈所有队友。'),

-- 传说角色
('dragon_knight', '龙骑士', 'warrior', 'divine', 'legendary', 150, 30, 3, 3, 5, '{\"stats\":{\"hp\":20,\"attack\":8},\"traits\":{\"dragon_breath\":{\"2\":1,\"3\":2,\"4\":3,\"5\":4}}}', '[{\"name\":\"龙息\",\"type\":\"positive\",\"effect_payload\":{\"aoe_damage\":50,\"burn_chance\":0.5}}]', 5000, '传说中的龙骑士，能够驾驭巨龙的力量。');

-- 2. 插入法术卡牌（cards - spell）
-- ============================================
INSERT INTO cards (code, name, card_type, rarity, slot_type, action_point_cost, stat_modifiers, effect_payload, camp_unlock_condition, shop_price, description) VALUES
-- 普通法术
('fireball', '火球术', 'spell', 'common', 'none', 2, '{}', '{\"damage\":30,\"target\":\"enemy\"}', '{}', '{\"currency_type\":\"gold\",\"amount\":200}', '发射一颗火球，对敌人造成30点伤害。'),
('heal', '治疗术', 'spell', 'common', 'none', 2, '{}', '{\"heal\":25,\"target\":\"ally\"}', '{}', '{\"currency_type\":\"gold\",\"amount\":200}', '恢复友方25点生命值。'),
('shield', '护盾术', 'spell', 'common', 'none', 1, '{}', '{\"armor\":15,\"duration\":2,\"target\":\"ally\"}', '{}', '{\"currency_type\":\"gold\",\"amount\":200}', '为友方提供15点护甲，持续2回合。'),

-- 稀有法术
('meteor', '陨石术', 'spell', 'rare', 'none', 4, '{}', '{\"damage\":60,\"aoe\":true,\"target\":\"enemy\"}', '{}', '{\"currency_type\":\"gold\",\"amount\":800}', '召唤陨石，对所有敌人造成60点伤害。'),
('mass_heal', '群体治疗', 'spell', 'rare', 'none', 3, '{}', '{\"heal\":30,\"target\":\"all_allies\"}', '{}', '{\"currency_type\":\"gold\",\"amount\":800}', '恢复所有友方30点生命值。'),
('ice_shield', '寒冰护盾', 'spell', 'rare', 'none', 2, '{}', '{\"armor\":25,\"duration\":3,\"freeze_chance\":0.3,\"target\":\"ally\"}', '{}', '{\"currency_type\":\"gold\",\"amount\":800}', '提供25点护甲，并有30%概率冰冻攻击者。'),

-- 史诗法术
('dragon_breath', '龙息术', 'spell', 'epic', 'none', 5, '{}', '{\"damage\":80,\"aoe\":true,\"burn\":true,\"burn_damage\":10,\"burn_duration\":3,\"target\":\"enemy\"}', '{}', '{\"currency_type\":\"gold\",\"amount\":2000}', '释放龙息，对所有敌人造成80点伤害并附加燃烧效果。'),
('resurrection', '复活术', 'spell', 'epic', 'none', 6, '{}', '{\"revive\":true,\"heal\":50,\"target\":\"ally\"}', '{}', '{\"currency_type\":\"gold\",\"amount\":2000}', '复活一名倒下的友方，并恢复50点生命值。'),

-- 传说法术
('apocalypse', '末日审判', 'spell', 'legendary', 'none', 8, '{}', '{\"damage\":150,\"aoe\":true,\"stun\":true,\"stun_duration\":2,\"target\":\"enemy\"}', '{}', '{\"currency_type\":\"gold\",\"amount\":5000}', '释放末日审判，对所有敌人造成150点伤害并眩晕2回合。');

-- 3. 插入装备卡牌（cards - equipment）
-- ============================================
INSERT INTO cards (code, name, card_type, rarity, slot_type, action_point_cost, stat_modifiers, effect_payload, camp_unlock_condition, shop_price, description) VALUES
-- 武器（weapon）
('iron_sword', '铁剑', 'equipment', 'common', 'weapon', 0, '{\"attack\":10}', '{}', '{}', '{\"currency_type\":\"gold\",\"amount\":300}', '基础武器，增加10点攻击力。'),
('steel_sword', '钢剑', 'equipment', 'rare', 'weapon', 0, '{\"attack\":20,\"crit_chance\":0.1}', '{\"crit_damage\":1.5}', '{}', '{\"currency_type\":\"gold\",\"amount\":1000}', '精制武器，增加20点攻击力和10%暴击率。'),
('dragon_blade', '龙刃', 'equipment', 'epic', 'weapon', 0, '{\"attack\":35,\"crit_chance\":0.2}', '{\"crit_damage\":2.0,\"burn_chance\":0.3}', '{}', '{\"currency_type\":\"gold\",\"amount\":2500}', '传说中的武器，增加35点攻击力并有30%概率造成燃烧。'),
('excalibur', '王者之剑', 'equipment', 'legendary', 'weapon', 0, '{\"attack\":50,\"crit_chance\":0.3}', '{\"crit_damage\":2.5,\"heal_on_kill\":20}', '{}', '{\"currency_type\":\"gold\",\"amount\":6000}', '传说中的王者之剑，击杀敌人时恢复生命值。'),

-- 护甲（armor）
('leather_armor', '皮甲', 'equipment', 'common', 'armor', 0, '{\"defense\":15,\"hp\":20}', '{}', '{}', '{\"currency_type\":\"gold\",\"amount\":300}', '基础护甲，增加15点防御和20点生命值。'),
('plate_armor', '板甲', 'equipment', 'rare', 'armor', 0, '{\"defense\":30,\"hp\":40}', '{\"damage_reduction\":0.1}', '{}', '{\"currency_type\":\"gold\",\"amount\":1000}', '重型护甲，增加30点防御和40点生命值，减少10%伤害。'),
('dragon_scale', '龙鳞甲', 'equipment', 'epic', 'armor', 0, '{\"defense\":50,\"hp\":60}', '{\"damage_reduction\":0.2,\"fire_resistance\":0.5}', '{}', '{\"currency_type\":\"gold\",\"amount\":2500}', '龙鳞制成的护甲，拥有强大的防御力和火焰抗性。'),

-- 饰品（trinket）
('health_ring', '生命之戒', 'equipment', 'common', 'trinket', 0, '{\"hp\":30}', '{\"regen\":5}', '{}', '{\"currency_type\":\"gold\",\"amount\":400}', '每回合恢复5点生命值。'),
('power_amulet', '力量护符', 'equipment', 'rare', 'trinket', 0, '{\"attack\":15}', '{\"attack_bonus_per_kill\":2}', '{}', '{\"currency_type\":\"gold\",\"amount\":1200}', '每次击杀敌人增加2点攻击力。'),
('phoenix_feather', '凤凰之羽', 'equipment', 'epic', 'trinket', 0, '{\"hp\":50}', '{\"revive_once\":true,\"revive_hp\":50}', '{}', '{\"currency_type\":\"gold\",\"amount\":3000}', '拥有一次复活机会，复活时恢复50%生命值。');

-- 4. 插入道具（items）
-- ============================================
INSERT INTO items (code, name, item_type, rarity, effect_payload, stack_limit, shop_price, description) VALUES
-- 消耗品（consumable）
('health_potion_small', '小型治疗药水', 'consumable', 'common', '{\"heal\":20}', 99, 50, '恢复20点生命值。'),
('health_potion_medium', '中型治疗药水', 'consumable', 'common', '{\"heal\":50}', 99, 120, '恢复50点生命值。'),
('health_potion_large', '大型治疗药水', 'consumable', 'rare', '{\"heal\":100}', 99, 250, '恢复100点生命值。'),
('stress_relief', '压力缓解剂', 'consumable', 'common', '{\"stress_reduce\":15}', 99, 80, '减少15点压力值。'),
('energy_drink', '能量饮料', 'consumable', 'rare', '{\"action_points\":2,\"duration\":1}', 50, 200, '本回合增加2点行动点。'),
('blessing_scroll', '祝福卷轴', 'consumable', 'epic', '{\"blessing\":true,\"attack_bonus\":20,\"defense_bonus\":20,\"duration\":3}', 20, 500, '获得祝福，3回合内增加20点攻击和防御。'),

-- 材料（material）
('iron_ore', '铁矿', 'material', 'common', '{}', 999, 10, '用于锻造的基础材料。'),
('steel_ingot', '钢锭', 'material', 'rare', '{}', 999, 50, '精炼的金属材料。'),
('dragon_scale_fragment', '龙鳞碎片', 'material', 'epic', '{}', 99, 200, '珍贵的龙鳞碎片，用于制作高级装备。'),

-- 货币包（currency_bundle）
('gold_pack_small', '小金币包', 'currency_bundle', 'common', '{\"gold\":500}', 99, 100, '打开后获得500金币。'),
('gold_pack_medium', '中金币包', 'currency_bundle', 'rare', '{\"gold\":1500}', 99, 300, '打开后获得1500金币。'),
('gold_pack_large', '大金币包', 'currency_bundle', 'epic', '{\"gold\":5000}', 99, 1000, '打开后获得5000金币。');

-- 5. 插入商城商品（shop_offers）
-- ============================================
-- 注意：target_id需要根据实际插入后的ID调整
-- 建议先执行上面的INSERT，然后查询ID，再执行下面的INSERT

-- 查询实际ID的SQL（执行后根据结果调整target_id）：
-- SELECT id, code, name FROM card_characters ORDER BY id;
-- SELECT id, code, name FROM cards WHERE card_type = 'spell' ORDER BY id;
-- SELECT id, code, name FROM cards WHERE card_type = 'equipment' ORDER BY id;
-- SELECT id, code, name FROM items ORDER BY id;

-- 角色商品（card_character）- 假设ID从1开始
INSERT INTO shop_offers (offer_type, target_id, price, display_order, refresh_rule) VALUES
('card_character', 1, 300, 1, '{\"refresh_type\":\"daily\",\"limit_per_user\":10}'),
('card_character', 2, 300, 2, '{\"refresh_type\":\"daily\",\"limit_per_user\":10}'),
('card_character', 3, 300, 3, '{\"refresh_type\":\"daily\",\"limit_per_user\":10}'),
('card_character', 4, 800, 4, '{\"refresh_type\":\"daily\",\"limit_per_user\":5}'),
('card_character', 5, 800, 5, '{\"refresh_type\":\"daily\",\"limit_per_user\":5}'),
('card_character', 6, 800, 6, '{\"refresh_type\":\"daily\",\"limit_per_user\":5}'),
('card_character', 7, 2000, 7, '{\"refresh_type\":\"weekly\",\"limit_per_user\":2}'),
('card_character', 8, 2000, 8, '{\"refresh_type\":\"weekly\",\"limit_per_user\":2}'),
('card_character', 9, 5000, 9, '{\"refresh_type\":\"weekly\",\"limit_per_user\":1}');

-- 法术商品（card - spell）- 假设ID从1开始
INSERT INTO shop_offers (offer_type, target_id, price, display_order, refresh_rule) VALUES
('card', 1, 200, 10, '{\"refresh_type\":\"daily\",\"limit_per_user\":20}'),
('card', 2, 200, 11, '{\"refresh_type\":\"daily\",\"limit_per_user\":20}'),
('card', 3, 200, 12, '{\"refresh_type\":\"daily\",\"limit_per_user\":20}'),
('card', 4, 800, 13, '{\"refresh_type\":\"daily\",\"limit_per_user\":10}'),
('card', 5, 800, 14, '{\"refresh_type\":\"daily\",\"limit_per_user\":10}'),
('card', 6, 800, 15, '{\"refresh_type\":\"daily\",\"limit_per_user\":10}'),
('card', 7, 2000, 16, '{\"refresh_type\":\"weekly\",\"limit_per_user\":3}'),
('card', 8, 2000, 17, '{\"refresh_type\":\"weekly\",\"limit_per_user\":3}'),
('card', 9, 5000, 18, '{\"refresh_type\":\"weekly\",\"limit_per_user\":1}');

-- 装备商品（card - equipment）- 假设ID从10开始
INSERT INTO shop_offers (offer_type, target_id, price, display_order, refresh_rule) VALUES
('card', 10, 300, 19, '{\"refresh_type\":\"daily\",\"limit_per_user\":5}'),
('card', 11, 1000, 20, '{\"refresh_type\":\"daily\",\"limit_per_user\":3}'),
('card', 12, 2500, 21, '{\"refresh_type\":\"weekly\",\"limit_per_user\":2}'),
('card', 13, 6000, 22, '{\"refresh_type\":\"weekly\",\"limit_per_user\":1}'),
('card', 14, 300, 23, '{\"refresh_type\":\"daily\",\"limit_per_user\":5}'),
('card', 15, 1000, 24, '{\"refresh_type\":\"daily\",\"limit_per_user\":3}'),
('card', 16, 2500, 25, '{\"refresh_type\":\"weekly\",\"limit_per_user\":2}'),
('card', 17, 400, 26, '{\"refresh_type\":\"daily\",\"limit_per_user\":5}'),
('card', 18, 1200, 27, '{\"refresh_type\":\"daily\",\"limit_per_user\":3}'),
('card', 19, 3000, 28, '{\"refresh_type\":\"weekly\",\"limit_per_user\":1}');

-- 道具商品（item）- 假设ID从1开始
INSERT INTO shop_offers (offer_type, target_id, price, display_order, refresh_rule) VALUES
('item', 1, 50, 29, '{\"refresh_type\":\"daily\",\"limit_per_user\":99}'),
('item', 2, 120, 30, '{\"refresh_type\":\"daily\",\"limit_per_user\":50}'),
('item', 3, 250, 31, '{\"refresh_type\":\"daily\",\"limit_per_user\":20}'),
('item', 4, 80, 32, '{\"refresh_type\":\"daily\",\"limit_per_user\":99}'),
('item', 5, 200, 33, '{\"refresh_type\":\"daily\",\"limit_per_user\":30}'),
('item', 6, 500, 34, '{\"refresh_type\":\"weekly\",\"limit_per_user\":10}'),
('item', 7, 10, 35, '{\"refresh_type\":\"daily\",\"limit_per_user\":999}'),
('item', 8, 50, 36, '{\"refresh_type\":\"daily\",\"limit_per_user\":200}'),
('item', 9, 200, 37, '{\"refresh_type\":\"weekly\",\"limit_per_user\":50}'),
('item', 10, 100, 38, '{\"refresh_type\":\"daily\",\"limit_per_user\":10}'),
('item', 11, 300, 39, '{\"refresh_type\":\"daily\",\"limit_per_user\":5}'),
('item', 12, 1000, 40, '{\"refresh_type\":\"weekly\",\"limit_per_user\":3}');

