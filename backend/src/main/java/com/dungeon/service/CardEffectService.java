package com.dungeon.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 卡牌效果执行服务
 * 用于执行法术和装备的效果逻辑，包括治疗、伤害、护盾、buff等
 */
@Service
public class CardEffectService {

    private static final Random RANDOM = new Random();

    /**
     * 战斗上下文，用于存储战斗状态
     */
    public static class BattleContext {
        // 英雄状态
        private int heroHp;
        private int heroMaxHp;
        private int heroAttack;
        private int heroShield; // 护盾值
        
        // 敌人状态
        private int enemyHp;
        private int enemyMaxHp;
        private int enemyAttack;
        private int enemyShield; // 护盾值
        
        // 战斗日志
        private List<String> battleLog;
        
        // Buff状态（持续效果）
        private Map<String, Object> heroBuffs;
        private Map<String, Object> enemyBuffs;

        public BattleContext(int heroHp, int heroMaxHp, int heroAttack, 
                           int enemyHp, int enemyMaxHp, int enemyAttack) {
            this.heroHp = heroHp;
            this.heroMaxHp = heroMaxHp;
            this.heroAttack = heroAttack;
            this.enemyHp = enemyHp;
            this.enemyMaxHp = enemyMaxHp;
            this.enemyAttack = enemyAttack;
            this.heroShield = 0;
            this.enemyShield = 0;
            this.battleLog = new ArrayList<>();
        }

        // Getters and Setters
        public int getHeroHp() { return heroHp; }
        public void setHeroHp(int heroHp) { this.heroHp = Math.max(0, Math.min(heroHp, heroMaxHp)); }
        public int getHeroMaxHp() { return heroMaxHp; }
        public void setHeroMaxHp(int heroMaxHp) { this.heroMaxHp = heroMaxHp; }
        public int getHeroAttack() { return heroAttack; }
        public void setHeroAttack(int heroAttack) { this.heroAttack = heroAttack; }
        public int getHeroShield() { return heroShield; }
        public void setHeroShield(int heroShield) { this.heroShield = Math.max(0, heroShield); }
        
        public int getEnemyHp() { return enemyHp; }
        public void setEnemyHp(int enemyHp) { this.enemyHp = Math.max(0, Math.min(enemyHp, enemyMaxHp)); }
        public int getEnemyMaxHp() { return enemyMaxHp; }
        public void setEnemyMaxHp(int enemyMaxHp) { this.enemyMaxHp = enemyMaxHp; }
        public int getEnemyAttack() { return enemyAttack; }
        public void setEnemyAttack(int enemyAttack) { this.enemyAttack = enemyAttack; }
        public int getEnemyShield() { return enemyShield; }
        public void setEnemyShield(int enemyShield) { this.enemyShield = Math.max(0, enemyShield); }
        
        public List<String> getBattleLog() { return battleLog; }
        public void addLog(String log) { this.battleLog.add(log); }
        
        public Map<String, Object> getHeroBuffs() { return heroBuffs; }
        public void setHeroBuffs(Map<String, Object> heroBuffs) { this.heroBuffs = heroBuffs; }
        public Map<String, Object> getEnemyBuffs() { return enemyBuffs; }
        public void setEnemyBuffs(Map<String, Object> enemyBuffs) { this.enemyBuffs = enemyBuffs; }
    }

    /**
     * 执行卡牌效果
     * @param effectPayload 效果配置（JSON字符串）
     * @param cardName 卡牌名称（用于日志）
     * @param context 战斗上下文
     * @param target 目标类型：hero-英雄, enemy-敌人, all_allies-所有友方, all_enemies-所有敌人
     */
    public void executeCardEffect(String effectPayload, String cardName, BattleContext context, String target) {
        if (!StringUtils.hasText(effectPayload)) {
            return;
        }

        try {
            JSONObject effect = JSON.parseObject(effectPayload);
            executeEffect(effect, cardName, context, target);
        } catch (Exception e) {
            context.addLog("卡牌效果解析失败：" + cardName + "，错误：" + e.getMessage());
        }
    }

    /**
     * 执行效果
     */
    private void executeEffect(JSONObject effect, String cardName, BattleContext context, String target) {
        // 1. 治疗效果
        if (effect.containsKey("heal") || effect.containsKey("heal_amount") || effect.containsKey("heal_single")) {
            executeHealEffect(effect, cardName, context, target);
        }
        
        // 2. 伤害效果
        if (effect.containsKey("damage") || effect.containsKey("damage_single")) {
            executeDamageEffect(effect, cardName, context, target);
        }
        
        // 3. 护盾效果
        if (effect.containsKey("armor") || effect.containsKey("shield")) {
            executeShieldEffect(effect, cardName, context, target);
        }
        
        // 4. Buff效果
        if (effect.containsKey("attack_bonus") || effect.containsKey("attack") || 
            effect.containsKey("defense_bonus") || effect.containsKey("defense")) {
            executeBuffEffect(effect, cardName, context, target);
        }
        
        // 5. 特殊效果
        if (effect.containsKey("lifesteal") || effect.containsKey("burn") || 
            effect.containsKey("stun") || effect.containsKey("freeze")) {
            executeSpecialEffect(effect, cardName, context, target);
        }
    }

    /**
     * 执行治疗效果
     */
    private void executeHealEffect(JSONObject effect, String cardName, BattleContext context, String target) {
        int healAmount = 0;
        
        // 兼容多种格式
        if (effect.containsKey("heal")) {
            healAmount = effect.getIntValue("heal");
        } else if (effect.containsKey("heal_amount")) {
            healAmount = effect.getIntValue("heal_amount");
        } else if (effect.containsKey("heal_single")) {
            healAmount = effect.getIntValue("heal_single");
        }
        
        if (healAmount <= 0) {
            return;
        }

        // 确定目标
        String actualTarget = target;
        if (effect.containsKey("target")) {
            actualTarget = effect.getString("target");
        }
        
        // 判断是否为群体治疗
        boolean isAOE = effect.getBooleanValue("aoe") || 
                        "all_allies".equalsIgnoreCase(actualTarget) ||
                        "allies".equalsIgnoreCase(actualTarget);

        if (isAOE || "all_allies".equalsIgnoreCase(actualTarget)) {
            // 群体治疗（治疗英雄）
            int oldHp = context.getHeroHp();
            context.setHeroHp(context.getHeroHp() + healAmount);
            int actualHeal = context.getHeroHp() - oldHp;
            if (actualHeal > 0) {
                context.addLog("法术：" + cardName + "，英雄恢复 " + actualHeal + " 点生命（当前HP：" + context.getHeroHp() + "/" + context.getHeroMaxHp() + "）");
            }
        } else {
            // 单体治疗
            if ("hero".equalsIgnoreCase(actualTarget) || "ally".equalsIgnoreCase(actualTarget) || actualTarget == null) {
                int oldHp = context.getHeroHp();
                context.setHeroHp(context.getHeroHp() + healAmount);
                int actualHeal = context.getHeroHp() - oldHp;
                if (actualHeal > 0) {
                    context.addLog("法术：" + cardName + "，英雄恢复 " + actualHeal + " 点生命（当前HP：" + context.getHeroHp() + "/" + context.getHeroMaxHp() + "）");
                }
            } else if ("enemy".equalsIgnoreCase(actualTarget)) {
                int oldHp = context.getEnemyHp();
                context.setEnemyHp(context.getEnemyHp() + healAmount);
                int actualHeal = context.getEnemyHp() - oldHp;
                if (actualHeal > 0) {
                    context.addLog("法术：" + cardName + "，敌人恢复 " + actualHeal + " 点生命（当前HP：" + context.getEnemyHp() + "/" + context.getEnemyMaxHp() + "）");
                }
            }
        }
    }

    /**
     * 执行伤害效果
     */
    private void executeDamageEffect(JSONObject effect, String cardName, BattleContext context, String target) {
        int damage = 0;
        
        // 兼容多种格式
        if (effect.containsKey("damage")) {
            damage = effect.getIntValue("damage");
        } else if (effect.containsKey("damage_single")) {
            damage = effect.getIntValue("damage_single");
        }
        
        if (damage <= 0) {
            return;
        }

        // 确定目标
        String actualTarget = target;
        if (effect.containsKey("target")) {
            actualTarget = effect.getString("target");
        }
        
        // 判断是否为AOE伤害
        boolean isAOE = effect.getBooleanValue("aoe") || 
                        "all_enemies".equalsIgnoreCase(actualTarget);

        if (isAOE || "all_enemies".equalsIgnoreCase(actualTarget)) {
            // AOE伤害（对敌人）
            applyDamage(context, damage, true, cardName);
        } else {
            // 单体伤害
            if ("enemy".equalsIgnoreCase(actualTarget) || actualTarget == null) {
                applyDamage(context, damage, true, cardName);
            } else if ("hero".equalsIgnoreCase(actualTarget) || "ally".equalsIgnoreCase(actualTarget)) {
                applyDamage(context, damage, false, cardName);
            }
        }
    }

    /**
     * 应用伤害（考虑护盾）
     */
    private void applyDamage(BattleContext context, int damage, boolean isToEnemy, String cardName) {
        if (isToEnemy) {
            // 对敌人造成伤害
            int shieldAbsorb = Math.min(context.getEnemyShield(), damage);
            int actualDamage = damage - shieldAbsorb;
            
            context.setEnemyShield(context.getEnemyShield() - shieldAbsorb);
            context.setEnemyHp(context.getEnemyHp() - actualDamage);
            
            String log = "法术：" + cardName + "，对敌人造成 " + damage + " 点伤害";
            if (shieldAbsorb > 0) {
                log += "（护盾吸收 " + shieldAbsorb + " 点）";
            }
            log += "。敌人剩余 " + context.getEnemyHp() + "/" + context.getEnemyMaxHp() + " HP";
            if (context.getEnemyShield() > 0) {
                log += "，护盾 " + context.getEnemyShield();
            }
            context.addLog(log);
        } else {
            // 对英雄造成伤害
            int shieldAbsorb = Math.min(context.getHeroShield(), damage);
            int actualDamage = damage - shieldAbsorb;
            
            context.setHeroShield(context.getHeroShield() - shieldAbsorb);
            context.setHeroHp(context.getHeroHp() - actualDamage);
            
            String log = "法术：" + cardName + "，对英雄造成 " + damage + " 点伤害";
            if (shieldAbsorb > 0) {
                log += "（护盾吸收 " + shieldAbsorb + " 点）";
            }
            log += "。英雄剩余 " + context.getHeroHp() + "/" + context.getHeroMaxHp() + " HP";
            if (context.getHeroShield() > 0) {
                log += "，护盾 " + context.getHeroShield();
            }
            context.addLog(log);
        }
    }

    /**
     * 执行护盾效果
     */
    private void executeShieldEffect(JSONObject effect, String cardName, BattleContext context, String target) {
        int armorAmount = 0;
        
        // 兼容多种格式
        if (effect.containsKey("armor")) {
            armorAmount = effect.getIntValue("armor");
        } else if (effect.containsKey("shield")) {
            armorAmount = effect.getIntValue("shield");
        }
        
        if (armorAmount <= 0) {
            return;
        }

        // 确定目标
        String actualTarget = target;
        if (effect.containsKey("target")) {
            actualTarget = effect.getString("target");
        }
        
        // 判断是否为群体护盾
        boolean isAOE = effect.getBooleanValue("aoe") || 
                        "all_allies".equalsIgnoreCase(actualTarget);

        if (isAOE || "all_allies".equalsIgnoreCase(actualTarget)) {
            // 群体护盾（给英雄）
            context.setHeroShield(context.getHeroShield() + armorAmount);
            context.addLog("法术：" + cardName + "，英雄获得 " + armorAmount + " 点护盾（当前护盾：" + context.getHeroShield() + "）");
        } else {
            // 单体护盾
            if ("hero".equalsIgnoreCase(actualTarget) || "ally".equalsIgnoreCase(actualTarget) || actualTarget == null) {
                context.setHeroShield(context.getHeroShield() + armorAmount);
                context.addLog("法术：" + cardName + "，英雄获得 " + armorAmount + " 点护盾（当前护盾：" + context.getHeroShield() + "）");
            } else if ("enemy".equalsIgnoreCase(actualTarget)) {
                context.setEnemyShield(context.getEnemyShield() + armorAmount);
                context.addLog("法术：" + cardName + "，敌人获得 " + armorAmount + " 点护盾（当前护盾：" + context.getEnemyShield() + "）");
            }
        }
    }

    /**
     * 执行Buff效果
     */
    private void executeBuffEffect(JSONObject effect, String cardName, BattleContext context, String target) {
        // 确定目标
        String actualTarget = target;
        if (effect.containsKey("target")) {
            actualTarget = effect.getString("target");
        }

        // 攻击力加成
        if (effect.containsKey("attack_bonus")) {
            int attackBonus = effect.getIntValue("attack_bonus");
            if ("hero".equalsIgnoreCase(actualTarget) || "ally".equalsIgnoreCase(actualTarget) || actualTarget == null) {
                context.setHeroAttack(context.getHeroAttack() + attackBonus);
                context.addLog("法术：" + cardName + "，英雄攻击力 +" + attackBonus + "（当前攻击：" + context.getHeroAttack() + "）");
            } else if ("enemy".equalsIgnoreCase(actualTarget)) {
                context.setEnemyAttack(context.getEnemyAttack() + attackBonus);
                context.addLog("法术：" + cardName + "，敌人攻击力 +" + attackBonus + "（当前攻击：" + context.getEnemyAttack() + "）");
            }
        }
        
        // 攻击力百分比减少（对敌人）
        if (effect.containsKey("attack_reduction")) {
            double reduction = effect.getDoubleValue("attack_reduction");
            if ("enemy".equalsIgnoreCase(actualTarget)) {
                int currentAttack = context.getEnemyAttack();
                int newAttack = (int)(currentAttack * (1 - reduction));
                context.setEnemyAttack(Math.max(0, newAttack));
                context.addLog("技能：" + cardName + "，敌人攻击力减少 " + String.format("%.0f", reduction * 100) + "%（当前攻击：" + context.getEnemyAttack() + "）");
            }
        }
        
        // 降低敌人攻击力（全局效果，不需要指定target）
        if (effect.containsKey("enemy_attack_reduction")) {
            double reduction = effect.getDoubleValue("enemy_attack_reduction");
            int currentAttack = context.getEnemyAttack();
            int newAttack = (int)(currentAttack * (1 - reduction));
            context.setEnemyAttack(Math.max(0, newAttack));
            context.addLog("技能：" + cardName + "，敌人攻击力减少 " + String.format("%.0f", reduction * 100) + "%（当前攻击：" + context.getEnemyAttack() + "）");
        }
        
        // 防御力加成
        if (effect.containsKey("defense_bonus") || effect.containsKey("defense")) {
            int defenseBonus = effect.containsKey("defense_bonus") ? 
                              effect.getIntValue("defense_bonus") : 
                              effect.getIntValue("defense");
            if ("hero".equalsIgnoreCase(actualTarget) || "ally".equalsIgnoreCase(actualTarget) || actualTarget == null) {
                context.setHeroShield(context.getHeroShield() + defenseBonus);
                context.addLog("法术：" + cardName + "，英雄防御力 +" + defenseBonus + "（当前护盾：" + context.getHeroShield() + "）");
            } else if ("enemy".equalsIgnoreCase(actualTarget)) {
                context.setEnemyShield(context.getEnemyShield() + defenseBonus);
                context.addLog("法术：" + cardName + "，敌人防御力 +" + defenseBonus + "（当前护盾：" + context.getEnemyShield() + "）");
            }
        }
    }

    /**
     * 执行特殊效果（吸血、燃烧、眩晕等）
     */
    private void executeSpecialEffect(JSONObject effect, String cardName, BattleContext context, String target) {
        // 吸血效果（lifesteal）
        if (effect.containsKey("lifesteal")) {
            double lifestealRate = effect.getDoubleValue("lifesteal");
            // 吸血效果通常在造成伤害时触发，这里先记录到buff中
            context.addLog("装备：" + cardName + "，获得 " + (lifestealRate * 100) + "% 吸血效果");
        }
        
        // 燃烧效果（burn）
        if (effect.containsKey("burn") || effect.containsKey("burn_chance")) {
            boolean hasBurn = effect.getBooleanValue("burn");
            double burnChance = effect.getDoubleValue("burn_chance");
            if (hasBurn || (burnChance > 0 && RANDOM.nextDouble() < burnChance)) {
                int burnDamage = effect.getIntValue("burn_damage");
                int burnDuration = effect.getIntValue("burn_duration");
                if (burnDamage > 0) {
                    context.addLog("法术：" + cardName + "，敌人受到燃烧效果，每回合造成 " + burnDamage + " 点伤害，持续 " + burnDuration + " 回合");
                    // 这里可以记录到buff中，在后续回合中持续造成伤害
                }
            }
        }
        
        // 眩晕效果（stun）
        if (effect.containsKey("stun") || effect.containsKey("stun_chance")) {
            boolean hasStun = effect.getBooleanValue("stun");
            double stunChance = effect.getDoubleValue("stun_chance");
            if (hasStun || (stunChance > 0 && RANDOM.nextDouble() < stunChance)) {
                int stunDuration = effect.getIntValue("stun_duration");
                context.addLog("法术：" + cardName + "，敌人被眩晕 " + stunDuration + " 回合");
                // 这里可以记录到buff中，跳过敌人的回合
            }
        }
        
        // 冰冻效果（freeze）
        if (effect.containsKey("freeze_chance")) {
            double freezeChance = effect.getDoubleValue("freeze_chance");
            if (RANDOM.nextDouble() < freezeChance) {
                context.addLog("法术：" + cardName + "，敌人被冰冻，无法行动");
                // 这里可以记录到buff中，跳过敌人的回合
            }
        }
    }

    /**
     * 执行角色特性效果（从 card_character_traits 表）
     * @param effectPayload 特性效果配置（JSON字符串）
     * @param traitName 特性名称
     * @param context 战斗上下文
     * @param starLevel 角色星级（用于计算缩放效果）
     */
    public void executeTraitEffect(String effectPayload, String traitName, BattleContext context, int starLevel) {
        if (!StringUtils.hasText(effectPayload)) {
            return;
        }

        try {
            JSONObject effect = JSON.parseObject(effectPayload);
            
            // 处理治疗效果（heal_allies）
            if (effect.containsKey("heal_allies")) {
                int healAmount = effect.getIntValue("heal_allies");
                // 这里可以根据星级缩放，但需要传入scalingPayload
                int oldHp = context.getHeroHp();
                context.setHeroHp(context.getHeroHp() + healAmount);
                int actualHeal = context.getHeroHp() - oldHp;
                if (actualHeal > 0) {
                    context.addLog("特性：" + traitName + "，英雄恢复 " + actualHeal + " 点生命（当前HP：" + context.getHeroHp() + "/" + context.getHeroMaxHp() + "）");
                }
            }
            
            // 处理护甲加成（armor_bonus）
            if (effect.containsKey("armor_bonus")) {
                int armorBonus = effect.getIntValue("armor_bonus");
                context.setHeroShield(context.getHeroShield() + armorBonus);
                context.addLog("特性：" + traitName + "，英雄获得 " + armorBonus + " 点护甲（当前护盾：" + context.getHeroShield() + "）");
            }
            
            // 处理攻击加成（attack_bonus）
            if (effect.containsKey("attack_bonus")) {
                int attackBonus = effect.getIntValue("attack_bonus");
                context.setHeroAttack(context.getHeroAttack() + attackBonus);
                context.addLog("特性：" + traitName + "，英雄攻击力 +" + attackBonus + "（当前攻击：" + context.getHeroAttack() + "）");
            }
            
        } catch (Exception e) {
            context.addLog("特性效果解析失败：" + traitName + "，错误：" + e.getMessage());
        }
    }

    /**
     * 根据星级计算特性效果值
     * @param baseEffect 基础效果（JSON对象）
     * @param scalingPayload 星级缩放配置（JSON字符串）
     * @param starLevel 当前星级
     * @return 计算后的效果值
     */
    public JSONObject calculateTraitEffectWithScaling(JSONObject baseEffect, String scalingPayload, int starLevel) {
        if (!StringUtils.hasText(scalingPayload) || starLevel <= 1) {
            return baseEffect;
        }

        try {
            JSONObject scaling = JSON.parseObject(scalingPayload);
            JSONObject scaledEffect = new JSONObject(baseEffect);
            
            // 查找对应星级的缩放配置
            String starKey = String.valueOf(starLevel);
            if (scaling.containsKey(starKey)) {
                JSONObject starScaling = scaling.getJSONObject(starKey);
                // 合并缩放配置到基础效果
                for (String key : starScaling.keySet()) {
                    scaledEffect.put(key, starScaling.get(key));
                }
            }
            
            return scaledEffect;
        } catch (Exception e) {
            // 解析失败，返回基础效果
            return baseEffect;
        }
    }
}

