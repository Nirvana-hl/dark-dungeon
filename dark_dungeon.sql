/*
 Navicat Premium Data Transfer

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : dark_dungeon

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 26/11/2025 16:00:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for achievements
-- ----------------------------
DROP TABLE IF EXISTS `achievements`;
CREATE TABLE `achievements`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `requirements` json NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_achievements_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of achievements
-- ----------------------------
INSERT INTO `achievements` VALUES (1, '初出茅庐', 'progression', '完成第1关，开始你的地牢冒险之旅', '{\"type\": \"stage_complete\", \"stage_number\": 1}');
INSERT INTO `achievements` VALUES (2, '勇者之路', 'progression', '击败第1章的Boss，证明你的实力', '{\"type\": \"stage_complete\", \"is_boss\": true, \"stage_number\": 5}');
INSERT INTO `achievements` VALUES (3, '章节征服者', 'progression', '完成第1章的所有关卡', '{\"type\": \"chapter_complete\", \"chapter_number\": 1}');
INSERT INTO `achievements` VALUES (4, '地牢探索者', 'progression', '完成10个不同的关卡', '{\"type\": \"stages_completed\", \"count\": 10}');
INSERT INTO `achievements` VALUES (5, '传奇冒险家', 'progression', '完成50个关卡，成为真正的传奇', '{\"type\": \"stages_completed\", \"count\": 50}');
INSERT INTO `achievements` VALUES (6, '完美通关', 'progression', '以胜利完成10次地牢探索', '{\"type\": \"runs_completed\", \"count\": 10, \"result\": \"victory\"}');
INSERT INTO `achievements` VALUES (7, '战斗大师', 'mastery', '在一场战斗中击败10个敌人', '{\"type\": \"enemies_defeated\", \"count\": 10, \"in_single_battle\": true}');
INSERT INTO `achievements` VALUES (8, '无伤通关', 'mastery', '完成一个关卡且角色未受到任何伤害', '{\"type\": \"stage_complete_no_damage\", \"stage_number\": null}');
INSERT INTO `achievements` VALUES (9, '压力掌控者', 'mastery', '在压力值达到100时完成关卡', '{\"type\": \"stage_complete_high_stress\", \"stress_level\": 100}');
INSERT INTO `achievements` VALUES (10, '技能专家', 'mastery', '解锁10个不同的技能', '{\"type\": \"skills_unlocked\", \"count\": 10}');
INSERT INTO `achievements` VALUES (11, '连击高手', 'mastery', '在一场战斗中连续使用5次技能', '{\"type\": \"skill_combo\", \"count\": 5, \"in_single_battle\": true}');
INSERT INTO `achievements` VALUES (12, '完美防御', 'mastery', '在一场战斗中完全防御所有攻击', '{\"type\": \"perfect_defense\", \"in_single_battle\": true}');
INSERT INTO `achievements` VALUES (13, '速战速决', 'mastery', '在3回合内完成一场战斗', '{\"type\": \"battle_complete_fast\", \"max_rounds\": 3}');
INSERT INTO `achievements` VALUES (14, '卡牌收藏家', 'collection', '收集10张不同的卡牌', '{\"type\": \"cards_collected\", \"unique_count\": 10}');
INSERT INTO `achievements` VALUES (15, '角色收集者', 'collection', '收集5个不同的角色卡', '{\"type\": \"card_characters_collected\", \"unique_count\": 5}');
INSERT INTO `achievements` VALUES (16, '史诗收藏家', 'collection', '收集5张史诗品质的卡牌', '{\"type\": \"cards_collected\", \"count\": 5, \"rarity\": \"epic\"}');
INSERT INTO `achievements` VALUES (17, '传说收藏家', 'collection', '收集3张传说品质的卡牌', '{\"type\": \"cards_collected\", \"count\": 3, \"rarity\": \"legendary\"}');
INSERT INTO `achievements` VALUES (18, '道具大师', 'collection', '收集20种不同的道具', '{\"type\": \"items_collected\", \"unique_count\": 20}');
INSERT INTO `achievements` VALUES (19, '全职业精通', 'collection', '收集所有职业的角色卡', '{\"type\": \"all_classes_collected\", \"classes\": [\"warrior\", \"occultist\", \"ranger\", \"priest\"]}');
INSERT INTO `achievements` VALUES (20, '星级提升', 'collection', '将任意角色卡提升到5星', '{\"type\": \"card_character_star_level\", \"star_level\": 5}');
INSERT INTO `achievements` VALUES (21, '活跃玩家', 'social', '连续7天登录游戏', '{\"days\": 7, \"type\": \"consecutive_login\"}');
INSERT INTO `achievements` VALUES (22, '持久冒险者', 'social', '累计登录30天', '{\"days\": 30, \"type\": \"total_login_days\"}');
INSERT INTO `achievements` VALUES (23, '财富积累', 'social', '累计获得10000金币', '{\"type\": \"total_gold_earned\", \"amount\": 10000}');
INSERT INTO `achievements` VALUES (24, '消费达人', 'social', '在商城中累计消费5000金币', '{\"type\": \"total_gold_spent\", \"amount\": 5000}');
INSERT INTO `achievements` VALUES (25, '探索记录', 'social', '累计进行50次地牢探索', '{\"type\": \"total_runs\", \"count\": 50}');
INSERT INTO `achievements` VALUES (26, '时间见证者', 'social', '游戏时长达到100小时', '{\"type\": \"total_playtime_hours\", \"hours\": 100}');
INSERT INTO `achievements` VALUES (27, '压力测试', 'mastery', '在压力值达到最高时完成Boss战', '{\"type\": \"boss_defeated_high_stress\", \"is_boss\": true, \"stress_level\": 100}');
INSERT INTO `achievements` VALUES (28, '完美探索', 'mastery', '完成一次探索且未使用任何道具', '{\"type\": \"run_complete_no_items\", \"items_used\": 0}');
INSERT INTO `achievements` VALUES (29, '团队协作', 'mastery', '在一场战斗中同时上阵4个角色', '{\"type\": \"characters_deployed\", \"count\": 4, \"in_single_battle\": true}');
INSERT INTO `achievements` VALUES (30, '资源管理', 'progression', '在单次探索中获得1000金币', '{\"type\": \"gold_earned_single_run\", \"amount\": 1000}');
INSERT INTO `achievements` VALUES (31, '事件专家', 'collection', '触发10个不同的随机事件', '{\"type\": \"events_triggered\", \"unique_count\": 10}');

-- ----------------------------
-- Table structure for card_character_traits
-- ----------------------------
DROP TABLE IF EXISTS `card_character_traits`;
CREATE TABLE `card_character_traits`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `card_character_id` bigint NOT NULL,
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `effect_payload` json NOT NULL,
  `scaling_payload` json NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_traits_card_character`(`card_character_id` ASC) USING BTREE,
  CONSTRAINT `fk_traits_card_character` FOREIGN KEY (`card_character_id`) REFERENCES `card_characters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of card_character_traits
-- ----------------------------
INSERT INTO `card_character_traits` VALUES (1, 1, '星辉祝福', 'positive', '{\"heal_allies\": 2}', '{\"2\": {\"heal_allies\": 3}, \"3\": {\"heal_allies\": 4}, \"4\": {\"heal_allies\": 5}}', '提升全队治疗量');
INSERT INTO `card_character_traits` VALUES (2, 2, '钢铁护盾', 'positive', '{\"armor_bonus\": 3}', '{\"2\": {\"armor_bonus\": 5}, \"3\": {\"armor_bonus\": 8}}', '提供额外护甲');

-- ----------------------------
-- Table structure for card_characters
-- ----------------------------
DROP TABLE IF EXISTS `card_characters`;
CREATE TABLE `card_characters`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `class` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `faction` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rarity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `base_hp` int NOT NULL,
  `base_attack` int NOT NULL,
  `action_point_cost` int NOT NULL,
  `base_star_level` tinyint NOT NULL DEFAULT 1,
  `max_star_level` tinyint NOT NULL DEFAULT 5,
  `star_upgrade_payload` json NOT NULL,
  `traits` json NOT NULL,
  `shop_price` int NOT NULL DEFAULT 0,
  `lore` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_card_characters_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of card_characters
-- ----------------------------
INSERT INTO `card_characters` VALUES (1, 'priestess', '星辉祭司', 'support', 'human', 'rare', 40, 8, 2, 1, 5, '{\"stats\": {\"hp\": 10, \"attack\": 2}, \"traits\": {\"heal_allies\": {\"2\": 1, \"3\": 2}}}', '[{\"name\": \"全体治疗\", \"value\": 2}]', 280, '神圣的治愈者，能够恢复队友的生命');
INSERT INTO `card_characters` VALUES (2, 'shield_guard', '重甲盾卫', 'protector', 'human', 'common', 55, 6, 1, 1, 5, '{\"stats\": {\"hp\": 15, \"armor\": 3}}', '[{\"name\": \"护盾\", \"value\": 3}]', 160, '坚不可摧的防御者');
INSERT INTO `card_characters` VALUES (3, 'fire_mage', '火焰法师', 'mage', 'human', 'epic', 35, 25, 3, 1, 5, '{\"stats\": {\"hp\": 8, \"attack\": 5}, \"traits\": {\"fire_damage\": {\"2\": 5, \"3\": 10}}}', '[{\"name\": \"火焰冲击\", \"value\": 25}]', 500, '掌控火焰的毁灭法师');
INSERT INTO `card_characters` VALUES (4, 'shadow_assassin', '暗影刺客', 'assassin', 'occult', 'epic', 30, 30, 2, 1, 5, '{\"stats\": {\"hp\": 5, \"attack\": 8}, \"traits\": {\"critical_chance\": {\"2\": 0.1, \"3\": 0.2}}}', '[{\"name\": \"致命一击\", \"value\": 30}]', 600, '暗影中的致命杀手');
INSERT INTO `card_characters` VALUES (6, 'warrior_common', '新兵战士', 'warrior', 'human', 'common', 80, 15, 2, 1, 5, '{\"stats\": {\"hp\": 10, \"attack\": 3}}', '[]', 300, '一名训练有素的新兵，擅长近战。');
INSERT INTO `card_characters` VALUES (7, 'ranger_common', '见习游侠', 'ranger', 'human', 'common', 70, 18, 2, 1, 5, '{\"stats\": {\"hp\": 8, \"attack\": 4}}', '[]', 300, '年轻的游侠，擅长远程攻击。');
INSERT INTO `card_characters` VALUES (8, 'priest_common', '初级祭司', 'priest', 'human', 'common', 60, 10, 2, 1, 5, '{\"stats\": {\"hp\": 6, \"attack\": 2}}', '[]', 300, '初出茅庐的祭司，能够治疗队友。');
INSERT INTO `card_characters` VALUES (9, 'warrior_rare', '精英战士', 'warrior', 'human', 'rare', 100, 20, 2, 2, 5, '{\"stats\": {\"hp\": 12, \"attack\": 4}, \"traits\": {\"shield_bash\": {\"2\": 1, \"3\": 2}}}', '[{\"name\": \"盾击\", \"type\": \"positive\", \"effect_payload\": {\"stun_chance\": 0.2}}]', 800, '经验丰富的战士，能够使用盾击。');
INSERT INTO `card_characters` VALUES (10, 'occultist_rare', '神秘学者', 'occultist', 'occult', 'rare', 65, 25, 3, 2, 5, '{\"stats\": {\"hp\": 7, \"attack\": 5}, \"traits\": {\"dark_bolt\": {\"2\": 2, \"3\": 3}}}', '[{\"name\": \"暗影箭\", \"type\": \"positive\", \"effect_payload\": {\"damage_multiplier\": 1.2}}]', 800, '掌握黑暗魔法的学者。');
INSERT INTO `card_characters` VALUES (11, 'ranger_rare', '资深游侠', 'ranger', 'human', 'rare', 85, 22, 2, 2, 5, '{\"stats\": {\"hp\": 9, \"attack\": 5}, \"traits\": {\"multi_shot\": {\"2\": 1, \"3\": 2}}}', '[{\"name\": \"多重射击\", \"type\": \"positive\", \"effect_payload\": {\"targets\": 2}}]', 800, '技艺精湛的游侠，能够同时攻击多个目标。');
INSERT INTO `card_characters` VALUES (12, 'warrior_epic', '传奇战士', 'warrior', 'human', 'epic', 120, 25, 2, 3, 5, '{\"stats\": {\"hp\": 15, \"attack\": 6}, \"traits\": {\"berserker_rage\": {\"2\": 1, \"3\": 2, \"4\": 3}}}', '[{\"name\": \"狂战士之怒\", \"type\": \"positive\", \"effect_payload\": {\"duration\": 3, \"attack_bonus\": 10}}]', 2000, '传说中的战士，拥有强大的战斗意志。');
INSERT INTO `card_characters` VALUES (13, 'priest_epic', '大祭司', 'priest', 'divine', 'epic', 90, 15, 2, 3, 5, '{\"stats\": {\"hp\": 10, \"attack\": 3}, \"traits\": {\"divine_heal\": {\"2\": 2, \"3\": 3, \"4\": 4}}}', '[{\"name\": \"神圣治疗\", \"type\": \"positive\", \"effect_payload\": {\"heal_all\": 30}}]', 2000, '受到神祇祝福的大祭司，能够治愈所有队友。');
INSERT INTO `card_characters` VALUES (14, 'dragon_knight', '龙骑士', 'warrior', 'divine', 'legendary', 150, 30, 3, 3, 5, '{\"stats\": {\"hp\": 20, \"attack\": 8}, \"traits\": {\"dragon_breath\": {\"2\": 1, \"3\": 2, \"4\": 3, \"5\": 4}}}', '[{\"name\": \"龙息\", \"type\": \"positive\", \"effect_payload\": {\"aoe_damage\": 50, \"burn_chance\": 0.5}}]', 5000, '传说中的龙骑士，能够驾驭巨龙的力量。');

-- ----------------------------
-- Table structure for cards
-- ----------------------------
DROP TABLE IF EXISTS `cards`;
CREATE TABLE `cards`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `card_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rarity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `slot_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `action_point_cost` int NOT NULL,
  `stat_modifiers` json NOT NULL,
  `effect_payload` json NOT NULL,
  `camp_unlock_condition` json NOT NULL,
  `shop_price` json NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_cards_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cards
-- ----------------------------
INSERT INTO `cards` VALUES (1, 'holy_light', '圣光术', 'spell', 'rare', 'none', 1, '{}', '{\"type\": \"heal_single\", \"heal_amount\": 12}', '{}', '{\"amount\": 120, \"currency_type\": \"gold\"}', '对友军单体治疗12点生命值');
INSERT INTO `cards` VALUES (2, 'fireball', '火球术', 'spell', 'common', 'none', 2, '{}', '{\"type\": \"damage_single\", \"damage\": 30}', '{}', '{\"amount\": 80, \"currency_type\": \"gold\"}', '对单个敌人造成30点火焰伤害');
INSERT INTO `cards` VALUES (3, 'iron_wall', '铁壁护甲', 'equipment', 'common', 'armor', 0, '{\"armor\": 6}', '{\"type\": \"buff_armor\"}', '{}', '{\"amount\": 90, \"currency_type\": \"gold\"}', '强化持有者的护甲，提供6点护甲值');
INSERT INTO `cards` VALUES (4, 'blood_sword', '血刃', 'equipment', 'epic', 'weapon', 0, '{\"attack\": 15, \"lifesteal\": 0.1}', '{\"type\": \"buff_attack\", \"lifesteal\": true}', '{}', '{\"amount\": 300, \"currency_type\": \"gold\"}', '强大的武器，攻击力+15，攻击时恢复10%伤害的生命值');
INSERT INTO `cards` VALUES (33, 'heal', '治疗术', 'spell', 'common', 'none', 2, '{}', '{\"heal\": 25, \"target\": \"ally\"}', '{}', '{\"amount\": 200, \"currency_type\": \"gold\"}', '恢复友方25点生命值。');
INSERT INTO `cards` VALUES (34, 'shield', '护盾术', 'spell', 'common', 'none', 1, '{}', '{\"armor\": 15, \"target\": \"ally\", \"duration\": 2}', '{}', '{\"amount\": 200, \"currency_type\": \"gold\"}', '为友方提供15点护甲，持续2回合。');
INSERT INTO `cards` VALUES (35, 'meteor', '陨石术', 'spell', 'rare', 'none', 4, '{}', '{\"aoe\": true, \"damage\": 60, \"target\": \"enemy\"}', '{}', '{\"amount\": 800, \"currency_type\": \"gold\"}', '召唤陨石，对所有敌人造成60点伤害。');
INSERT INTO `cards` VALUES (36, 'mass_heal', '群体治疗', 'spell', 'rare', 'none', 3, '{}', '{\"heal\": 30, \"target\": \"all_allies\"}', '{}', '{\"amount\": 800, \"currency_type\": \"gold\"}', '恢复所有友方30点生命值。');
INSERT INTO `cards` VALUES (37, 'ice_shield', '寒冰护盾', 'spell', 'rare', 'none', 2, '{}', '{\"armor\": 25, \"target\": \"ally\", \"duration\": 3, \"freeze_chance\": 0.3}', '{}', '{\"amount\": 800, \"currency_type\": \"gold\"}', '提供25点护甲，并有30%概率冰冻攻击者。');
INSERT INTO `cards` VALUES (38, 'dragon_breath', '龙息术', 'spell', 'epic', 'none', 5, '{}', '{\"aoe\": true, \"burn\": true, \"damage\": 80, \"target\": \"enemy\", \"burn_damage\": 10, \"burn_duration\": 3}', '{}', '{\"amount\": 2000, \"currency_type\": \"gold\"}', '释放龙息，对所有敌人造成80点伤害并附加燃烧效果。');
INSERT INTO `cards` VALUES (39, 'resurrection', '复活术', 'spell', 'epic', 'none', 6, '{}', '{\"heal\": 50, \"revive\": true, \"target\": \"ally\"}', '{}', '{\"amount\": 2000, \"currency_type\": \"gold\"}', '复活一名倒下的友方，并恢复50点生命值。');
INSERT INTO `cards` VALUES (40, 'apocalypse', '末日审判', 'spell', 'legendary', 'none', 8, '{}', '{\"aoe\": true, \"stun\": true, \"damage\": 150, \"target\": \"enemy\", \"stun_duration\": 2}', '{}', '{\"amount\": 5000, \"currency_type\": \"gold\"}', '释放末日审判，对所有敌人造成150点伤害并眩晕2回合。');
INSERT INTO `cards` VALUES (41, 'iron_sword', '铁剑', 'equipment', 'common', 'weapon', 0, '{\"attack\": 10}', '{}', '{}', '{\"amount\": 300, \"currency_type\": \"gold\"}', '基础武器，增加10点攻击力。');
INSERT INTO `cards` VALUES (42, 'steel_sword', '钢剑', 'equipment', 'rare', 'weapon', 0, '{\"attack\": 20, \"crit_chance\": 0.1}', '{\"crit_damage\": 1.5}', '{}', '{\"amount\": 1000, \"currency_type\": \"gold\"}', '精制武器，增加20点攻击力和10%暴击率。');
INSERT INTO `cards` VALUES (43, 'dragon_blade', '龙刃', 'equipment', 'epic', 'weapon', 0, '{\"attack\": 35, \"crit_chance\": 0.2}', '{\"burn_chance\": 0.3, \"crit_damage\": 2.0}', '{}', '{\"amount\": 2500, \"currency_type\": \"gold\"}', '传说中的武器，增加35点攻击力并有30%概率造成燃烧。');
INSERT INTO `cards` VALUES (44, 'excalibur', '王者之剑', 'equipment', 'legendary', 'weapon', 0, '{\"attack\": 50, \"crit_chance\": 0.3}', '{\"crit_damage\": 2.5, \"heal_on_kill\": 20}', '{}', '{\"amount\": 6000, \"currency_type\": \"gold\"}', '传说中的王者之剑，击杀敌人时恢复生命值。');
INSERT INTO `cards` VALUES (45, 'leather_armor', '皮甲', 'equipment', 'common', 'armor', 0, '{\"hp\": 20, \"defense\": 15}', '{}', '{}', '{\"amount\": 300, \"currency_type\": \"gold\"}', '基础护甲，增加15点防御和20点生命值。');
INSERT INTO `cards` VALUES (46, 'plate_armor', '板甲', 'equipment', 'rare', 'armor', 0, '{\"hp\": 40, \"defense\": 30}', '{\"damage_reduction\": 0.1}', '{}', '{\"amount\": 1000, \"currency_type\": \"gold\"}', '重型护甲，增加30点防御和40点生命值，减少10%伤害。');
INSERT INTO `cards` VALUES (47, 'dragon_scale', '龙鳞甲', 'equipment', 'epic', 'armor', 0, '{\"hp\": 60, \"defense\": 50}', '{\"fire_resistance\": 0.5, \"damage_reduction\": 0.2}', '{}', '{\"amount\": 2500, \"currency_type\": \"gold\"}', '龙鳞制成的护甲，拥有强大的防御力和火焰抗性。');
INSERT INTO `cards` VALUES (48, 'health_ring', '生命之戒', 'equipment', 'common', 'trinket', 0, '{\"hp\": 30}', '{\"regen\": 5}', '{}', '{\"amount\": 400, \"currency_type\": \"gold\"}', '每回合恢复5点生命值。');
INSERT INTO `cards` VALUES (49, 'power_amulet', '力量护符', 'equipment', 'rare', 'trinket', 0, '{\"attack\": 15}', '{\"attack_bonus_per_kill\": 2}', '{}', '{\"amount\": 1200, \"currency_type\": \"gold\"}', '每次击杀敌人增加2点攻击力。');
INSERT INTO `cards` VALUES (50, 'phoenix_feather', '凤凰之羽', 'equipment', 'epic', 'trinket', 0, '{\"hp\": 50}', '{\"revive_hp\": 50, \"revive_once\": true}', '{}', '{\"amount\": 3000, \"currency_type\": \"gold\"}', '拥有一次复活机会，复活时恢复50%生命值。');

-- ----------------------------
-- Table structure for dungeons
-- ----------------------------
DROP TABLE IF EXISTS `dungeons`;
CREATE TABLE `dungeons`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `recommended_cards` json NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `theme` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地牢主题（如：森林、洞穴、废墟等）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dungeons
-- ----------------------------
INSERT INTO `dungeons` VALUES (1, '灰烬回廊', 'normal', '[\"holy_light\", \"iron_wall\"]', '穿越灰烬骑士防线，小心他们的火焰攻击', NULL);
INSERT INTO `dungeons` VALUES (2, '虚空裂隙', 'hard', '[\"holy_light\"]', '不断湧出的虚空怪物，需要强大的治疗能力', NULL);
INSERT INTO `dungeons` VALUES (3, '暗影森林', 'easy', '[\"fireball\"]', '适合新手探索的森林地牢', NULL);
INSERT INTO `dungeons` VALUES (4, '初始地牢', 'easy', '[\"holy_light\", \"iron_wall\"]', '适合新手探索的初始地牢', '森林');
INSERT INTO `dungeons` VALUES (5, '黑暗走廊', 'normal', '[\"fireball\"]', '充满危险的黑暗走廊', '洞穴');
INSERT INTO `dungeons` VALUES (6, '废弃房间', 'normal', '[\"holy_light\"]', '废弃已久的房间', '废墟');
INSERT INTO `dungeons` VALUES (7, '深渊入口', 'hard', '[\"holy_light\", \"fireball\"]', '通往深渊的入口', '深渊');
INSERT INTO `dungeons` VALUES (8, '暗影领主巢穴', 'expert', '[\"holy_light\", \"fireball\", \"iron_wall\"]', '暗影领主的巢穴（Boss关卡）', '暗影');
INSERT INTO `dungeons` VALUES (9, '新章节地牢', 'normal', '[\"holy_light\"]', '新章节的开始', '森林');

-- ----------------------------
-- Table structure for enemies
-- ----------------------------
DROP TABLE IF EXISTS `enemies`;
CREATE TABLE `enemies`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `base_stats` json NOT NULL,
  `behavior_script` json NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enemies
-- ----------------------------
INSERT INTO `enemies` VALUES (1, '暗影猎手', 'normal', '{\"hp\": 120, \"armor\": 5, \"attack\": 18}', '{\"pattern\": \"strike_poison\", \"priority\": \"low_hp\"}');
INSERT INTO `enemies` VALUES (2, '虚空之手', 'boss', '{\"hp\": 320, \"armor\": 10, \"attack\": 28}', '{\"pattern\": \"aoe_then_shield\", \"priority\": \"all\"}');
INSERT INTO `enemies` VALUES (3, '灰烬骑士', 'normal', '{\"hp\": 100, \"armor\": 8, \"attack\": 20}', '{\"pattern\": \"fire_attack\", \"priority\": \"front\"}');

-- ----------------------------
-- Table structure for enemy_cards
-- ----------------------------
DROP TABLE IF EXISTS `enemy_cards`;
CREATE TABLE `enemy_cards`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enemy_id` bigint NOT NULL,
  `card_id` bigint NOT NULL,
  `weight` int NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_enemy_cards_enemy`(`enemy_id` ASC) USING BTREE,
  INDEX `fk_enemy_cards_card`(`card_id` ASC) USING BTREE,
  CONSTRAINT `fk_enemy_cards_card` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_enemy_cards_enemy` FOREIGN KEY (`enemy_id`) REFERENCES `enemies` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enemy_cards
-- ----------------------------
INSERT INTO `enemy_cards` VALUES (1, 1, 2, 2);

-- ----------------------------
-- Table structure for events
-- ----------------------------
DROP TABLE IF EXISTS `events`;
CREATE TABLE `events`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `location_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `effect_payload` json NOT NULL,
  `trigger_condition` json NULL COMMENT '触发条件（关卡编号范围、章节、房间类型等）',
  `trigger_chance` decimal(3, 2) NULL DEFAULT NULL COMMENT '触发概率（0.00-1.00，地牢事件使用）',
  `choices` json NULL COMMENT '选择项配置（如果有多个选择）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of events
-- ----------------------------
INSERT INTO `events` VALUES (1, '吟游诗人来访', 'camp', '恢复10点压力并提供随机卡牌折扣', '{\"discount_pct\": 0.2, \"stress_adjust\": -10}', NULL, NULL, NULL);
INSERT INTO `events` VALUES (2, '暗影伏击', 'dungeon', '随机敌人提前行动', '{\"enemy_preemptive\": true}', '{\"chapter\": 1, \"stage_range\": [1, 10]}', 0.30, '[]');
INSERT INTO `events` VALUES (3, '神秘商人', 'camp', '可以购买稀有道具', '{\"rare_items\": true, \"shop_refresh\": true}', NULL, NULL, NULL);
INSERT INTO `events` VALUES (4, '发现宝箱', 'dungeon', '你发现了一个宝箱，里面可能有奖励', '{\"reward\": {\"gold\": 50, \"items\": [1]}}', '{\"stage_range\": [1, 20]}', 0.40, '[]');
INSERT INTO `events` VALUES (5, '神秘商人', 'dungeon', '遇到神秘商人，可以购买道具', '{\"shop\": true, \"discount\": 0.2}', '{\"stage_range\": [1, 15]}', 0.20, '[{\"text\": \"购买\", \"effect\": {\"cost\": 100, \"reward\": {\"items\": [1]}}}, {\"text\": \"离开\", \"effect\": {}}]');
INSERT INTO `events` VALUES (6, '陷阱触发', 'dungeon', '你触发了陷阱，受到伤害', '{\"damage\": 10, \"stress\": 5}', '{\"stage_range\": [1, 20]}', 0.30, '[{\"text\": \"小心避开\", \"effect\": {\"stress\": 5}}, {\"text\": \"强行通过\", \"effect\": {\"damage\": 15}}]');
INSERT INTO `events` VALUES (7, '恢复泉水', 'dungeon', '发现恢复泉水，可以恢复生命值', '{\"heal\": 30, \"stress_reduce\": 10}', '{\"stage_range\": [1, 20]}', 0.25, '[]');

-- ----------------------------
-- Table structure for inventory
-- ----------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `quantity` int NOT NULL DEFAULT 0,
  `bind_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'unbound',
  `last_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_inventory_user_item`(`user_id` ASC, `item_id` ASC) USING BTREE,
  INDEX `fk_inventory_item`(`item_id` ASC) USING BTREE,
  CONSTRAINT `fk_inventory_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory
-- ----------------------------
INSERT INTO `inventory` VALUES (1, 1, 1, 3, 'unbound', '2025-11-18 10:23:15');
INSERT INTO `inventory` VALUES (2, 1, 2, 5, 'unbound', '2025-11-18 10:23:15');
INSERT INTO `inventory` VALUES (3, 3, 1, 5, 'unbound', '2025-11-21 11:47:51');

-- ----------------------------
-- Table structure for items
-- ----------------------------
DROP TABLE IF EXISTS `items`;
CREATE TABLE `items`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `item_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rarity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `effect_payload` json NOT NULL,
  `stack_limit` int NOT NULL DEFAULT 99,
  `shop_price` int NOT NULL DEFAULT 0,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_items_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 85 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of items
-- ----------------------------
INSERT INTO `items` VALUES (1, 'elixir_focus', '专注药剂', 'consumable', 'rare', '{\"restore_action_points\": 2}', 5, 150, '立即恢复2点行动点');
INSERT INTO `items` VALUES (2, 'healing_potion', '治疗药水', 'consumable', 'common', '{\"heal_amount\": 30}', 10, 50, '恢复30点生命值');
INSERT INTO `items` VALUES (3, 'soul_fragment', '魂晶碎片', 'material', 'epic', '{\"crafting_use\": \"unlock_trait\"}', 50, 0, '用于高级锻造和特性解锁');
INSERT INTO `items` VALUES (4, 'stress_relief', '镇静剂', 'consumable', 'rare', '{\"reduce_stress\": 20}', 3, 200, '减少20点压力值');
INSERT INTO `items` VALUES (49, 'health_potion_large', '大型治疗药水', 'consumable', 'rare', '{\"heal\": 100}', 99, 250, '恢复100点生命值。');
INSERT INTO `items` VALUES (50, 'health_potion_small', '小型治疗药水', 'consumable', 'common', '{\"heal\": 20}', 99, 50, '恢复20点生命值。');
INSERT INTO `items` VALUES (58, 'health_potion_medium', '中型治疗药水', 'consumable', 'common', '{\"heal\": 50}', 99, 120, '恢复50点生命值。');
INSERT INTO `items` VALUES (80, 'energy_drink', '能量饮料', 'consumable', 'rare', '{\"duration\": 1, \"action_points\": 2}', 50, 200, '本回合增加2点行动点。');
INSERT INTO `items` VALUES (81, 'blessing_scroll', '祝福卷轴', 'consumable', 'epic', '{\"blessing\": true, \"duration\": 3, \"attack_bonus\": 20, \"defense_bonus\": 20}', 20, 500, '获得祝福，3回合内增加20点攻击和防御。');
INSERT INTO `items` VALUES (82, 'gold_pack_small', '小金币包', 'currency_bundle', 'common', '{\"gold\": 500}', 99, 100, '打开后获得500金币。');
INSERT INTO `items` VALUES (83, 'gold_pack_medium', '中金币包', 'currency_bundle', 'rare', '{\"gold\": 1500}', 99, 300, '打开后获得1500金币。');
INSERT INTO `items` VALUES (84, 'gold_pack_large', '大金币包', 'currency_bundle', 'epic', '{\"gold\": 5000}', 99, 1000, '打开后获得5000金币。');

-- ----------------------------
-- Table structure for player_actions
-- ----------------------------
DROP TABLE IF EXISTS `player_actions`;
CREATE TABLE `player_actions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `action_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `source_scene` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `metadata` json NOT NULL,
  `occurred_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_actions_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_player_actions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_actions
-- ----------------------------

-- ----------------------------
-- Table structure for player_characters
-- ----------------------------
DROP TABLE IF EXISTS `player_characters`;
CREATE TABLE `player_characters`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `base_hp` int NOT NULL,
  `hp_per_level` int NOT NULL,
  `lore` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_player_characters_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_characters
-- ----------------------------
INSERT INTO `player_characters` VALUES (1, 'warden', '守望者', 80, 12, '古老仪式守护者，擅长防御和支援');
INSERT INTO `player_characters` VALUES (2, 'occultist', '秘术师', 60, 9, '操纵禁忌咒语的学者，精通暗影魔法');
INSERT INTO `player_characters` VALUES (3, 'ranger', '游侠', 70, 10, '森林中的猎手，擅长远程攻击和追踪');
INSERT INTO `player_characters` VALUES (4, 'warrior', '战士', 100, 15, '勇猛的近战战士，拥有强大的物理攻击力');

-- ----------------------------
-- Table structure for runs
-- ----------------------------
DROP TABLE IF EXISTS `runs`;
CREATE TABLE `runs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `user_player_character_id` bigint NOT NULL,
  `dungeon_id` bigint NOT NULL,
  `stage_number` int NOT NULL,
  `chapter_number` int NOT NULL,
  `difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `preparation_snapshot` json NOT NULL,
  `current_stage_progress` json NOT NULL,
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `started_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ended_at` timestamp NULL DEFAULT NULL,
  `reward_snapshot` json NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_runs_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_runs_character`(`user_player_character_id` ASC) USING BTREE,
  INDEX `idx_runs_dungeon`(`dungeon_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of runs
-- ----------------------------
INSERT INTO `runs` VALUES (6, 3, 2, 1, 1, 1, 'easy', '{\"notes\": \"我传送到了一个新的地方\", \"cardIds\": [], \"consumables\": [], \"cardCharacterIds\": []}', '{\"status\": \"completed\", \"eventLog\": [], \"battleLog\": [], \"currentRoom\": \"Room-3\", \"visitedRooms\": [1, 2, 3], \"currentRoomId\": 3, \"exploredRooms\": 2, \"awaitingBattle\": false, \"defeatedEnemies\": 0}', 'victory', '2025-11-25 14:49:51', '2025-11-25 14:54:20', '{\"key\": {}}');

-- ----------------------------
-- Table structure for shop_offers
-- ----------------------------
DROP TABLE IF EXISTS `shop_offers`;
CREATE TABLE `shop_offers`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `offer_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `target_id` bigint NOT NULL,
  `price` bigint NOT NULL,
  `display_order` int NOT NULL DEFAULT 0,
  `refresh_rule` json NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_offers
-- ----------------------------
INSERT INTO `shop_offers` VALUES (1, 'card', 1, 180, 1, '{\"weight\": 30, \"limit_per_day\": 1}');
INSERT INTO `shop_offers` VALUES (2, 'item', 1, 150, 2, '{\"limit_per_day\": 2}');
INSERT INTO `shop_offers` VALUES (3, 'card_character', 1, 300, 1, '{\"refresh_type\": \"daily\", \"limit_per_user\": 10}');
INSERT INTO `shop_offers` VALUES (4, 'card_character', 2, 300, 2, '{\"refresh_type\": \"daily\", \"limit_per_user\": 10}');
INSERT INTO `shop_offers` VALUES (5, 'card_character', 3, 300, 3, '{\"refresh_type\": \"daily\", \"limit_per_user\": 10}');
INSERT INTO `shop_offers` VALUES (6, 'card_character', 4, 800, 4, '{\"refresh_type\": \"daily\", \"limit_per_user\": 5}');
INSERT INTO `shop_offers` VALUES (7, 'card_character', 5, 800, 5, '{\"refresh_type\": \"daily\", \"limit_per_user\": 5}');
INSERT INTO `shop_offers` VALUES (8, 'card_character', 6, 800, 6, '{\"refresh_type\": \"daily\", \"limit_per_user\": 5}');
INSERT INTO `shop_offers` VALUES (9, 'card_character', 7, 2000, 7, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 2}');
INSERT INTO `shop_offers` VALUES (10, 'card_character', 8, 2000, 8, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 2}');
INSERT INTO `shop_offers` VALUES (11, 'card_character', 9, 5000, 9, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 1}');
INSERT INTO `shop_offers` VALUES (12, 'card', 1, 200, 10, '{\"refresh_type\": \"daily\", \"limit_per_user\": 20}');
INSERT INTO `shop_offers` VALUES (13, 'card', 2, 200, 11, '{\"refresh_type\": \"daily\", \"limit_per_user\": 20}');
INSERT INTO `shop_offers` VALUES (14, 'card', 3, 200, 12, '{\"refresh_type\": \"daily\", \"limit_per_user\": 20}');
INSERT INTO `shop_offers` VALUES (15, 'card', 4, 800, 13, '{\"refresh_type\": \"daily\", \"limit_per_user\": 10}');
INSERT INTO `shop_offers` VALUES (16, 'card', 5, 800, 14, '{\"refresh_type\": \"daily\", \"limit_per_user\": 10}');
INSERT INTO `shop_offers` VALUES (17, 'card', 6, 800, 15, '{\"refresh_type\": \"daily\", \"limit_per_user\": 10}');
INSERT INTO `shop_offers` VALUES (18, 'card', 7, 2000, 16, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 3}');
INSERT INTO `shop_offers` VALUES (19, 'card', 8, 2000, 17, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 3}');
INSERT INTO `shop_offers` VALUES (20, 'card', 9, 5000, 18, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 1}');
INSERT INTO `shop_offers` VALUES (21, 'card', 10, 300, 19, '{\"refresh_type\": \"daily\", \"limit_per_user\": 5}');
INSERT INTO `shop_offers` VALUES (22, 'card', 11, 1000, 20, '{\"refresh_type\": \"daily\", \"limit_per_user\": 3}');
INSERT INTO `shop_offers` VALUES (23, 'card', 12, 2500, 21, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 2}');
INSERT INTO `shop_offers` VALUES (24, 'card', 13, 6000, 22, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 1}');
INSERT INTO `shop_offers` VALUES (25, 'card', 14, 300, 23, '{\"refresh_type\": \"daily\", \"limit_per_user\": 5}');
INSERT INTO `shop_offers` VALUES (26, 'card', 15, 1000, 24, '{\"refresh_type\": \"daily\", \"limit_per_user\": 3}');
INSERT INTO `shop_offers` VALUES (27, 'card', 16, 2500, 25, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 2}');
INSERT INTO `shop_offers` VALUES (28, 'card', 17, 400, 26, '{\"refresh_type\": \"daily\", \"limit_per_user\": 5}');
INSERT INTO `shop_offers` VALUES (29, 'card', 18, 1200, 27, '{\"refresh_type\": \"daily\", \"limit_per_user\": 3}');
INSERT INTO `shop_offers` VALUES (30, 'card', 19, 3000, 28, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 1}');
INSERT INTO `shop_offers` VALUES (31, 'item', 1, 50, 29, '{\"refresh_type\": \"daily\", \"limit_per_user\": 99}');
INSERT INTO `shop_offers` VALUES (32, 'item', 2, 120, 30, '{\"refresh_type\": \"daily\", \"limit_per_user\": 50}');
INSERT INTO `shop_offers` VALUES (33, 'item', 3, 250, 31, '{\"refresh_type\": \"daily\", \"limit_per_user\": 20}');
INSERT INTO `shop_offers` VALUES (34, 'item', 4, 80, 32, '{\"refresh_type\": \"daily\", \"limit_per_user\": 99}');
INSERT INTO `shop_offers` VALUES (35, 'item', 5, 200, 33, '{\"refresh_type\": \"daily\", \"limit_per_user\": 30}');
INSERT INTO `shop_offers` VALUES (36, 'item', 6, 500, 34, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 10}');
INSERT INTO `shop_offers` VALUES (37, 'item', 7, 10, 35, '{\"refresh_type\": \"daily\", \"limit_per_user\": 999}');
INSERT INTO `shop_offers` VALUES (38, 'item', 8, 50, 36, '{\"refresh_type\": \"daily\", \"limit_per_user\": 200}');
INSERT INTO `shop_offers` VALUES (39, 'item', 9, 200, 37, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 50}');
INSERT INTO `shop_offers` VALUES (40, 'item', 10, 100, 38, '{\"refresh_type\": \"daily\", \"limit_per_user\": 10}');
INSERT INTO `shop_offers` VALUES (41, 'item', 11, 300, 39, '{\"refresh_type\": \"daily\", \"limit_per_user\": 5}');
INSERT INTO `shop_offers` VALUES (42, 'item', 12, 1000, 40, '{\"refresh_type\": \"weekly\", \"limit_per_user\": 3}');

-- ----------------------------
-- Table structure for skills
-- ----------------------------
DROP TABLE IF EXISTS `skills`;
CREATE TABLE `skills`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `effect_payload` json NOT NULL,
  `required_level` int NOT NULL,
  `position_in_tree` json NOT NULL,
  `unlock_path` json NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skills
-- ----------------------------
INSERT INTO `skills` VALUES (1, 'warden', 'god', 'test', '{}', 0, '1', '0');
INSERT INTO `skills` VALUES (2, 'warden', 'god', 'test', '{}', 0, '1', '0');

-- ----------------------------
-- Table structure for stages
-- ----------------------------
DROP TABLE IF EXISTS `stages`;
CREATE TABLE `stages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stage_number` int NOT NULL COMMENT '关卡编号（1, 2, 3...）',
  `chapter_number` int NOT NULL COMMENT '章节编号（每5关为一章：1, 2, 3...）',
  `dungeon_id` bigint NULL DEFAULT NULL COMMENT '关联的地牢模板ID',
  `stage_name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关卡名称',
  `difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '难度：easy/normal/hard/expert',
  `is_boss_stage` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为Boss关卡（每章第5关）',
  `enemy_pool` json NULL COMMENT '敌人池配置（用于随机生成敌人）',
  `event_pool` json NULL COMMENT '事件池配置（用于随机生成事件）',
  `reward_pool` json NULL COMMENT '奖励池配置（关卡完成奖励）',
  `exploration_map` json NULL COMMENT '探索地图配置（房间、路径、探索点）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '关卡描述',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stage_number`(`stage_number` ASC) USING BTREE,
  INDEX `fk_stages_dungeon`(`dungeon_id` ASC) USING BTREE,
  CONSTRAINT `fk_stages_dungeon` FOREIGN KEY (`dungeon_id`) REFERENCES `dungeons` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of stages
-- ----------------------------
INSERT INTO `stages` VALUES (1, 1, 1, 1, '初始地牢', 'easy', 0, '{\"enemies\": [{\"id\": 1, \"weight\": 3}, {\"id\": 2, \"weight\": 1}]}', '{\"events\": [{\"id\": 2, \"weight\": 2}]}', '{\"exp\": 100, \"gold\": 50, \"items\": []}', '{\"paths\": [[1, 2], [2, 3]], \"rooms\": [{\"id\": 1, \"type\": \"start\"}, {\"id\": 2, \"type\": \"normal\"}, {\"id\": 3, \"type\": \"end\"}]}', '新手教程关卡，熟悉游戏机制');
INSERT INTO `stages` VALUES (2, 2, 1, 2, '黑暗走廊', 'normal', 0, '{\"enemies\": [{\"id\": 1, \"weight\": 2}, {\"id\": 2, \"weight\": 2}, {\"id\": 3, \"weight\": 1}]}', '{\"events\": [{\"id\": 2, \"weight\": 1}, {\"id\": 3, \"weight\": 1}]}', '{\"exp\": 150, \"gold\": 80, \"items\": [1]}', '{\"paths\": [[1, 2], [2, 3], [3, 4]], \"rooms\": [{\"id\": 1, \"type\": \"start\"}, {\"id\": 2, \"type\": \"normal\"}, {\"id\": 3, \"type\": \"event\"}, {\"id\": 4, \"type\": \"end\"}]}', '探索黑暗的走廊，小心敌人');
INSERT INTO `stages` VALUES (3, 3, 1, 3, '废弃房间', 'normal', 0, '{\"enemies\": [{\"id\": 2, \"weight\": 3}, {\"id\": 3, \"weight\": 2}]}', '{\"events\": [{\"id\": 2, \"weight\": 2}, {\"id\": 3, \"weight\": 1}]}', '{\"exp\": 200, \"gold\": 100, \"items\": []}', '{\"paths\": [[1, 2], [2, 3], [3, 4]], \"rooms\": [{\"id\": 1, \"type\": \"start\"}, {\"id\": 2, \"type\": \"normal\"}, {\"id\": 3, \"type\": \"normal\"}, {\"id\": 4, \"type\": \"end\"}]}', '探索废弃的房间，寻找宝藏');
INSERT INTO `stages` VALUES (4, 4, 1, 4, '深渊入口', 'hard', 0, '{\"enemies\": [{\"id\": 3, \"weight\": 2}, {\"id\": 2, \"weight\": 1}]}', '{\"events\": [{\"id\": 2, \"weight\": 1}]}', '{\"exp\": 300, \"gold\": 150, \"items\": [2]}', '{\"paths\": [[1, 2], [2, 3], [3, 4], [4, 5]], \"rooms\": [{\"id\": 1, \"type\": \"start\"}, {\"id\": 2, \"type\": \"normal\"}, {\"id\": 3, \"type\": \"event\"}, {\"id\": 4, \"type\": \"normal\"}, {\"id\": 5, \"type\": \"end\"}]}', '接近深渊，危险增加');
INSERT INTO `stages` VALUES (5, 5, 1, 5, '暗影领主', 'expert', 1, '{\"enemies\": [{\"id\": 2, \"weight\": 1}]}', '{\"events\": []}', '{\"exp\": 500, \"gold\": 300, \"cards\": [1], \"items\": [1, 2]}', '{\"paths\": [[1, 2]], \"rooms\": [{\"id\": 1, \"type\": \"start\"}, {\"id\": 2, \"type\": \"boss\"}]}', '第1章Boss：暗影领主');

-- ----------------------------
-- Table structure for stress_debuff_configs
-- ----------------------------
DROP TABLE IF EXISTS `stress_debuff_configs`;
CREATE TABLE `stress_debuff_configs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stress_level` int NOT NULL,
  `debuff_name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `debuff_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `effect_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `trigger_chance` decimal(3, 2) NOT NULL,
  `effect_payload` json NOT NULL,
  `is_persistent` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of stress_debuff_configs
-- ----------------------------
INSERT INTO `stress_debuff_configs` VALUES (1, 1, '轻微焦虑', 'mental', '回合开始时10%概率跳过行动', 0.10, '{\"duration\": 1, \"skip_round\": true}', 1);
INSERT INTO `stress_debuff_configs` VALUES (2, 2, '惊惧低语', 'mental', '回合开始时30%概率跳过行动', 0.30, '{\"duration\": 2, \"skip_round\": true}', 1);
INSERT INTO `stress_debuff_configs` VALUES (3, 2, '注意力分散', 'combat', '攻击命中率降低15%', 0.25, '{\"hit_rate_reduction\": 0.15}', 1);
INSERT INTO `stress_debuff_configs` VALUES (4, 3, '血液沸腾', 'combat', '受到攻击时额外承受15%伤害', 0.25, '{\"extra_damage_pct\": 0.15}', 1);
INSERT INTO `stress_debuff_configs` VALUES (5, 3, '恐慌发作', 'behavioral', '无法使用主动技能', 0.20, '{\"disable_active_skills\": true}', 1);
INSERT INTO `stress_debuff_configs` VALUES (6, 4, '精神崩溃', 'mental', '每回合损失5点血量', 0.50, '{\"hp_loss_per_round\": 5}', 1);

-- ----------------------------
-- Table structure for user_achievements
-- ----------------------------
DROP TABLE IF EXISTS `user_achievements`;
CREATE TABLE `user_achievements`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID（关联users.id）',
  `achievement_id` bigint NOT NULL COMMENT '成就ID（关联achievements.id）',
  `is_completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已完成（0-未完成，1-已完成）',
  `progress` int NOT NULL DEFAULT 0 COMMENT '完成进度（0-100）',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间（NULL表示未完成）',
  `reward_claimed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已领取奖励（0-未领取，1-已领取）',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（首次触发该成就时）',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_achievement`(`user_id` ASC, `achievement_id` ASC) USING BTREE COMMENT '确保每个用户每个成就只有一条记录',
  INDEX `idx_user_achievements_user`(`user_id` ASC) USING BTREE COMMENT '按用户查询索引',
  INDEX `idx_user_achievements_achievement`(`achievement_id` ASC) USING BTREE COMMENT '按成就查询索引',
  INDEX `idx_user_achievements_completed`(`user_id` ASC, `is_completed` ASC) USING BTREE COMMENT '按用户和完成状态查询索引',
  CONSTRAINT `fk_user_achievements_achievement` FOREIGN KEY (`achievement_id`) REFERENCES `achievements` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_achievements_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户成就实例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_achievements
-- ----------------------------
INSERT INTO `user_achievements` VALUES (1, 3, 1, 1, 0, NULL, 0, '2025-11-24 15:12:43', '2025-11-24 15:33:07');

-- ----------------------------
-- Table structure for user_card_characters
-- ----------------------------
DROP TABLE IF EXISTS `user_card_characters`;
CREATE TABLE `user_card_characters`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `card_character_id` bigint NOT NULL,
  `quantity` int NOT NULL DEFAULT 1 COMMENT '拥有数量（用于升星）',
  `current_hp` int NOT NULL,
  `current_armor` int NOT NULL DEFAULT 0,
  `is_deployed` tinyint(1) NOT NULL DEFAULT 0,
  `deployed_round` int NOT NULL DEFAULT 0,
  `current_star_level` tinyint NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_ucc_user`(`user_id` ASC) USING BTREE,
  INDEX `fk_ucc_card_character`(`card_character_id` ASC) USING BTREE,
  CONSTRAINT `fk_ucc_card_character` FOREIGN KEY (`card_character_id`) REFERENCES `card_characters` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ucc_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_card_characters
-- ----------------------------
INSERT INTO `user_card_characters` VALUES (1, 3, 1, 2, 40, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (2, 1, 2, 1, 55, 0, 0, 0, 1);
INSERT INTO `user_card_characters` VALUES (3, 3, 2, 1, 45, 10, 1, 2, 3);

-- ----------------------------
-- Table structure for user_cards
-- ----------------------------
DROP TABLE IF EXISTS `user_cards`;
CREATE TABLE `user_cards`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `card_id` bigint NOT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `level` int NOT NULL DEFAULT 1,
  `loadout_id` bigint NULL DEFAULT NULL,
  `equipped_to_user_card_character_id` bigint NULL DEFAULT NULL,
  `acquired_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `acquired_source` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_used_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_user_cards_user`(`user_id` ASC) USING BTREE,
  INDEX `fk_user_cards_card`(`card_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_cards_card` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_cards_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_cards
-- ----------------------------
INSERT INTO `user_cards` VALUES (1, 1, 1, 2, 1, NULL, NULL, '2025-11-18 10:23:15', 'camp_shop', NULL);
INSERT INTO `user_cards` VALUES (2, 3, 3, 1, 1, NULL, NULL, '2025-11-18 10:23:15', 'quest_reward', NULL);

-- ----------------------------
-- Table structure for user_player_character_skills
-- ----------------------------
DROP TABLE IF EXISTS `user_player_character_skills`;
CREATE TABLE `user_player_character_skills`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `player_character_id` bigint NOT NULL COMMENT '职业模板ID（关联player_characters.id）',
  `skill_id` bigint NOT NULL COMMENT '技能模板ID（关联skills.id）',
  `unlocked_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_character_skill`(`user_id` ASC, `player_character_id` ASC, `skill_id` ASC) USING BTREE COMMENT '用户+职业+技能唯一约束，防止重复解锁',
  INDEX `idx_user_character`(`user_id` ASC, `player_character_id` ASC) USING BTREE COMMENT '用于查询用户某个职业的所有技能',
  INDEX `idx_skill`(`skill_id` ASC) USING BTREE COMMENT '用于查询技能被哪些用户解锁',
  INDEX `fk_ups_character`(`player_character_id` ASC) USING BTREE,
  CONSTRAINT `fk_ups_character` FOREIGN KEY (`player_character_id`) REFERENCES `player_characters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ups_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ups_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户职业技能解锁记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_player_character_skills
-- ----------------------------
INSERT INTO `user_player_character_skills` VALUES (1, 3, 1, 1, '2025-11-21 10:17:29');
INSERT INTO `user_player_character_skills` VALUES (3, 3, 1, 2, '2025-11-21 10:42:11');

-- ----------------------------
-- Table structure for user_player_characters
-- ----------------------------
DROP TABLE IF EXISTS `user_player_characters`;
CREATE TABLE `user_player_characters`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `player_character_id` bigint NOT NULL,
  `max_hp` int NOT NULL,
  `current_hp` int NOT NULL,
  `max_action_points` int NOT NULL,
  `current_action_points` int NOT NULL,
  `current_stress` int NOT NULL DEFAULT 0,
  `stress_level` int NOT NULL DEFAULT 1,
  `stress_debuffs` json NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_player`(`user_id` ASC, `player_character_id` ASC) USING BTREE,
  INDEX `fk_upc_template`(`player_character_id` ASC) USING BTREE,
  CONSTRAINT `fk_upc_template` FOREIGN KEY (`player_character_id`) REFERENCES `player_characters` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_upc_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_player_characters
-- ----------------------------
INSERT INTO `user_player_characters` VALUES (1, 1, 1, 128, 120, 8, 8, 15, 1, '[]');
INSERT INTO `user_player_characters` VALUES (2, 3, 1, 78, 60, 6, 6, 30, 2, '[]');

-- ----------------------------
-- Table structure for user_stage_progress
-- ----------------------------
DROP TABLE IF EXISTS `user_stage_progress`;
CREATE TABLE `user_stage_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `stage_number` int NOT NULL COMMENT '关卡编号',
  `chapter_number` int NOT NULL COMMENT '章节编号',
  `is_passed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已通过',
  `is_unlocked` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否已解锁（通过前一关后解锁）',
  `best_result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最佳结果：victory/defeat',
  `passed_at` timestamp NULL DEFAULT NULL COMMENT '首次通过时间',
  `attempt_count` int NOT NULL DEFAULT 0 COMMENT '尝试次数',
  `best_score` int NULL DEFAULT NULL COMMENT '最佳评分（可选）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_stage`(`user_id` ASC, `stage_number` ASC) USING BTREE,
  CONSTRAINT `fk_user_stage_progress_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_stage_progress
-- ----------------------------
INSERT INTO `user_stage_progress` VALUES (1, 1, 1, 1, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `user_stage_progress` VALUES (2, 3, 1, 1, 1, 1, 'victory', '2025-11-25 14:54:20', 2, NULL);
INSERT INTO `user_stage_progress` VALUES (5, 3, 2, 1, 0, 1, NULL, NULL, 0, NULL);

-- ----------------------------
-- Table structure for user_wallets
-- ----------------------------
DROP TABLE IF EXISTS `user_wallets`;
CREATE TABLE `user_wallets`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `currency_type` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `balance` bigint NOT NULL DEFAULT 0,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wallet_user_currency`(`user_id` ASC, `currency_type` ASC) USING BTREE,
  CONSTRAINT `fk_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_wallets
-- ----------------------------
INSERT INTO `user_wallets` VALUES (1, 1, 'gold', 5000, '2025-11-18 10:23:15');
INSERT INTO `user_wallets` VALUES (2, 1, 'soulstone', 100, '2025-11-18 10:23:15');
INSERT INTO `user_wallets` VALUES (3, 3, 'gold', 50, '2025-11-21 11:45:44');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `player_level` int NOT NULL DEFAULT 1,
  `player_exp` bigint NOT NULL DEFAULT 0,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_users_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '测试玩家1', 'test1@example.com', '', 5, 4500, 'active', '2025-11-18 10:23:15');
INSERT INTO `users` VALUES (2, '测试玩家2', 'test2@example.com', NULL, 3, 2000, 'active', '2025-11-18 10:23:15');
INSERT INTO `users` VALUES (3, 'xiaosheng', '1982415487@qq.com', '$2a$10$AWEyl0VWvG9Fxpbl83/oQOrsjhhmMItCQ8/Gll/ub75rUR860eZ/C', 1, 0, 'active', '2025-11-18 15:16:39');

SET FOREIGN_KEY_CHECKS = 1;
