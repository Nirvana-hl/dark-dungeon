-- 为各个职业添加更多主动和被动技能
-- 包括攻击力、法力值提升的被动技能

-- ============================================
-- 守望者 (warden) 新增技能
-- ============================================

-- 守望者 - 神圣力量（被动：提升攻击力）
INSERT INTO `skills` VALUES (67, 'warden_sacred_power', 'warden', '神圣力量', '守望者通过神圣力量的加持，永久提升自己的攻击力。神圣力量让守望者的每一次攻击都带有神圣属性，对邪恶敌人造成额外伤害。', '{\"type\": \"passive\", \"attack_bonus\": 0.15, \"holy_damage_bonus\": 0.1}', 3, '{\"row\": 2, \"column\": 4}', '[\"warden_sacred_shield\"]');

-- 守望者 - 法力源泉（被动：提升法力值上限）
INSERT INTO `skills` VALUES (68, 'warden_mana_spring', 'warden', '法力源泉', '守望者与神圣能量建立深层连接，永久提升自己的法力值上限。法力源泉让守望者能够更频繁地使用技能，在战斗中持续提供支援。', '{\"type\": \"passive\", \"max_mana_bonus\": 0.2}', 3, '{\"row\": 2, \"column\": 5}', '[\"warden_healing_light\"]');

-- 守望者 - 神圣审判（主动：攻击技能）
INSERT INTO `skills` VALUES (69, 'warden_divine_judgment', 'warden', '神圣审判', '守望者召唤神圣审判之光，对单个敌人造成大量神圣伤害。对邪恶和亡灵类敌人造成双倍伤害。神圣审判是守望者最强大的攻击技能，体现了正义的力量。', '{\"type\": \"attack\", \"damage\": 40, \"target\": \"enemy\", \"action_cost\": 3, \"damage_type\": \"holy\", \"evil_damage_multiplier\": 2.0}', 5, '{\"row\": 3, \"column\": 3}', '[\"warden_sacred_purification\"]');

-- 守望者 - 守护者意志（被动：提升生命值和防御）
INSERT INTO `skills` VALUES (70, 'warden_guardian_will', 'warden', '守护者意志', '守望者通过坚定的守护意志，永久提升自己的生命值上限和物理防御力。守护者意志让守望者能够承受更多伤害，更好地保护队友。', '{\"type\": \"passive\", \"hp_bonus\": 0.25, \"physical_defense_bonus\": 0.3}', 5, '{\"row\": 3, \"column\": 4}', '[\"warden_absolute_defense\"]');

-- ============================================
-- 秘术师 (occultist) 新增技能
-- ============================================

-- 秘术师 - 暗影之力（被动：提升攻击力）
INSERT INTO `skills` VALUES (71, 'occultist_shadow_power', 'occultist', '暗影之力', '秘术师通过掌握暗影力量，永久提升自己的魔法攻击力。暗影之力让秘术师的魔法攻击更加致命，能够穿透敌人的魔法抗性。', '{\"type\": \"passive\", \"attack_bonus\": 0.2, \"magic_penetration\": 0.15}', 3, '{\"row\": 2, \"column\": 4}', '[\"occultist_shadow_bolt\"]');

-- 秘术师 - 暗影源泉（被动：提升法力值上限）
INSERT INTO `skills` VALUES (72, 'occultist_shadow_well', 'occultist', '暗影源泉', '秘术师与暗影维度建立连接，永久提升自己的法力值上限。暗影源泉让秘术师能够更频繁地施放强大的暗影魔法，持续输出伤害。', '{\"type\": \"passive\", \"max_mana_bonus\": 0.25}', 3, '{\"row\": 2, \"column\": 5}', '[\"occultist_weakness_curse\"]');

-- 秘术师 - 暗影风暴（主动：群体攻击）
INSERT INTO `skills` VALUES (73, 'occultist_shadow_storm', 'occultist', '暗影风暴', '秘术师召唤暗影风暴，对所有敌人造成持续3回合的暗影伤害。暗影风暴会降低敌人的魔法抗性，让后续的魔法攻击更加有效。', '{\"type\": \"attack\", \"damage\": 18, \"target\": \"all_enemies\", \"action_cost\": 4, \"damage_type\": \"shadow\", \"dot_damage\": 8, \"dot_duration\": 3, \"magic_resistance_reduction\": 0.2}', 5, '{\"row\": 3, \"column\": 3}', '[\"occultist_shadow_burst\"]');

-- 秘术师 - 暗影体质（被动：提升生命值和法力恢复）
INSERT INTO `skills` VALUES (74, 'occultist_shadow_constitution', 'occultist', '暗影体质', '秘术师通过暗影能量的改造，永久提升自己的生命值上限，并在战斗中持续恢复法力值。暗影体质让脆弱的秘术师有了更强的生存能力。', '{\"type\": \"passive\", \"hp_bonus\": 0.15, \"mana_regen_per_turn\": 5}', 5, '{\"row\": 3, \"column\": 4}', '[\"occultist_dark_pact\"]');

-- ============================================
-- 游侠 (ranger) 新增技能
-- ============================================

-- 游侠 - 精准之力（被动：提升攻击力）
INSERT INTO `skills` VALUES (75, 'ranger_precision_power', 'ranger', '精准之力', '游侠通过长期的训练，永久提升自己的攻击力和暴击率。精准之力让游侠的每一箭都更加致命，能够精准命中敌人的弱点。', '{\"type\": \"passive\", \"attack_bonus\": 0.18, \"crit_chance_bonus\": 0.1}', 3, '{\"row\": 2, \"column\": 4}', '[\"ranger_precise_shot\"]');

-- 游侠 - 自然源泉（被动：提升法力值上限）
INSERT INTO `skills` VALUES (76, 'ranger_nature_well', 'ranger', '自然源泉', '游侠与自然力量建立连接，永久提升自己的法力值上限。自然源泉让游侠能够更频繁地使用追踪和陷阱技能，更好地控制战场。', '{\"type\": \"passive\", \"max_mana_bonus\": 0.2}', 3, '{\"row\": 2, \"column\": 5}', '[\"ranger_tracking_mark\"]');

-- 游侠 - 箭雨风暴（主动：群体攻击）
INSERT INTO `skills` VALUES (77, 'ranger_arrow_storm', 'ranger', '箭雨风暴', '游侠向天空射出大量箭矢，形成箭雨覆盖整个战场，对所有敌人造成物理伤害。箭雨风暴是游侠最强大的群体攻击技能，箭矢如暴雨般倾泻而下。', '{\"type\": \"attack\", \"damage\": 25, \"target\": \"all_enemies\", \"action_cost\": 4, \"damage_type\": \"physical\", \"hits\": 3}', 5, '{\"row\": 3, \"column\": 3}', '[\"ranger_multi_shot\"]');

-- 游侠 - 猎手体魄（被动：提升生命值和攻击速度）
INSERT INTO `skills` VALUES (78, 'ranger_hunter_physique', 'ranger', '猎手体魄', '游侠通过长期的野外生存训练，永久提升自己的生命值上限和攻击速度。猎手体魄让游侠在战斗中更加灵活，能够快速输出伤害。', '{\"type\": \"passive\", \"hp_bonus\": 0.2, \"attack_speed_bonus\": 0.15}', 5, '{\"row\": 3, \"column\": 4}', '[\"ranger_hunter_instinct\"]');

-- ============================================
-- 战士 (warrior) 新增技能
-- ============================================

-- 战士 - 狂暴之力（被动：提升攻击力）
INSERT INTO `skills` VALUES (79, 'warrior_berserker_power', 'warrior', '狂暴之力', '战士通过激发内在的狂暴力量，永久提升自己的攻击力。狂暴之力让战士的每一次攻击都更加凶猛，能够轻易撕裂敌人的防御。', '{\"type\": \"passive\", \"attack_bonus\": 0.25, \"armor_penetration\": 0.2}', 3, '{\"row\": 2, \"column\": 4}', '[\"warrior_heavy_strike\"]');

-- 战士 - 战斗意志（被动：提升法力值上限）
INSERT INTO `skills` VALUES (80, 'warrior_battle_will', 'warrior', '战斗意志', '战士通过坚定的战斗意志，永久提升自己的法力值上限。战斗意志让战士能够更频繁地使用战吼和嘲讽等技能，更好地保护队友。', '{\"type\": \"passive\", \"max_mana_bonus\": 0.15}', 3, '{\"row\": 2, \"column\": 5}', '[\"warrior_battle_cry\"]');

-- 战士 - 大地震击（主动：群体攻击）
INSERT INTO `skills` VALUES (81, 'warrior_earthquake', 'warrior', '大地震击', '战士用尽全力重击地面，引发地震冲击波，对所有敌人造成物理伤害并造成短暂眩晕。大地震击是战士最强大的群体控制技能，能够瞬间改变战场局势。', '{\"type\": \"attack\", \"damage\": 30, \"target\": \"all_enemies\", \"action_cost\": 4, \"damage_type\": \"physical\", \"stun_duration\": 1}', 5, '{\"row\": 3, \"column\": 3}', '[\"warrior_whirlwind\"]');

-- 战士 - 钢铁意志（被动：提升生命值和攻击力）
INSERT INTO `skills` VALUES (82, 'warrior_iron_will', 'warrior', '钢铁意志', '战士通过钢铁般的意志，永久提升自己的生命值上限和攻击力。钢铁意志让战士在战斗中更加坚韧，能够承受更多伤害并输出更高伤害。', '{\"type\": \"passive\", \"hp_bonus\": 0.3, \"attack_bonus\": 0.15}', 5, '{\"row\": 3, \"column\": 4}', '[\"warrior_ultimate_strike\"]');

