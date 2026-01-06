/*
 * 技能表法力值消耗更新脚本
 * 
 * 功能：将所有主动技能的 effect_payload 中的 action_cost 改为 mana_cost
 * 
 * 说明：
 * 1. 只更新主动技能（type 不是 "passive" 或 "passive_trigger" 的技能）
 * 2. 被动技能不需要消耗法力值，保持不变
 * 3. 使用 MySQL 的 JSON 函数来更新 JSON 字段
 * 
 * 执行前请备份数据库！
 */

-- ============================================
-- 第一步：查看需要更新的技能数量
-- ============================================
SELECT 
    COUNT(*) as total_active_skills,
    COUNT(CASE WHEN JSON_EXTRACT(effect_payload, '$.action_cost') IS NOT NULL THEN 1 END) as skills_with_action_cost
FROM skills
WHERE JSON_EXTRACT(effect_payload, '$.type') NOT IN ('passive', 'passive_trigger');

-- ============================================
-- 第二步：查看需要更新的技能详情（预览）
-- ============================================
SELECT 
    id,
    code,
    name,
    JSON_EXTRACT(effect_payload, '$.type') as skill_type,
    JSON_EXTRACT(effect_payload, '$.action_cost') as current_action_cost,
    JSON_EXTRACT(effect_payload, '$.mana_cost') as current_mana_cost
FROM skills
WHERE JSON_EXTRACT(effect_payload, '$.type') NOT IN ('passive', 'passive_trigger')
  AND JSON_EXTRACT(effect_payload, '$.action_cost') IS NOT NULL
ORDER BY id;

-- ============================================
-- 第三步：更新主动技能，将 action_cost 改为 mana_cost
-- ============================================
-- 注意：MySQL 的 JSON_SET 函数会添加新字段，JSON_REMOVE 会删除旧字段
-- 我们需要先添加 mana_cost，然后删除 action_cost

UPDATE skills
SET effect_payload = JSON_REMOVE(
    JSON_SET(
        effect_payload,
        '$.mana_cost',
        JSON_EXTRACT(effect_payload, '$.action_cost')
    ),
    '$.action_cost'
)
WHERE JSON_EXTRACT(effect_payload, '$.type') NOT IN ('passive', 'passive_trigger')
  AND JSON_EXTRACT(effect_payload, '$.action_cost') IS NOT NULL
  AND JSON_EXTRACT(effect_payload, '$.mana_cost') IS NULL;

-- ============================================
-- 第四步：验证更新结果
-- ============================================
-- 检查是否还有 action_cost 字段（应该为0）
SELECT 
    COUNT(*) as remaining_action_cost
FROM skills
WHERE JSON_EXTRACT(effect_payload, '$.type') NOT IN ('passive', 'passive_trigger')
  AND JSON_EXTRACT(effect_payload, '$.action_cost') IS NOT NULL;

-- 检查 mana_cost 字段是否正确添加
SELECT 
    id,
    code,
    name,
    JSON_EXTRACT(effect_payload, '$.type') as skill_type,
    JSON_EXTRACT(effect_payload, '$.mana_cost') as new_mana_cost,
    JSON_EXTRACT(effect_payload, '$.action_cost') as old_action_cost
FROM skills
WHERE JSON_EXTRACT(effect_payload, '$.type') NOT IN ('passive', 'passive_trigger')
ORDER BY id
LIMIT 20;

-- ============================================
-- 第五步：特殊情况处理
-- ============================================
-- 处理被动触发技能中的 action_cost: 0（应该删除，因为被动技能不消耗法力值）
-- 例如：warrior_unyielding_will 有 "action_cost": 0

UPDATE skills
SET effect_payload = JSON_REMOVE(effect_payload, '$.action_cost')
WHERE JSON_EXTRACT(effect_payload, '$.type') IN ('passive', 'passive_trigger')
  AND JSON_EXTRACT(effect_payload, '$.action_cost') IS NOT NULL;

-- ============================================
-- 更新完成后的验证查询
-- ============================================
-- 统计各职业的主动技能数量
SELECT 
    player_character_code,
    COUNT(*) as total_skills,
    COUNT(CASE WHEN JSON_EXTRACT(effect_payload, '$.type') NOT IN ('passive', 'passive_trigger') THEN 1 END) as active_skills,
    COUNT(CASE WHEN JSON_EXTRACT(effect_payload, '$.type') IN ('passive', 'passive_trigger') THEN 1 END) as passive_skills,
    COUNT(CASE WHEN JSON_EXTRACT(effect_payload, '$.mana_cost') IS NOT NULL THEN 1 END) as skills_with_mana_cost
FROM skills
GROUP BY player_character_code;

-- 显示所有主动技能的法力值消耗
SELECT 
    id,
    code,
    name,
    player_character_code,
    JSON_EXTRACT(effect_payload, '$.type') as skill_type,
    JSON_EXTRACT(effect_payload, '$.mana_cost') as mana_cost,
    JSON_EXTRACT(effect_payload, '$.target') as target
FROM skills
WHERE JSON_EXTRACT(effect_payload, '$.type') NOT IN ('passive', 'passive_trigger')
ORDER BY player_character_code, id;

