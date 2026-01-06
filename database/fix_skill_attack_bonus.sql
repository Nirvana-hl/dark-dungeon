-- ============================================
-- 技能系统修复SQL脚本
-- 修复所有不适合玩家职业的 attack_bonus 相关效果
-- 创建时间：2026-01-06
-- ============================================

-- 修复技能60：战吼（warrior_battle_cry）
-- 移除 attack_bonus，保留 enemy_attack_reduction，新增 damage_reduction
UPDATE `skills` SET 
  `effect_payload` = '{"type": "buff", "target": "all_allies", "duration": 4, "action_cost": 2, "morale_bonus": 0.1, "enemy_attack_reduction": 0.1, "damage_reduction": 0.1}',
  `description` = '战士发出震撼战场的战吼，为所有友军提供士气加成和伤害减免，同时震慑敌人，降低敌人的攻击力。战吼不仅能够激励队友，还能震慑敌人。这是战士最强大的支援技能，体现了他们的领导力。'
WHERE `code` = 'warrior_battle_cry';

-- 修复技能64：狂暴（warrior_berserker_rage）
-- 移除 attack_bonus 和 attack_speed_bonus，新增 damage_bonus 和 action_points_bonus
UPDATE `skills` SET 
  `effect_payload` = '{"type": "buff", "target": "self", "duration": 4, "action_cost": 3, "damage_bonus": 0.3, "defense_penalty": 0.2, "action_points_bonus": 1}',
  `description` = '战士进入狂暴状态，在接下来的4回合内大幅提升技能伤害和行动点恢复速度，但防御力会降低。狂暴状态下，战士会不顾一切地使用技能，直到战斗结束。这是战士最强大的输出技能。'
WHERE `code` = 'warrior_berserker_rage';

-- 修复技能67：神圣力量（warden_sacred_power）
-- 移除 attack_bonus，新增 max_mana_bonus 和 mana_regen_per_turn
UPDATE `skills` SET 
  `effect_payload` = '{"type": "passive", "holy_damage_bonus": 0.1, "max_mana_bonus": 0.15, "mana_regen_per_turn": 1}',
  `description` = '守望者通过神圣力量的加持，永久提升自己的法力值上限和每回合法力恢复速度。神圣力量让守望者的技能带有神圣属性，对邪恶敌人造成额外伤害。'
WHERE `code` = 'warden_sacred_power';

-- 修复技能71：暗影之力（occultist_shadow_power）
-- 移除 attack_bonus，新增 max_mana_bonus 和 mana_regen_per_turn
UPDATE `skills` SET 
  `effect_payload` = '{"type": "passive", "magic_penetration": 0.15, "max_mana_bonus": 0.2, "mana_regen_per_turn": 1}',
  `description` = '秘术师通过掌握暗影力量，永久提升自己的法力值上限和每回合法力恢复速度。暗影之力让秘术师的魔法攻击能够穿透敌人的魔法抗性。'
WHERE `code` = 'occultist_shadow_power';

-- 修复技能75：精准之力（ranger_precision_power）
-- 移除 attack_bonus，新增 max_mana_bonus 和 mana_regen_per_turn
UPDATE `skills` SET 
  `effect_payload` = '{"type": "passive", "crit_chance_bonus": 0.1, "max_mana_bonus": 0.18, "mana_regen_per_turn": 1}',
  `description` = '游侠通过长期的训练，永久提升自己的暴击率和法力值上限。精准之力让游侠的每一箭都更加致命，能够精准命中敌人的弱点。'
WHERE `code` = 'ranger_precision_power';

-- 修复技能79：狂暴之力（warrior_berserker_power）
-- 移除 attack_bonus，新增 max_mana_bonus 和 mana_regen_per_turn
UPDATE `skills` SET 
  `effect_payload` = '{"type": "passive", "armor_penetration": 0.2, "max_mana_bonus": 0.25, "mana_regen_per_turn": 1}',
  `description` = '战士通过激发内在的狂暴力量，永久提升自己的法力值上限和每回合法力恢复速度。狂暴之力让战士的技能能够穿透敌人的防御，造成更高伤害。'
WHERE `code` = 'warrior_berserker_power';

-- 修复技能82：钢铁意志（warrior_iron_will）
-- 移除 attack_bonus，新增 max_mana_bonus 和 mana_regen_per_turn
UPDATE `skills` SET 
  `effect_payload` = '{"type": "passive", "hp_bonus": 0.3, "max_mana_bonus": 0.15, "mana_regen_per_turn": 1}',
  `description` = '战士通过钢铁般的意志，永久提升自己的生命值上限和法力值上限。钢铁意志让战士在战斗中更加坚韧，能够承受更多伤害并更频繁地使用技能。'
WHERE `code` = 'warrior_iron_will';

-- 修复技能58：猎手本能（ranger_hunter_instinct）
-- 移除 attack_speed_bonus，新增 action_points_bonus
UPDATE `skills` SET 
  `effect_payload` = '{"type": "buff", "target": "self", "duration": 4, "action_cost": 3, "crit_chance_bonus": 0.3, "movement_speed_bonus": 0.4, "action_points_bonus": 1}',
  `description` = '游侠激活猎手本能，在接下来的4回合内大幅提高暴击率、移动速度和行动点恢复速度。这个技能让游侠进入最佳战斗状态，如同真正的猎手般敏锐和致命。'
WHERE `code` = 'ranger_hunter_instinct';

-- 修复技能78：猎手体魄（ranger_hunter_physique）
-- 移除 attack_speed_bonus，新增 max_mana_bonus 和 mana_regen_per_turn
UPDATE `skills` SET 
  `effect_payload` = '{"type": "passive", "hp_bonus": 0.2, "max_mana_bonus": 0.15, "mana_regen_per_turn": 1}',
  `description` = '游侠通过长期的野外生存训练，永久提升自己的生命值上限和法力值上限。猎手体魄让游侠在战斗中更加灵活，能够更频繁地使用技能。'
WHERE `code` = 'ranger_hunter_physique';

-- ============================================
-- 验证修复结果
-- ============================================
-- 查询所有仍有 attack_bonus 的技能（应该返回空结果）
SELECT `id`, `code`, `name`, `effect_payload` 
FROM `skills` 
WHERE `effect_payload` LIKE '%attack_bonus%' 
  AND `player_character_code` IN ('warden', 'occultist', 'ranger', 'warrior');

-- 查询所有仍有 attack_speed_bonus 的技能（应该返回空结果）
SELECT `id`, `code`, `name`, `effect_payload` 
FROM `skills` 
WHERE `effect_payload` LIKE '%attack_speed_bonus%' 
  AND `player_character_code` IN ('warden', 'occultist', 'ranger', 'warrior');

