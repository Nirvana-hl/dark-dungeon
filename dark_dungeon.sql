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

 Date: 06/01/2026 08:59:13
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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of card_character_traits
-- ----------------------------
INSERT INTO `card_character_traits` VALUES (1, 1, '星辉祝福', 'positive', '{\"heal_allies\": 2}', '{\"2\": {\"heal_allies\": 3}, \"3\": {\"heal_allies\": 4}, \"4\": {\"heal_allies\": 5}}', '治疗所有角色，恢复2点生命');
INSERT INTO `card_character_traits` VALUES (2, 2, '钢铁护盾', 'positive', '{\"armor_bonus\": 3}', '{\"2\": {\"armor_bonus\": 5}, \"3\": {\"armor_bonus\": 8}}', '提供额外护甲');
INSERT INTO `card_character_traits` VALUES (3, 13, '神圣治疗', 'positive', '{\"heal_allies\": 30}', '{\"2\": {\"heal_allies\": 35}, \"3\": {\"heal_allies\": 40}, \"4\": {\"heal_allies\": 50}}', '给团队大额回血');

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
  `card_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'player' COMMENT '卡牌类型：player(玩家)/enemy(敌人)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_card_characters_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of card_characters
-- ----------------------------
INSERT INTO `card_characters` VALUES (1, 'priestess', '星辉祭司', 'support', 'human', 'rare', 40, 8, 2, 1, 5, '{\"stats\": {\"hp\": 10, \"attack\": 2}, \"traits\": {\"heal_allies\": {\"2\": 1, \"3\": 2}}}', '[{\"name\": \"全体治疗\", \"value\": 2}]', 280, '神圣的治愈者，能够恢复队友的生命', 'player');
INSERT INTO `card_characters` VALUES (2, 'shield_guard', '重甲盾卫', 'protector', 'human', 'common', 55, 6, 1, 1, 5, '{\"stats\": {\"hp\": 15, \"armor\": 3}}', '[{\"name\": \"护盾\", \"value\": 3}]', 160, '坚不可摧的防御者', 'player');
INSERT INTO `card_characters` VALUES (3, 'fire_mage', '火焰法师', 'mage', 'human', 'epic', 35, 25, 3, 1, 5, '{\"stats\": {\"hp\": 8, \"attack\": 5}, \"traits\": {\"fire_damage\": {\"2\": 5, \"3\": 10}}}', '[{\"name\": \"火焰冲击\", \"value\": 25}]', 500, '掌控火焰的毁灭法师', 'player');
INSERT INTO `card_characters` VALUES (4, 'shadow_assassin', '暗影刺客', 'assassin', 'occult', 'epic', 30, 30, 2, 1, 5, '{\"stats\": {\"hp\": 5, \"attack\": 8}, \"traits\": {\"critical_chance\": {\"2\": 0.1, \"3\": 0.2}}}', '[{\"name\": \"致命一击\", \"value\": 30}]', 600, '暗影中的致命杀手', 'player');
INSERT INTO `card_characters` VALUES (6, 'warrior_common', '新兵战士', 'warrior', 'human', 'common', 70, 5, 0, 1, 5, '{\"stats\": {\"hp\": 10, \"attack\": 3}}', '[]', 300, '一名训练有素的新兵，擅长近战。', 'player');
INSERT INTO `card_characters` VALUES (7, 'ranger_common', '见习游侠', 'ranger', 'human', 'common', 70, 18, 2, 1, 5, '{\"stats\": {\"hp\": 8, \"attack\": 4}}', '[]', 300, '年轻的游侠，擅长远程攻击。', 'player');
INSERT INTO `card_characters` VALUES (8, 'priest_common', '初级祭司', 'priest', 'human', 'common', 60, 10, 2, 1, 5, '{\"stats\": {\"hp\": 6, \"attack\": 2}}', '[]', 300, '初出茅庐的祭司，能够治疗队友。', 'player');
INSERT INTO `card_characters` VALUES (9, 'warrior_rare', '精英战士', 'warrior', 'human', 'rare', 100, 10, 2, 2, 5, '{\"stats\": {\"hp\": 12, \"attack\": 4}, \"traits\": {\"shield_bash\": {\"2\": 1, \"3\": 2}}}', '[{\"name\": \"盾击\", \"type\": \"positive\", \"effect_payload\": {\"stun_chance\": 0.2}}]', 800, '经验丰富的战士，能够使用盾击。', 'player');
INSERT INTO `card_characters` VALUES (10, 'occultist_rare', '神秘学者', 'occultist', 'occult', 'rare', 65, 25, 3, 2, 5, '{\"stats\": {\"hp\": 7, \"attack\": 5}, \"traits\": {\"dark_bolt\": {\"2\": 2, \"3\": 3}}}', '[{\"name\": \"暗影箭\", \"type\": \"positive\", \"effect_payload\": {\"damage_multiplier\": 1.2}}]', 800, '掌握黑暗魔法的学者。', 'player');
INSERT INTO `card_characters` VALUES (11, 'ranger_rare', '资深游侠', 'ranger', 'human', 'rare', 85, 22, 2, 2, 5, '{\"stats\": {\"hp\": 9, \"attack\": 5}, \"traits\": {\"multi_shot\": {\"2\": 1, \"3\": 2}}}', '[{\"name\": \"多重射击\", \"type\": \"positive\", \"effect_payload\": {\"targets\": 2}}]', 800, '技艺精湛的游侠，能够同时攻击多个目标。', 'player');
INSERT INTO `card_characters` VALUES (12, 'warrior_epic', '传奇战士', 'warrior', 'human', 'epic', 120, 25, 2, 3, 5, '{\"stats\": {\"hp\": 15, \"attack\": 6}, \"traits\": {\"berserker_rage\": {\"2\": 1, \"3\": 2, \"4\": 3}}}', '[{\"name\": \"狂战士之怒\", \"type\": \"positive\", \"effect_payload\": {\"duration\": 3, \"attack_bonus\": 10}}]', 2000, '传说中的战士，拥有强大的战斗意志。', 'player');
INSERT INTO `card_characters` VALUES (13, 'priest_epic', '大祭司', 'priest', 'divine', 'epic', 90, 15, 2, 3, 5, '{\"stats\": {\"hp\": 10, \"attack\": 3}, \"traits\": {\"divine_heal\": {\"2\": 2, \"3\": 3, \"4\": 4}}}', '[{\"name\": \"神圣治疗\", \"type\": \"positive\", \"effect_payload\": {\"heal_all\": 30}}]', 2000, '受到神祇祝福的大祭司，能够治愈所有队友。', 'player');
INSERT INTO `card_characters` VALUES (14, 'dragon_knight', '龙骑士', 'warrior', 'divine', 'legendary', 150, 10, 3, 3, 5, '{\"stats\": {\"hp\": 20, \"attack\": 8}, \"traits\": {\"dragon_breath\": {\"2\": 1, \"3\": 2, \"4\": 3, \"5\": 4}}}', '[{\"name\": \"龙息\", \"type\": \"positive\", \"effect_payload\": {\"aoe_damage\": 50, \"burn_chance\": 0.5}}]', 5000, '传说中的龙骑士，能够驾驭巨龙的力量。', 'player');
INSERT INTO `card_characters` VALUES (15, 'enemy_skeleton_warrior', '骷髅战士', 'warrior', 'undead', 'common', 80, 15, 2, 1, 1, '{\"stats\": {\"hp\": 10, \"attack\": 3}}', '[]', 0, '不死族战士，只会基础攻击', 'enemy');
INSERT INTO `card_characters` VALUES (16, 'enemy_skeleton_archer', '骷髅弓箭手', 'ranger', 'undead', 'common', 60, 20, 2, 1, 1, '{\"stats\": {\"hp\": 8, \"attack\": 4}}', '[]', 0, '远程攻击的不死族弓手', 'enemy');
INSERT INTO `card_characters` VALUES (17, 'enemy_ghoul', '食尸鬼', 'assassin', 'undead', 'rare', 100, 25, 2, 1, 1, '{\"stats\": {\"hp\": 12, \"attack\": 5}}', '[{\"name\": \"撕裂\", \"type\": \"positive\", \"effect_payload\": {\"bleed\": true}}]', 0, '凶残的不死族捕食者', 'enemy');
INSERT INTO `card_characters` VALUES (18, 'enemy_wight', '尸妖', 'warrior', 'undead', 'rare', 120, 22, 2, 1, 1, '{\"stats\": {\"hp\": 15, \"attack\": 4}}', '[{\"name\": \"死亡之触\", \"type\": \"positive\", \"effect_payload\": {\"drain\": true}}]', 0, '吸取生命力的不死族战士', 'enemy');
INSERT INTO `card_characters` VALUES (19, 'enemy_imp', '小恶魔', 'mage', 'demon', 'common', 50, 22, 2, 1, 1, '{\"stats\": {\"hp\": 6, \"attack\": 5}}', '[]', 0, '低等恶魔，会发射火球', 'enemy');
INSERT INTO `card_characters` VALUES (20, 'enemy_demon_warrior', '恶魔战士', 'warrior', 'demon', 'common', 90, 18, 2, 1, 1, '{\"stats\": {\"hp\": 12, \"attack\": 3}}', '[]', 0, '普通恶魔战士', 'enemy');
INSERT INTO `card_characters` VALUES (21, 'enemy_hellfire_wizard', '狱火巫师', 'mage', 'demon', 'rare', 70, 30, 3, 1, 1, '{\"stats\": {\"hp\": 8, \"attack\": 6}}', '[{\"name\": \"火焰风暴\", \"type\": \"positive\", \"effect_payload\": {\"aoe_damage\": 25}}]', 0, '掌握火焰魔法的中阶恶魔', 'enemy');
INSERT INTO `card_characters` VALUES (22, 'enemy_demon_lord', '恶魔领主', 'warrior', 'demon', 'epic', 200, 35, 3, 1, 1, '{\"stats\": {\"hp\": 25, \"attack\": 8}}', '[{\"name\": \"恶魔之怒\", \"type\": \"positive\", \"effect_payload\": {\"buff_attack\": 15}}]', 0, '统领低等恶魔的强者', 'enemy');
INSERT INTO `card_characters` VALUES (23, 'enemy_lich', '巫妖', 'mage', 'undead', 'epic', 300, 40, 3, 1, 1, '{\"stats\": {\"hp\": 30, \"attack\": 10}}', '[{\"name\": \"死亡射线\", \"type\": \"positive\", \"effect_payload\": {\"damage\": 60}}]', 0, '不死族的大法师', 'enemy');
INSERT INTO `card_characters` VALUES (24, 'enemy_demon_king', '魔王', 'warrior', 'demon', 'legendary', 500, 50, 3, 1, 1, '{\"stats\": {\"hp\": 40, \"attack\": 12}}', '[{\"name\": \"毁灭之息\", \"type\": \"positive\", \"effect_payload\": {\"aoe_damage\": 80}}]', 0, '统治恶魔世界的王者', 'enemy');

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
  `usage_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'player' COMMENT '使用类型：player-玩家卡牌, enemy-敌人卡牌',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_cards_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cards
-- ----------------------------
INSERT INTO `cards` VALUES (1, 'holy_light', '圣光术', 'spell', 'rare', 'none', 1, '{}', '{\"type\": \"heal_single\", \"heal_amount\": 12}', '{}', '{\"amount\": 120, \"currency_type\": \"gold\"}', '对友军单体治疗12点生命值', 'player');
INSERT INTO `cards` VALUES (2, 'fireball', '火球术', 'spell', 'common', 'none', 2, '{}', '{\"type\": \"damage_single\", \"damage\": 30}', '{}', '{\"amount\": 80, \"currency_type\": \"gold\"}', '对单个敌人造成30点火焰伤害', 'player');
INSERT INTO `cards` VALUES (3, 'iron_wall', '铁壁护甲', 'equipment', 'common', 'armor', 0, '{\"armor\": 6}', '{\"type\": \"buff_armor\"}', '{}', '{\"amount\": 90, \"currency_type\": \"gold\"}', '强化持有者的护甲，提供6点护甲值', 'player');
INSERT INTO `cards` VALUES (4, 'blood_sword', '血刃', 'equipment', 'epic', 'weapon', 0, '{\"attack\": 15, \"lifesteal\": 0.1}', '{\"type\": \"buff_attack\", \"lifesteal\": true}', '{}', '{\"amount\": 300, \"currency_type\": \"gold\"}', '强大的武器，攻击力+15，攻击时恢复10%伤害的生命值', 'player');
INSERT INTO `cards` VALUES (33, 'heal', '治疗术', 'spell', 'common', 'none', 2, '{}', '{\"heal\": 25, \"target\": \"ally\"}', '{}', '{\"amount\": 200, \"currency_type\": \"gold\"}', '恢复友方25点生命值。', 'player');
INSERT INTO `cards` VALUES (34, 'shield', '护盾术', 'spell', 'common', 'none', 1, '{}', '{\"armor\": 15, \"target\": \"ally\", \"duration\": 2}', '{}', '{\"amount\": 200, \"currency_type\": \"gold\"}', '为友方提供15点护甲，持续2回合。', 'player');
INSERT INTO `cards` VALUES (35, 'meteor', '陨石术', 'spell', 'rare', 'none', 4, '{}', '{\"aoe\": true, \"damage\": 60, \"target\": \"enemy\"}', '{}', '{\"amount\": 800, \"currency_type\": \"gold\"}', '召唤陨石，对所有敌人造成60点伤害。', 'player');
INSERT INTO `cards` VALUES (36, 'mass_heal', '群体治疗', 'spell', 'rare', 'none', 3, '{}', '{\"heal\": 30, \"target\": \"all_allies\"}', '{}', '{\"amount\": 800, \"currency_type\": \"gold\"}', '恢复所有友方30点生命值。', 'player');
INSERT INTO `cards` VALUES (37, 'ice_shield', '寒冰护盾', 'spell', 'rare', 'none', 2, '{}', '{\"armor\": 25, \"target\": \"ally\", \"duration\": 3, \"freeze_chance\": 0.3}', '{}', '{\"amount\": 800, \"currency_type\": \"gold\"}', '提供25点护甲，并有30%概率冰冻攻击者。', 'player');
INSERT INTO `cards` VALUES (38, 'dragon_breath', '龙息术', 'spell', 'epic', 'none', 5, '{}', '{\"aoe\": true, \"burn\": true, \"damage\": 80, \"target\": \"enemy\", \"burn_damage\": 10, \"burn_duration\": 3}', '{}', '{\"amount\": 2000, \"currency_type\": \"gold\"}', '释放龙息，对所有敌人造成80点伤害并附加燃烧效果。', 'player');
INSERT INTO `cards` VALUES (39, 'resurrection', '复活术', 'spell', 'epic', 'none', 6, '{}', '{\"heal\": 50, \"revive\": true, \"target\": \"ally\"}', '{}', '{\"amount\": 2000, \"currency_type\": \"gold\"}', '复活一名倒下的友方，并恢复50点生命值。', 'player');
INSERT INTO `cards` VALUES (40, 'apocalypse', '末日审判', 'spell', 'legendary', 'none', 8, '{}', '{\"aoe\": true, \"stun\": true, \"damage\": 150, \"target\": \"enemy\", \"stun_duration\": 2}', '{}', '{\"amount\": 5000, \"currency_type\": \"gold\"}', '释放末日审判，对所有敌人造成150点伤害并眩晕2回合。', 'player');
INSERT INTO `cards` VALUES (41, 'iron_sword', '铁剑', 'equipment', 'common', 'weapon', 0, '{\"attack\": 10}', '{}', '{}', '{\"amount\": 300, \"currency_type\": \"gold\"}', '基础武器，增加10点攻击力。', 'player');
INSERT INTO `cards` VALUES (42, 'steel_sword', '钢剑', 'equipment', 'rare', 'weapon', 0, '{\"attack\": 20, \"crit_chance\": 0.1}', '{\"crit_damage\": 1.5}', '{}', '{\"amount\": 1000, \"currency_type\": \"gold\"}', '精制武器，增加20点攻击力和10%暴击率。', 'player');
INSERT INTO `cards` VALUES (43, 'dragon_blade', '龙刃', 'equipment', 'epic', 'weapon', 0, '{\"attack\": 35, \"crit_chance\": 0.2}', '{\"burn_chance\": 0.3, \"crit_damage\": 2.0}', '{}', '{\"amount\": 2500, \"currency_type\": \"gold\"}', '传说中的武器，增加35点攻击力并有30%概率造成燃烧。', 'player');
INSERT INTO `cards` VALUES (44, 'excalibur', '王者之剑', 'equipment', 'legendary', 'weapon', 0, '{\"attack\": 50, \"crit_chance\": 0.3}', '{\"crit_damage\": 2.5, \"heal_on_kill\": 20}', '{}', '{\"amount\": 6000, \"currency_type\": \"gold\"}', '传说中的王者之剑，击杀敌人时恢复生命值。', 'player');
INSERT INTO `cards` VALUES (45, 'leather_armor', '皮甲', 'equipment', 'common', 'armor', 0, '{\"hp\": 20, \"defense\": 15}', '{}', '{}', '{\"amount\": 300, \"currency_type\": \"gold\"}', '基础护甲，增加15点防御和20点生命值。', 'player');
INSERT INTO `cards` VALUES (46, 'plate_armor', '板甲', 'equipment', 'rare', 'armor', 0, '{\"hp\": 40, \"defense\": 30}', '{\"damage_reduction\": 0.1}', '{}', '{\"amount\": 1000, \"currency_type\": \"gold\"}', '重型护甲，增加30点防御和40点生命值，减少10%伤害。', 'player');
INSERT INTO `cards` VALUES (47, 'dragon_scale', '龙鳞甲', 'equipment', 'epic', 'armor', 0, '{\"hp\": 60, \"defense\": 50}', '{\"fire_resistance\": 0.5, \"damage_reduction\": 0.2}', '{}', '{\"amount\": 2500, \"currency_type\": \"gold\"}', '龙鳞制成的护甲，拥有强大的防御力和火焰抗性。', 'player');
INSERT INTO `cards` VALUES (48, 'health_ring', '生命之戒', 'equipment', 'common', 'trinket', 0, '{\"hp\": 30}', '{\"regen\": 5}', '{}', '{\"amount\": 400, \"currency_type\": \"gold\"}', '每回合恢复5点生命值。', 'player');
INSERT INTO `cards` VALUES (49, 'power_amulet', '力量护符', 'equipment', 'rare', 'trinket', 0, '{\"attack\": 15}', '{\"attack_bonus_per_kill\": 2}', '{}', '{\"amount\": 1200, \"currency_type\": \"gold\"}', '每次击杀敌人增加2点攻击力。', 'player');
INSERT INTO `cards` VALUES (50, 'phoenix_feather', '凤凰之羽', 'equipment', 'epic', 'trinket', 0, '{\"hp\": 50}', '{\"revive_hp\": 50, \"revive_once\": true}', '{}', '{\"amount\": 3000, \"currency_type\": \"gold\"}', '拥有一次复活机会，复活时恢复50%生命值。', 'player');

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
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enemies
-- ----------------------------
INSERT INTO `enemies` VALUES (1, '暗影猎手', 'normal', '{\"hp\": 120, \"armor\": 5, \"attack\": 18}', '{\"pattern\": \"strike_poison\", \"priority\": \"low_hp\"}');
INSERT INTO `enemies` VALUES (2, '虚空之手', 'boss', '{\"hp\": 320, \"armor\": 10, \"attack\": 28}', '{\"pattern\": \"aoe_then_shield\", \"priority\": \"all\"}');
INSERT INTO `enemies` VALUES (3, '灰烬骑士', 'normal', '{\"hp\": 100, \"armor\": 8, \"attack\": 20}', '{\"pattern\": \"fire_attack\", \"priority\": \"front\"}');
INSERT INTO `enemies` VALUES (4, '幽影潜行者', 'normal', '{\"hp\": 90, \"armor\": 3, \"attack\": 22}', '{\"pattern\": \"backstab\", \"priority\": \"back_row\"}');
INSERT INTO `enemies` VALUES (5, '腐坏守卫', 'normal', '{\"hp\": 140, \"armor\": 12, \"attack\": 15}', '{\"pattern\": \"defend_then_attack\", \"priority\": \"high_dps\"}');
INSERT INTO `enemies` VALUES (6, '奥术学徒', 'normal', '{\"hp\": 85, \"armor\": 2, \"attack\": 25}', '{\"pattern\": \"arcane_bolt\", \"priority\": \"low_mana\"}');
INSERT INTO `enemies` VALUES (7, '血爪狼人', 'elite', '{\"hp\": 180, \"armor\": 7, \"attack\": 26}', '{\"pattern\": \"berserk_rage\", \"priority\": \"closest\"}');
INSERT INTO `enemies` VALUES (8, '寒冰巫妖', 'elite', '{\"hp\": 150, \"armor\": 4, \"attack\": 30}', '{\"pattern\": \"frost_nova\", \"priority\": \"most_allies\"}');
INSERT INTO `enemies` VALUES (9, '破城巨像', 'elite', '{\"hp\": 250, \"armor\": 18, \"attack\": 22}', '{\"pattern\": \"smash_aoe\", \"priority\": \"tank\"}');
INSERT INTO `enemies` VALUES (10, '深渊督军', 'boss', '{\"hp\": 450, \"armor\": 15, \"attack\": 35}', '{\"pattern\": \"cleave_then_heal\", \"priority\": \"low_armor\"}');
INSERT INTO `enemies` VALUES (11, '永恒巫后', 'boss', '{\"hp\": 380, \"armor\": 8, \"attack\": 40}', '{\"pattern\": \"curse_then_summon\", \"priority\": \"healer\"}');
INSERT INTO `enemies` VALUES (12, '熔岩巨兽', 'boss', '{\"hp\": 500, \"armor\": 20, \"attack\": 32}', '{\"pattern\": \"magma_eruption\", \"priority\": \"random\"}');

-- ----------------------------
-- Table structure for enemy_card_characters
-- ----------------------------
DROP TABLE IF EXISTS `enemy_card_characters`;
CREATE TABLE `enemy_card_characters`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enemy_id` bigint NOT NULL COMMENT '敌人ID',
  `card_character_id` bigint NOT NULL COMMENT '角色卡ID',
  `weight` int NOT NULL DEFAULT 1 COMMENT '权重（用于随机抽取）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_enemy_card_characters_enemy`(`enemy_id` ASC) USING BTREE,
  INDEX `idx_enemy_card_characters_character`(`card_character_id` ASC) USING BTREE,
  CONSTRAINT `fk_enemy_card_characters_character` FOREIGN KEY (`card_character_id`) REFERENCES `card_characters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_enemy_card_characters_enemy` FOREIGN KEY (`enemy_id`) REFERENCES `enemies` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '敌人角色卡关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enemy_card_characters
-- ----------------------------
INSERT INTO `enemy_card_characters` VALUES (1, 1, 15, 3);
INSERT INTO `enemy_card_characters` VALUES (2, 1, 17, 2);
INSERT INTO `enemy_card_characters` VALUES (3, 2, 24, 1);
INSERT INTO `enemy_card_characters` VALUES (4, 3, 20, 3);
INSERT INTO `enemy_card_characters` VALUES (5, 3, 21, 2);
INSERT INTO `enemy_card_characters` VALUES (6, 4, 17, 3);
INSERT INTO `enemy_card_characters` VALUES (7, 4, 16, 2);
INSERT INTO `enemy_card_characters` VALUES (8, 5, 15, 4);
INSERT INTO `enemy_card_characters` VALUES (9, 5, 18, 2);
INSERT INTO `enemy_card_characters` VALUES (10, 6, 19, 3);
INSERT INTO `enemy_card_characters` VALUES (11, 6, 21, 2);
INSERT INTO `enemy_card_characters` VALUES (12, 7, 22, 2);
INSERT INTO `enemy_card_characters` VALUES (13, 7, 18, 2);
INSERT INTO `enemy_card_characters` VALUES (14, 8, 23, 1);
INSERT INTO `enemy_card_characters` VALUES (15, 9, 24, 3);
INSERT INTO `enemy_card_characters` VALUES (16, 10, 24, 2);
INSERT INTO `enemy_card_characters` VALUES (17, 10, 23, 1);
INSERT INTO `enemy_card_characters` VALUES (18, 11, 23, 2);
INSERT INTO `enemy_card_characters` VALUES (19, 12, 24, 1);

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
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory
-- ----------------------------
INSERT INTO `inventory` VALUES (1, 1, 1, 3, 'unbound', '2025-11-18 10:23:15');
INSERT INTO `inventory` VALUES (2, 1, 2, 5, 'unbound', '2025-11-18 10:23:15');
INSERT INTO `inventory` VALUES (3, 3, 1, 5, 'unbound', '2025-11-21 11:47:51');
INSERT INTO `inventory` VALUES (15, 7, 81, 2, 'unbound', '2025-12-31 14:51:24');
INSERT INTO `inventory` VALUES (16, 7, 4, 1, 'unbound', '2025-12-31 14:50:29');
INSERT INTO `inventory` VALUES (17, 7, 49, 1, 'unbound', '2025-12-31 14:50:30');

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
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of player_characters
-- ----------------------------
INSERT INTO `player_characters` VALUES (1, 'warden', '守望者', 80, 12, '古老仪式守护者，擅长防御和支援');
INSERT INTO `player_characters` VALUES (9, 'occultist', '秘术师', 60, 9, '秘术师是那些敢于触碰禁忌知识的学者，他们深入研究被世人遗忘的暗影魔法和古老诅咒。这些危险的学者往往因为对知识的渴望而走上了一条不归路，他们操纵着黑暗力量，能够召唤亡灵、施放诅咒、控制敌人的心智。秘术师通常来自古老的魔法学院或秘密组织，他们为了追求力量不惜付出任何代价。虽然他们的身体脆弱，但他们的魔法力量足以让最强大的敌人感到恐惧。在暗黑地牢中，秘术师是强大的控场者和输出者，但需要队友的保护。');
INSERT INTO `player_characters` VALUES (10, 'ranger', '游侠', 70, 10, '游侠是森林和荒野中的猎手，他们精通远程武器和追踪技巧，能够在敌人发现之前就将其击败。这些敏捷的战士从小在野外长大，学会了如何利用环境优势、设置陷阱、追踪猎物。游侠通常独来独往，但也会为了共同的目标而加入冒险队伍。他们擅长使用弓箭、弩箭和飞刀，能够在远距离对敌人造成致命打击。在暗黑地牢中，游侠是优秀的侦察兵和远程输出者，他们的陷阱和追踪技能能够帮助队伍提前发现危险。');
INSERT INTO `player_characters` VALUES (11, 'warrior', '战士', 100, 15, '战士是勇猛的近战专家，他们拥有强大的物理攻击力和惊人的生命力。这些无畏的战士从小接受严格的战斗训练，精通各种近战武器和战斗技巧。战士通常来自军事学院或佣兵团，他们为了荣誉、财富或正义而战斗。他们身穿重甲，手持巨剑或战斧，能够在战场上冲锋陷阵，为队友开辟道路。战士的生命力极其顽强，即使受到重伤也能继续战斗。在暗黑地牢中，战士是队伍的前排核心，他们能够承受大量伤害，同时用强大的攻击力摧毁敌人。');

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
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_offers
-- ----------------------------
INSERT INTO `shop_offers` VALUES (1, 'card', 1, 180, 1, '{\"weight\": 30, \"limit_per_day\": 1}');
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
INSERT INTO `shop_offers` VALUES (59, 'item', 82, 100, 1, '{}');
INSERT INTO `shop_offers` VALUES (60, 'item', 83, 300, 2, '{}');
INSERT INTO `shop_offers` VALUES (61, 'item', 84, 1000, 3, '{}');
INSERT INTO `shop_offers` VALUES (62, 'item', 1, 150, 4, '{}');
INSERT INTO `shop_offers` VALUES (63, 'item', 2, 50, 5, '{}');
INSERT INTO `shop_offers` VALUES (64, 'item', 50, 50, 6, '{}');
INSERT INTO `shop_offers` VALUES (65, 'item', 81, 500, 7, '{}');
INSERT INTO `shop_offers` VALUES (66, 'item', 4, 200, 8, '{}');
INSERT INTO `shop_offers` VALUES (99, 'card_character', 14, 5000, 1, '{}');
INSERT INTO `shop_offers` VALUES (100, 'card_character', 13, 2000, 2, '{}');
INSERT INTO `shop_offers` VALUES (101, 'card_character', 12, 2000, 3, '{}');
INSERT INTO `shop_offers` VALUES (102, 'card_character', 11, 800, 4, '{}');
INSERT INTO `shop_offers` VALUES (103, 'card_character', 3, 500, 5, '{}');
INSERT INTO `shop_offers` VALUES (104, 'card_character', 2, 160, 6, '{}');
INSERT INTO `shop_offers` VALUES (105, 'card_character', 8, 300, 7, '{}');
INSERT INTO `shop_offers` VALUES (106, 'card_character', 6, 300, 8, '{}');

-- ----------------------------
-- Table structure for skills
-- ----------------------------
DROP TABLE IF EXISTS `skills`;
CREATE TABLE `skills`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `player_character_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `effect_payload` json NOT NULL,
  `required_level` int NOT NULL,
  `position_in_tree` json NOT NULL,
  `unlock_path` json NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 83 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of skills
-- ----------------------------
INSERT INTO `skills` VALUES (35, 'warden_sacred_shield', 'warden', '神圣护盾', '守望者召唤神圣能量形成护盾，为自己或队友提供持续3回合的护甲加成。护盾能够吸收物理伤害，并在被击破时对攻击者造成神圣反伤。这是守望者最基础的防御技能，体现了他们守护他人的职责。', '{\"type\": \"buff\", \"target\": \"ally\", \"duration\": 3, \"action_cost\": 2, \"armor_bonus\": 15, \"reflect_damage\": 5}', 1, '{\"row\": 1, \"column\": 1}', '[]');
INSERT INTO `skills` VALUES (36, 'warden_healing_light', 'warden', '治疗之光', '守望者引导神圣能量，对单个友军单位恢复生命值。这个技能不仅能够治愈伤口，还能驱散轻微的负面状态。治疗之光温暖而纯净，是守望者最常用的支援技能，体现了他们守护生命的使命。', '{\"type\": \"heal\", \"target\": \"ally\", \"action_cost\": 2, \"heal_amount\": 25, \"remove_debuff\": true}', 1, '{\"row\": 1, \"column\": 2}', '[]');
INSERT INTO `skills` VALUES (37, 'warden_iron_will', 'warden', '坚韧意志', '守望者通过坚定的意志力提升自己的防御能力，减少受到的伤害并提高对负面状态的抗性。这个被动技能让守望者能够在战斗中保持冷静，即使面对强大的敌人也不会退缩。', '{\"type\": \"passive\", \"damage_reduction\": 0.15, \"debuff_resistance\": 0.2}', 1, '{\"row\": 1, \"column\": 3}', '[]');
INSERT INTO `skills` VALUES (38, 'warden_guardian_wall', 'warden', '守护之墙', '守望者在前方召唤一道神圣能量墙，为所有友军提供护甲加成和伤害减免。这道墙能够持续5回合，是守望者最强大的群体防御技能。在墙的保护下，队友能够更安全地进行战斗。', '{\"type\": \"buff\", \"target\": \"all_allies\", \"duration\": 5, \"action_cost\": 3, \"armor_bonus\": 10, \"damage_reduction\": 0.1}', 3, '{\"row\": 2, \"column\": 1}', '[\"warden_sacred_shield\"]');
INSERT INTO `skills` VALUES (39, 'warden_group_heal', 'warden', '群体治疗', '守望者释放强大的治疗能量，同时恢复所有友军的生命值。这个技能消耗较大，但能够在关键时刻拯救整个队伍。治疗之光如同圣雨般洒向所有队友，驱散黑暗和伤痛。', '{\"type\": \"heal\", \"target\": \"all_allies\", \"action_cost\": 4, \"heal_amount\": 20, \"remove_debuff\": true}', 3, '{\"row\": 2, \"column\": 2}', '[\"warden_healing_light\"]');
INSERT INTO `skills` VALUES (40, 'warden_sacred_purification', 'warden', '神圣净化', '守望者释放神圣能量，驱散所有友军的负面状态并恢复少量生命值。这个技能对诅咒、中毒、虚弱等负面效果特别有效，是守望者对抗黑暗魔法的重要手段。', '{\"type\": \"cleanse\", \"target\": \"all_allies\", \"action_cost\": 3, \"heal_amount\": 15, \"remove_all_debuffs\": true}', 3, '{\"row\": 2, \"column\": 3}', '[\"warden_iron_will\"]');
INSERT INTO `skills` VALUES (41, 'warden_absolute_defense', 'warden', '绝对防御', '守望者进入绝对防御状态，在接下来的2回合内免疫所有伤害，但无法进行攻击。这是守望者最强大的保命技能，能够在危急时刻保护自己或为队友争取时间。绝对防御状态下，守望者如同不可撼动的山岳。', '{\"type\": \"buff\", \"target\": \"self\", \"duration\": 2, \"action_cost\": 4, \"cannot_attack\": true, \"damage_immunity\": true}', 5, '{\"row\": 3, \"column\": 1}', '[\"warden_guardian_wall\"]');
INSERT INTO `skills` VALUES (42, 'warden_life_link', 'warden', '生命链接', '守望者与一个友军建立生命链接，在接下来的5回合内，守望者受到的伤害会部分转移到自己身上，同时链接的友军会获得持续的生命恢复。这个技能体现了守望者牺牲自己保护他人的精神。', '{\"type\": \"link\", \"target\": \"ally\", \"duration\": 5, \"action_cost\": 3, \"damage_share\": 0.4, \"heal_per_turn\": 10}', 5, '{\"row\": 3, \"column\": 2}', '[\"warden_group_heal\", \"warden_sacred_purification\"]');
INSERT INTO `skills` VALUES (43, 'occultist_shadow_bolt', 'occultist', '暗影箭', '秘术师凝聚暗影能量，向敌人发射一支暗影箭，造成暗影属性伤害。暗影箭能够穿透部分护甲，对敌人造成持续3回合的暗影侵蚀效果，每回合造成额外伤害。这是秘术师最基础的攻击技能。', '{\"type\": \"attack\", \"damage\": 18, \"target\": \"enemy\", \"dot_damage\": 5, \"action_cost\": 2, \"damage_type\": \"shadow\", \"dot_duration\": 3, \"armor_penetration\": 0.2}', 1, '{\"row\": 1, \"column\": 1}', '[]');
INSERT INTO `skills` VALUES (44, 'occultist_weakness_curse', 'occultist', '虚弱诅咒', '秘术师对敌人施放虚弱诅咒，降低敌人的攻击力和防御力，持续4回合。被诅咒的敌人会感到力量流失，行动变得迟缓。这个技能是秘术师控制战场的重要手段。', '{\"type\": \"debuff\", \"target\": \"enemy\", \"duration\": 4, \"action_cost\": 2, \"attack_reduction\": 0.25, \"defense_reduction\": 0.2}', 1, '{\"row\": 1, \"column\": 2}', '[]');
INSERT INTO `skills` VALUES (45, 'occultist_shadow_armor', 'occultist', '暗影护体', '秘术师用暗影能量包裹自己，提高魔法抗性并减少受到的物理伤害。在暗影护体的保护下，秘术师能够更安全地施放魔法。这个被动技能让脆弱的秘术师有了生存保障。', '{\"type\": \"passive\", \"magic_resistance\": 0.3, \"physical_damage_reduction\": 0.15}', 1, '{\"row\": 1, \"column\": 3}', '[]');
INSERT INTO `skills` VALUES (46, 'occultist_death_coil', 'occultist', '死亡缠绕', '秘术师召唤死亡能量缠绕敌人，造成大量暗影伤害并吸取敌人的生命值来恢复自己。被死亡缠绕的敌人会感到生命力快速流失，而秘术师则从中获得力量。这是秘术师最强大的单体攻击技能。', '{\"type\": \"attack\", \"damage\": 35, \"target\": \"enemy\", \"lifesteal\": 0.4, \"action_cost\": 3, \"damage_type\": \"shadow\"}', 3, '{\"row\": 2, \"column\": 1}', '[\"occultist_shadow_bolt\"]');
INSERT INTO `skills` VALUES (47, 'occultist_group_curse', 'occultist', '群体诅咒', '秘术师释放强大的诅咒能量，对所有敌人施放虚弱诅咒和暗影侵蚀效果。这个技能能够同时削弱多个敌人，是秘术师最强大的控场技能。诅咒的能量如同瘟疫般在敌人之间蔓延。', '{\"type\": \"debuff\", \"target\": \"all_enemies\", \"duration\": 4, \"dot_damage\": 3, \"action_cost\": 4, \"dot_duration\": 4, \"attack_reduction\": 0.2, \"defense_reduction\": 0.15}', 3, '{\"row\": 2, \"column\": 2}', '[\"occultist_weakness_curse\"]');
INSERT INTO `skills` VALUES (48, 'occultist_shadow_burst', 'occultist', '暗影爆发', '秘术师引爆周围的暗影能量，对所有敌人造成暗影伤害。这个技能的范围伤害能力极强，但消耗也很大。暗影爆发时，整个战场都会被黑暗能量笼罩，敌人无处可逃。', '{\"type\": \"attack\", \"damage\": 22, \"target\": \"all_enemies\", \"action_cost\": 4, \"damage_type\": \"shadow\"}', 3, '{\"row\": 2, \"column\": 3}', '[\"occultist_shadow_armor\"]');
INSERT INTO `skills` VALUES (49, 'occultist_soul_harvest', 'occultist', '灵魂收割', '秘术师对生命值低于30%的敌人进行灵魂收割，造成巨额暗影伤害。如果成功击杀敌人，秘术师会恢复大量生命值并获得额外的行动点。这个技能让秘术师在敌人虚弱时能够快速终结战斗。', '{\"type\": \"execute\", \"damage\": 60, \"target\": \"enemy\", \"action_cost\": 3, \"damage_type\": \"shadow\", \"heal_on_kill\": 50, \"execute_threshold\": 0.3, \"bonus_action_points\": 1}', 5, '{\"row\": 3, \"column\": 1}', '[\"occultist_death_coil\"]');
INSERT INTO `skills` VALUES (50, 'occultist_dark_pact', 'occultist', '黑暗契约', '秘术师与黑暗力量签订契约，在接下来的3回合内大幅提升自己的魔法伤害，但每回合会损失少量生命值。这个技能体现了秘术师为了力量不惜代价的决心。契约期间，秘术师的魔法会变得更加致命。', '{\"type\": \"buff\", \"target\": \"self\", \"duration\": 3, \"action_cost\": 3, \"hp_cost_per_turn\": 8, \"magic_damage_bonus\": 0.5}', 5, '{\"row\": 3, \"column\": 2}', '[\"occultist_group_curse\", \"occultist_shadow_burst\"]');
INSERT INTO `skills` VALUES (51, 'ranger_precise_shot', 'ranger', '精准射击', '游侠瞄准敌人的弱点，进行精准射击，造成物理伤害并有较高概率造成暴击。精准射击是游侠最基础的攻击技能，体现了他们精湛的箭术。这一箭往往能够命中敌人的要害，造成致命伤害。', '{\"type\": \"attack\", \"damage\": 20, \"target\": \"enemy\", \"action_cost\": 2, \"crit_chance\": 0.35, \"damage_type\": \"physical\", \"crit_multiplier\": 1.8}', 1, '{\"row\": 1, \"column\": 1}', '[]');
INSERT INTO `skills` VALUES (52, 'ranger_tracking_mark', 'ranger', '追踪标记', '游侠对敌人施加追踪标记，在接下来的5回合内，被标记的敌人会受到额外伤害，并且游侠对其攻击的命中率和暴击率都会提高。追踪标记让游侠能够锁定目标，持续输出伤害。', '{\"type\": \"debuff\", \"target\": \"enemy\", \"duration\": 5, \"action_cost\": 2, \"hit_rate_bonus\": 0.2, \"crit_chance_bonus\": 0.15, \"damage_taken_increase\": 0.15}', 1, '{\"row\": 1, \"column\": 2}', '[]');
INSERT INTO `skills` VALUES (53, 'ranger_agile_steps', 'ranger', '敏捷步伐', '游侠通过敏捷的步伐提高自己的闪避率和移动速度。这个被动技能让游侠能够在战斗中灵活移动，躲避敌人的攻击。游侠的敏捷是他们生存的关键。', '{\"type\": \"passive\", \"dodge_chance\": 0.25, \"movement_speed_bonus\": 0.3}', 1, '{\"row\": 1, \"column\": 3}', '[]');
INSERT INTO `skills` VALUES (54, 'ranger_multi_shot', 'ranger', '多重射击', '游侠同时射出多支箭矢，对多个敌人造成物理伤害。每支箭矢的伤害略低于精准射击，但总伤害输出更高。多重射击是游侠应对多个敌人的有效手段，箭雨覆盖整个战场。', '{\"type\": \"attack\", \"damage\": 15, \"target\": \"all_enemies\", \"action_cost\": 3, \"damage_type\": \"physical\"}', 3, '{\"row\": 2, \"column\": 1}', '[\"ranger_precise_shot\"]');
INSERT INTO `skills` VALUES (55, 'ranger_trap_setting', 'ranger', '陷阱布置', '游侠在战场上布置陷阱，当敌人触发陷阱时会受到物理伤害并被减速。陷阱可以持续3回合，最多同时存在2个。这个技能让游侠能够控制战场，限制敌人的行动。', '{\"type\": \"trap\", \"damage\": 18, \"max_traps\": 2, \"action_cost\": 2, \"damage_type\": \"physical\", \"slow_duration\": 2, \"trap_duration\": 3}', 3, '{\"row\": 2, \"column\": 2}', '[\"ranger_tracking_mark\"]');
INSERT INTO `skills` VALUES (56, 'ranger_rapid_fire', 'ranger', '疾风连射', '游侠以极快的速度连续射击，对单个敌人造成多次物理伤害。每次射击的伤害较低，但总伤害很高。疾风连射是游侠最强大的单体输出技能，箭矢如雨点般落在敌人身上。', '{\"hits\": 4, \"type\": \"attack\", \"target\": \"enemy\", \"action_cost\": 3, \"damage_type\": \"physical\", \"damage_per_hit\": 8}', 3, '{\"row\": 2, \"column\": 3}', '[\"ranger_agile_steps\"]');
INSERT INTO `skills` VALUES (57, 'ranger_killing_blow', 'ranger', '致命一击', '游侠瞄准敌人的致命弱点，进行致命一击。如果敌人生命值低于50%，伤害会大幅提高。这个技能让游侠能够在敌人虚弱时快速终结战斗，一箭封喉。', '{\"type\": \"attack\", \"damage\": 45, \"target\": \"enemy\", \"action_cost\": 3, \"damage_type\": \"physical\", \"execute_bonus\": 0.6, \"execute_threshold\": 0.5}', 5, '{\"row\": 3, \"column\": 1}', '[\"ranger_multi_shot\"]');
INSERT INTO `skills` VALUES (58, 'ranger_hunter_instinct', 'ranger', '猎手本能', '游侠激活猎手本能，在接下来的4回合内大幅提高攻击速度、暴击率和移动速度。这个技能让游侠进入最佳战斗状态，如同真正的猎手般敏锐和致命。', '{\"type\": \"buff\", \"target\": \"self\", \"duration\": 4, \"action_cost\": 3, \"crit_chance_bonus\": 0.3, \"attack_speed_bonus\": 0.5, \"movement_speed_bonus\": 0.4}', 5, '{\"row\": 3, \"column\": 2}', '[\"ranger_trap_setting\", \"ranger_rapid_fire\"]');
INSERT INTO `skills` VALUES (59, 'warrior_heavy_strike', 'warrior', '重击', '战士用尽全力进行重击，对敌人造成大量物理伤害。重击有较高概率击退敌人并造成短暂眩晕。这是战士最基础的攻击技能，体现了他们强大的力量。重击时，战士的武器会发出破空之声，敌人往往无法抵挡。', '{\"type\": \"attack\", \"damage\": 28, \"target\": \"enemy\", \"action_cost\": 2, \"damage_type\": \"physical\", \"stun_duration\": 1, \"knockback_chance\": 0.4}', 1, '{\"row\": 1, \"column\": 1}', '[]');
INSERT INTO `skills` VALUES (60, 'warrior_battle_cry', 'warrior', '战吼', '战士发出震撼战场的战吼，提升所有友军的攻击力和士气，持续4回合。战吼不仅能够激励队友，还能震慑敌人，降低敌人的攻击力。这是战士最强大的支援技能，体现了他们的领导力。', '{\"type\": \"buff\", \"target\": \"all_allies\", \"duration\": 4, \"action_cost\": 2, \"attack_bonus\": 0.15, \"morale_bonus\": 0.1, \"enemy_attack_reduction\": 0.1}', 1, '{\"row\": 1, \"column\": 2}', '[]');
INSERT INTO `skills` VALUES (61, 'warrior_robust_body', 'warrior', '坚韧体魄', '战士通过长期的训练拥有坚韧的体魄，大幅提高生命值上限和物理防御力。这个被动技能让战士能够承受更多伤害，是他们在战场上屹立不倒的基础。', '{\"type\": \"passive\", \"hp_bonus\": 0.2, \"physical_defense_bonus\": 0.25}', 1, '{\"row\": 1, \"column\": 3}', '[]');
INSERT INTO `skills` VALUES (62, 'warrior_whirlwind', 'warrior', '旋风斩', '战士挥舞武器进行旋转攻击，对所有敌人造成物理伤害。旋风斩是战士最强大的群体攻击技能，战士如同旋风般在敌人中穿梭，所到之处敌人纷纷倒下。', '{\"type\": \"attack\", \"damage\": 20, \"target\": \"all_enemies\", \"action_cost\": 3, \"damage_type\": \"physical\"}', 3, '{\"row\": 2, \"column\": 1}', '[\"warrior_heavy_strike\"]');
INSERT INTO `skills` VALUES (63, 'warrior_taunt', 'warrior', '嘲讽', '战士通过嘲讽吸引所有敌人的注意力，强制敌人攻击自己，持续3回合。嘲讽期间，战士的防御力会提高。这个技能让战士能够保护脆弱的队友，是坦克型战士的核心技能。', '{\"type\": \"taunt\", \"target\": \"all_enemies\", \"duration\": 3, \"action_cost\": 2, \"defense_bonus\": 0.3}', 3, '{\"row\": 2, \"column\": 2}', '[\"warrior_battle_cry\"]');
INSERT INTO `skills` VALUES (64, 'warrior_berserker_rage', 'warrior', '狂暴', '战士进入狂暴状态，在接下来的4回合内大幅提高攻击力和攻击速度，但防御力会降低。狂暴状态下，战士会不顾一切地攻击敌人，直到战斗结束。这是战士最强大的输出技能。', '{\"type\": \"buff\", \"target\": \"self\", \"duration\": 4, \"action_cost\": 3, \"attack_bonus\": 0.4, \"defense_penalty\": 0.2, \"attack_speed_bonus\": 0.35}', 3, '{\"row\": 2, \"column\": 3}', '[\"warrior_robust_body\"]');
INSERT INTO `skills` VALUES (65, 'warrior_ultimate_strike', 'warrior', '终极打击', '战士凝聚所有力量进行终极打击，对单个敌人造成巨额物理伤害。如果敌人生命值低于40%，伤害会进一步提高。终极打击是战士最强大的单体攻击技能，往往能够一击必杀。', '{\"type\": \"attack\", \"damage\": 70, \"target\": \"enemy\", \"action_cost\": 4, \"damage_type\": \"physical\", \"execute_bonus\": 0.5, \"execute_threshold\": 0.4}', 5, '{\"row\": 3, \"column\": 1}', '[\"warrior_whirlwind\"]');
INSERT INTO `skills` VALUES (66, 'warrior_unyielding_will', 'warrior', '不屈意志', '战士激活不屈意志，在生命值低于30%时自动触发，大幅提高防御力、生命恢复速度和所有负面状态抗性，持续5回合。这个技能让战士在绝境中能够继续战斗，体现了他们永不放弃的精神。', '{\"type\": \"passive_trigger\", \"duration\": 5, \"action_cost\": 0, \"defense_bonus\": 0.5, \"debuff_resistance\": 0.4, \"hp_regen_per_turn\": 15, \"trigger_hp_threshold\": 0.3}', 5, '{\"row\": 3, \"column\": 2}', '[\"warrior_taunt\", \"warrior_berserker_rage\"]');
INSERT INTO `skills` VALUES (67, 'warden_sacred_power', 'warden', '神圣力量', '守望者通过神圣力量的加持，永久提升自己的攻击力。神圣力量让守望者的每一次攻击都带有神圣属性，对邪恶敌人造成额外伤害。', '{\"type\": \"passive\", \"attack_bonus\": 0.15, \"holy_damage_bonus\": 0.1}', 3, '{\"row\": 2, \"column\": 4}', '[\"warden_sacred_shield\"]');
INSERT INTO `skills` VALUES (68, 'warden_mana_spring', 'warden', '法力源泉', '守望者与神圣能量建立深层连接，永久提升自己的法力值上限。法力源泉让守望者能够更频繁地使用技能，在战斗中持续提供支援。', '{\"type\": \"passive\", \"max_mana_bonus\": 0.2}', 3, '{\"row\": 2, \"column\": 5}', '[\"warden_healing_light\"]');
INSERT INTO `skills` VALUES (69, 'warden_divine_judgment', 'warden', '神圣审判', '守望者召唤神圣审判之光，对单个敌人造成大量神圣伤害。对邪恶和亡灵类敌人造成双倍伤害。神圣审判是守望者最强大的攻击技能，体现了正义的力量。', '{\"type\": \"attack\", \"damage\": 40, \"target\": \"enemy\", \"action_cost\": 3, \"damage_type\": \"holy\", \"evil_damage_multiplier\": 2.0}', 5, '{\"row\": 3, \"column\": 3}', '[\"warden_sacred_purification\"]');
INSERT INTO `skills` VALUES (70, 'warden_guardian_will', 'warden', '守护者意志', '守望者通过坚定的守护意志，永久提升自己的生命值上限和物理防御力。守护者意志让守望者能够承受更多伤害，更好地保护队友。', '{\"type\": \"passive\", \"hp_bonus\": 0.25, \"physical_defense_bonus\": 0.3}', 5, '{\"row\": 3, \"column\": 4}', '[\"warden_absolute_defense\"]');
INSERT INTO `skills` VALUES (71, 'occultist_shadow_power', 'occultist', '暗影之力', '秘术师通过掌握暗影力量，永久提升自己的魔法攻击力。暗影之力让秘术师的魔法攻击更加致命，能够穿透敌人的魔法抗性。', '{\"type\": \"passive\", \"attack_bonus\": 0.2, \"magic_penetration\": 0.15}', 3, '{\"row\": 2, \"column\": 4}', '[\"occultist_shadow_bolt\"]');
INSERT INTO `skills` VALUES (72, 'occultist_shadow_well', 'occultist', '暗影源泉', '秘术师与暗影维度建立连接，永久提升自己的法力值上限。暗影源泉让秘术师能够更频繁地施放强大的暗影魔法，持续输出伤害。', '{\"type\": \"passive\", \"max_mana_bonus\": 0.25}', 3, '{\"row\": 2, \"column\": 5}', '[\"occultist_weakness_curse\"]');
INSERT INTO `skills` VALUES (73, 'occultist_shadow_storm', 'occultist', '暗影风暴', '秘术师召唤暗影风暴，对所有敌人造成持续3回合的暗影伤害。暗影风暴会降低敌人的魔法抗性，让后续的魔法攻击更加有效。', '{\"type\": \"attack\", \"damage\": 18, \"target\": \"all_enemies\", \"dot_damage\": 8, \"action_cost\": 4, \"damage_type\": \"shadow\", \"dot_duration\": 3, \"magic_resistance_reduction\": 0.2}', 5, '{\"row\": 3, \"column\": 3}', '[\"occultist_shadow_burst\"]');
INSERT INTO `skills` VALUES (74, 'occultist_shadow_constitution', 'occultist', '暗影体质', '秘术师通过暗影能量的改造，永久提升自己的生命值上限，并在战斗中持续恢复法力值。暗影体质让脆弱的秘术师有了更强的生存能力。', '{\"type\": \"passive\", \"hp_bonus\": 0.15, \"mana_regen_per_turn\": 5}', 5, '{\"row\": 3, \"column\": 4}', '[\"occultist_dark_pact\"]');
INSERT INTO `skills` VALUES (75, 'ranger_precision_power', 'ranger', '精准之力', '游侠通过长期的训练，永久提升自己的攻击力和暴击率。精准之力让游侠的每一箭都更加致命，能够精准命中敌人的弱点。', '{\"type\": \"passive\", \"attack_bonus\": 0.18, \"crit_chance_bonus\": 0.1}', 3, '{\"row\": 2, \"column\": 4}', '[\"ranger_precise_shot\"]');
INSERT INTO `skills` VALUES (76, 'ranger_nature_well', 'ranger', '自然源泉', '游侠与自然力量建立连接，永久提升自己的法力值上限。自然源泉让游侠能够更频繁地使用追踪和陷阱技能，更好地控制战场。', '{\"type\": \"passive\", \"max_mana_bonus\": 0.2}', 3, '{\"row\": 2, \"column\": 5}', '[\"ranger_tracking_mark\"]');
INSERT INTO `skills` VALUES (77, 'ranger_arrow_storm', 'ranger', '箭雨风暴', '游侠向天空射出大量箭矢，形成箭雨覆盖整个战场，对所有敌人造成物理伤害。箭雨风暴是游侠最强大的群体攻击技能，箭矢如暴雨般倾泻而下。', '{\"hits\": 3, \"type\": \"attack\", \"damage\": 25, \"target\": \"all_enemies\", \"action_cost\": 4, \"damage_type\": \"physical\"}', 5, '{\"row\": 3, \"column\": 3}', '[\"ranger_multi_shot\"]');
INSERT INTO `skills` VALUES (78, 'ranger_hunter_physique', 'ranger', '猎手体魄', '游侠通过长期的野外生存训练，永久提升自己的生命值上限和攻击速度。猎手体魄让游侠在战斗中更加灵活，能够快速输出伤害。', '{\"type\": \"passive\", \"hp_bonus\": 0.2, \"attack_speed_bonus\": 0.15}', 5, '{\"row\": 3, \"column\": 4}', '[\"ranger_hunter_instinct\"]');
INSERT INTO `skills` VALUES (79, 'warrior_berserker_power', 'warrior', '狂暴之力', '战士通过激发内在的狂暴力量，永久提升自己的攻击力。狂暴之力让战士的每一次攻击都更加凶猛，能够轻易撕裂敌人的防御。', '{\"type\": \"passive\", \"attack_bonus\": 0.25, \"armor_penetration\": 0.2}', 3, '{\"row\": 2, \"column\": 4}', '[\"warrior_heavy_strike\"]');
INSERT INTO `skills` VALUES (80, 'warrior_battle_will', 'warrior', '战斗意志', '战士通过坚定的战斗意志，永久提升自己的法力值上限。战斗意志让战士能够更频繁地使用战吼和嘲讽等技能，更好地保护队友。', '{\"type\": \"passive\", \"max_mana_bonus\": 0.15}', 3, '{\"row\": 2, \"column\": 5}', '[\"warrior_battle_cry\"]');
INSERT INTO `skills` VALUES (81, 'warrior_earthquake', 'warrior', '大地震击', '战士用尽全力重击地面，引发地震冲击波，对所有敌人造成物理伤害并造成短暂眩晕。大地震击是战士最强大的群体控制技能，能够瞬间改变战场局势。', '{\"type\": \"attack\", \"damage\": 30, \"target\": \"all_enemies\", \"action_cost\": 4, \"damage_type\": \"physical\", \"stun_duration\": 1}', 5, '{\"row\": 3, \"column\": 3}', '[\"warrior_whirlwind\"]');
INSERT INTO `skills` VALUES (82, 'warrior_iron_will', 'warrior', '钢铁意志', '战士通过钢铁般的意志，永久提升自己的生命值上限和攻击力。钢铁意志让战士在战斗中更加坚韧，能够承受更多伤害并输出更高伤害。', '{\"type\": \"passive\", \"hp_bonus\": 0.3, \"attack_bonus\": 0.15}', 5, '{\"row\": 3, \"column\": 4}', '[\"warrior_ultimate_strike\"]');

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
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_card_characters
-- ----------------------------
INSERT INTO `user_card_characters` VALUES (1, 3, 1, 2, 40, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (2, 1, 2, 1, 55, 0, 0, 0, 1);
INSERT INTO `user_card_characters` VALUES (3, 3, 2, 1, 45, 10, 1, 2, 3);
INSERT INTO `user_card_characters` VALUES (7, 6, 2, 2, 55, 0, 1, 0, 2);
INSERT INTO `user_card_characters` VALUES (8, 6, 1, 0, 40, 0, 0, 0, 3);
INSERT INTO `user_card_characters` VALUES (9, 6, 3, 1, 35, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (11, 7, 2, 1, 70, 0, 1, 0, 2);
INSERT INTO `user_card_characters` VALUES (12, 7, 2, 2, 55, 0, 0, 0, 1);
INSERT INTO `user_card_characters` VALUES (14, 6, 6, 2, 1000, 0, 1, 0, 2);
INSERT INTO `user_card_characters` VALUES (15, 6, 6, 1, 70, 0, 0, 0, 1);
INSERT INTO `user_card_characters` VALUES (17, 7, 14, 1, 150, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (18, 7, 13, 2, 90, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (19, 7, 3, 1, 35, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (20, 7, 1, 2, 40, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (21, 6, 2, 1, 55, 0, 0, 0, 1);
INSERT INTO `user_card_characters` VALUES (22, 6, 3, 1, 43, 0, 0, 0, 2);
INSERT INTO `user_card_characters` VALUES (23, 8, 6, 1, 70, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (25, 8, 2, 1, 70, 0, 1, 0, 2);
INSERT INTO `user_card_characters` VALUES (26, 6, 13, 1, 90, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (27, 6, 14, 1, 150, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (29, 13, 8, 1, 60, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (30, 13, 2, 1, 70, 0, 1, 0, 2);
INSERT INTO `user_card_characters` VALUES (31, 14, 4, 1, 30, 0, 1, 0, 1);
INSERT INTO `user_card_characters` VALUES (33, 14, 14, 1, 150, 0, 0, 0, 1);
INSERT INTO `user_card_characters` VALUES (34, 14, 2, 1, 70, 0, 1, 0, 2);
INSERT INTO `user_card_characters` VALUES (35, 7, 10, 1, 65, 0, 0, 0, 1);

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
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_cards
-- ----------------------------
INSERT INTO `user_cards` VALUES (1, 1, 1, 2, 1, NULL, NULL, '2025-11-18 10:23:15', 'camp_shop', NULL);
INSERT INTO `user_cards` VALUES (2, 3, 3, 1, 1, NULL, NULL, '2025-11-18 10:23:15', 'quest_reward', NULL);
INSERT INTO `user_cards` VALUES (4, 6, 1, 2, 1, 1, NULL, '2025-12-04 09:15:59', 'shop', NULL);
INSERT INTO `user_cards` VALUES (5, 6, 4, 1, 1, 1, NULL, '2025-12-04 09:16:01', 'shop', NULL);
INSERT INTO `user_cards` VALUES (6, 6, 2, 3, 1, 1, NULL, '2025-12-05 10:53:10', 'shop', NULL);
INSERT INTO `user_cards` VALUES (7, 7, 4, 1, 1, 1, NULL, '2025-12-11 11:02:31', 'shop', NULL);
INSERT INTO `user_cards` VALUES (8, 7, 2, 1, 1, 1, NULL, '2025-12-11 11:02:32', 'shop', NULL);
INSERT INTO `user_cards` VALUES (9, 13, 2, 1, 1, 1, NULL, '2025-12-11 11:51:46', 'shop', NULL);
INSERT INTO `user_cards` VALUES (10, 13, 4, 2, 1, 1, NULL, '2025-12-11 11:51:53', 'shop', NULL);
INSERT INTO `user_cards` VALUES (11, 14, 4, 1, 1, 1, NULL, '2025-12-15 21:39:21', 'shop', NULL);
INSERT INTO `user_cards` VALUES (12, 14, 2, 2, 1, 1, NULL, '2025-12-15 21:39:24', 'shop', NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户职业技能解锁记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_player_character_skills
-- ----------------------------
INSERT INTO `user_player_character_skills` VALUES (6, 7, 1, 35, '2026-01-04 09:15:13');

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
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_player_characters
-- ----------------------------
INSERT INTO `user_player_characters` VALUES (1, 1, 1, 128, 120, 8, 8, 15, 1, '[]');
INSERT INTO `user_player_characters` VALUES (2, 3, 1, 78, 60, 6, 6, 30, 2, '[]');
INSERT INTO `user_player_characters` VALUES (6, 6, 1, 80, 40, 4, 4, 100, 4, '[\"精神崩溃\"]');
INSERT INTO `user_player_characters` VALUES (8, 7, 1, 80, 40, 4, 4, 80, 3, '[]');
INSERT INTO `user_player_characters` VALUES (9, 8, 1, 80, 80, 4, 4, 0, 1, '[]');
INSERT INTO `user_player_characters` VALUES (13, 12, 1, 80, 80, 4, 4, 0, 1, '[]');
INSERT INTO `user_player_characters` VALUES (14, 13, 1, 80, 40, 4, 4, 50, 2, '[\"惊惧低语\", \"注意力分散\"]');
INSERT INTO `user_player_characters` VALUES (15, 14, 1, 80, 40, 4, 4, 100, 4, '[]');

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
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_stage_progress
-- ----------------------------
INSERT INTO `user_stage_progress` VALUES (1, 1, 1, 1, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `user_stage_progress` VALUES (2, 3, 1, 1, 1, 1, 'victory', '2025-11-25 14:54:20', 2, NULL);
INSERT INTO `user_stage_progress` VALUES (5, 3, 2, 1, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `user_stage_progress` VALUES (6, 6, 1, 1, 1, 1, 'victory', '2025-12-05 09:03:33', 1, NULL);
INSERT INTO `user_stage_progress` VALUES (7, 6, 2, 1, 1, 1, 'victory', '2025-12-05 09:19:35', 1, NULL);
INSERT INTO `user_stage_progress` VALUES (8, 6, 3, 1, 1, 1, 'victory', '2025-12-05 11:09:47', 1, NULL);
INSERT INTO `user_stage_progress` VALUES (9, 6, 4, 1, 1, 1, 'victory', '2025-12-08 15:23:10', 2, NULL);
INSERT INTO `user_stage_progress` VALUES (10, 6, 5, 1, 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `user_stage_progress` VALUES (11, 7, 1, 1, 1, 1, 'victory', '2025-12-09 11:13:27', 1, NULL);
INSERT INTO `user_stage_progress` VALUES (12, 7, 2, 1, 1, 1, 'victory', '2025-12-09 11:16:11', 1, NULL);
INSERT INTO `user_stage_progress` VALUES (13, 7, 3, 1, 1, 1, 'victory', '2025-12-09 11:42:44', 1, NULL);
INSERT INTO `user_stage_progress` VALUES (14, 7, 4, 1, 1, 1, 'victory', '2025-12-09 15:20:51', 1, NULL);
INSERT INTO `user_stage_progress` VALUES (15, 7, 5, 1, 0, 1, NULL, NULL, 0, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_wallets
-- ----------------------------
INSERT INTO `user_wallets` VALUES (1, 1, 'gold', 5000, '2025-11-18 10:23:15');
INSERT INTO `user_wallets` VALUES (2, 1, 'soulstone', 100, '2025-11-18 10:23:15');
INSERT INTO `user_wallets` VALUES (3, 3, 'gold', 50, '2025-11-21 11:45:44');
INSERT INTO `user_wallets` VALUES (4, 6, 'gold', 84579, '2025-12-11 10:15:21');
INSERT INTO `user_wallets` VALUES (5, 7, 'gold', 56319, '2025-12-31 14:51:24');
INSERT INTO `user_wallets` VALUES (6, 8, 'gold', 220, '2025-12-11 10:03:50');
INSERT INTO `user_wallets` VALUES (10, 12, 'gold', 1000, '2025-12-11 10:41:00');
INSERT INTO `user_wallets` VALUES (11, 13, 'gold', 7420, '2025-12-11 11:53:01');
INSERT INTO `user_wallets` VALUES (12, 14, 'gold', 12720, '2025-12-15 21:39:59');
INSERT INTO `user_wallets` VALUES (13, 7, 'soulstone', 100, '2025-12-30 14:58:56');

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
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '测试玩家1', 'test1@example.com', '', 5, 4500, 'active', '2025-11-18 10:23:15');
INSERT INTO `users` VALUES (2, '测试玩家2', 'test2@example.com', NULL, 3, 2000, 'active', '2025-11-18 10:23:15');
INSERT INTO `users` VALUES (3, 'xiaosheng', '1982415487@qq.com', '$2a$10$AWEyl0VWvG9Fxpbl83/oQOrsjhhmMItCQ8/Gll/ub75rUR860eZ/C', 1, 0, 'active', '2025-11-18 15:16:39');
INSERT INTO `users` VALUES (6, '123', '3657267209@qq.com', '$2a$10$Y8RU3veRg7cq3LCORdGjF.rPN9sO/gX9Q3EZs.9gq6z.wUPYwIK4u', 1, 0, 'active', '2025-12-04 09:14:52');
INSERT INTO `users` VALUES (7, '内侧玩家', 'admin@example.com', '$2a$10$Ij.I0cUritCGVPXsDvHxnO2wwdjRUQdSc35MMZNM1m5vQdObXre9m', 1, 0, 'active', '2025-12-04 09:37:10');
INSERT INTO `users` VALUES (8, 'show', '3045771329@qq.com', '$2a$10$pXMIU/Ug2sfiw2h.d3S4G.VY5vmo9Cxiv1w/S9ApiO4l792vUiiOa', 1, 0, 'active', '2025-12-10 14:33:45');
INSERT INTO `users` VALUES (12, 'last test', '3838229363@qq.com', '$2a$10$CVQ.sEJT24eyhUEd6Ui8b.oqMSnKGHJ/NdAzmv6JV7kS0Xnk24AW.', 1, 0, 'active', '2025-12-11 10:40:59');
INSERT INTO `users` VALUES (13, 'test', '18833112536@163.com', '$2a$10$pAPao0FE8xnzHEQK.Xb9Dujn/ntZvbfiVpsofqeTYVDttXA.q4YS2', 1, 0, 'active', '2025-12-11 11:50:01');
INSERT INTO `users` VALUES (14, 'test3', '3892738457@qq.com', '$2a$10$neb16ayKTaCuX2/8PUYDsOv8CFDg5rFPrV7ana6mDRHSuDn1AO29a', 1, 0, 'active', '2025-12-15 21:38:33');

SET FOREIGN_KEY_CHECKS = 1;
