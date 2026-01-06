package com.dungeon.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.SkillDTO;
import com.dungeon.dto.UnlockSkillRequest;
import com.dungeon.dto.UserSkillDTO;
import com.dungeon.entity.Skill;
import com.dungeon.entity.User;
import com.dungeon.entity.UserPlayerCharacterSkill;
import com.dungeon.mapper.PlayerCharacterMapper;
import com.dungeon.mapper.SkillMapper;
import com.dungeon.mapper.UserPlayerCharacterSkillMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 技能服务
 * 负责技能相关的所有业务逻辑，包括：
 * 1. 技能数据管理（查询技能树、获取已解锁技能）
 * 2. 技能解锁逻辑（检查条件、解锁技能）
 * 3. 技能效果执行（在战斗中使用技能）
 * 
 * 设计说明：
 * - 技能解锁关联到"用户+职业模板"，而不是"角色实例"
 * - 直接查询 user_player_character_skills 表，无需关联 user_player_characters 表
 * - 这样即使删除重建角色实例，技能解锁也不会丢失
 * - 技能效果执行复用 CardEffectService 的逻辑
 */
@Service
public class SkillService {

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private UserPlayerCharacterSkillMapper userPlayerCharacterSkillMapper;

    @Autowired
    private PlayerCharacterMapper playerCharacterMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CardEffectService cardEffectService;

    /**
     * 根据职业短码获取技能树
     * @param Code 职业短码
     * @param userId 用户ID（用于判断技能是否已解锁）
     * @return 技能树列表
     */
    public List<SkillDTO> getSkillTreeByPlayerCharacterCode(String Code, Long userId) {
        // 查询该职业的所有技能（使用 player_character_code 字段）
        QueryWrapper<Skill> wrapper = new QueryWrapper<>();
        wrapper.eq("player_character_code", Code);
        List<Skill> skills = skillMapper.selectList(wrapper);

        if (skills.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取职业模板ID（通过player_character_code找到对应的player_character_id）
        QueryWrapper<com.dungeon.entity.PlayerCharacter> characterWrapper = new QueryWrapper<>();
        characterWrapper.eq("code", Code);
        com.dungeon.entity.PlayerCharacter playerCharacter = playerCharacterMapper.selectOne(characterWrapper);
        
        if (playerCharacter == null) {
            // 如果职业模板不存在，返回技能树但不标记解锁状态
            return skills.stream()
                    .map(skill -> toSkillDTO(skill, false, false))
                    .collect(Collectors.toList());
        }

        Long playerCharacterId = playerCharacter.getId();

        // 直接查询 user_player_character_skills 表，获取用户已解锁的技能ID集合
        QueryWrapper<UserPlayerCharacterSkill> unlockedWrapper = new QueryWrapper<>();
        unlockedWrapper.eq("user_id", userId)
                      .eq("player_character_id", playerCharacterId);
        List<UserPlayerCharacterSkill> unlockedSkills = userPlayerCharacterSkillMapper.selectList(unlockedWrapper);
        Set<Long> unlockedSkillIds = unlockedSkills.stream()
                .map(UserPlayerCharacterSkill::getSkillId)
                .collect(Collectors.toSet());

        // 获取用户等级
        User user = userService.getUserById(userId);
        Integer userLevel = user != null ? user.getPlayerLevel() : 1;

        // 转换为DTO并标记解锁状态
        return skills.stream()
                .map(skill -> {
                    boolean isUnlocked = unlockedSkillIds.contains(skill.getId());
                    boolean canUnlock = canUnlockSkill(skill, userLevel, unlockedSkillIds);
                    return toSkillDTO(skill, isUnlocked, canUnlock);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取用户已解锁的技能列表
     * 返回该用户所有职业的已解锁技能
     * 
     * @param userId 用户ID
     * @return 已解锁技能列表（包含所有职业）
     */
    public List<UserSkillDTO> getUserUnlockedSkills(Long userId) {
        // 直接查询 user_player_character_skills 表，获取该用户所有已解锁的技能
        QueryWrapper<UserPlayerCharacterSkill> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserPlayerCharacterSkill> unlockedSkills = userPlayerCharacterSkillMapper.selectList(wrapper);

        // 转换为DTO
        return unlockedSkills.stream()
                .map(this::toUserSkillDTO)
                .collect(Collectors.toList());
    }

    /**
     * 解锁技能
     * @param userId 用户ID
     * @param request 解锁请求
     * @return 解锁后的技能DTO
     */
    @Transactional
    public SkillDTO unlockSkill(Long userId, UnlockSkillRequest request) {
        // 查询技能模板
        Skill skill = skillMapper.selectById(request.getSkillId());
        if (skill == null) {
            throw new RuntimeException("技能不存在");
        }

        // 通过技能的 player_character_code 找到对应的 player_character_id
        QueryWrapper<com.dungeon.entity.PlayerCharacter> characterWrapper = new QueryWrapper<>();
        characterWrapper.eq("code", skill.getPlayerCharacterCode());
        com.dungeon.entity.PlayerCharacter playerCharacter = playerCharacterMapper.selectOne(characterWrapper);
        
        if (playerCharacter == null) {
            throw new RuntimeException("该技能对应的职业模板不存在");
        }

        Long playerCharacterId = playerCharacter.getId();

        // 检查是否已解锁（直接查询 user_player_character_skills 表）
        QueryWrapper<UserPlayerCharacterSkill> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("user_id", userId)
                   .eq("player_character_id", playerCharacterId)
                   .eq("skill_id", request.getSkillId());
        UserPlayerCharacterSkill existing = userPlayerCharacterSkillMapper.selectOne(checkWrapper);
        if (existing != null) {
            throw new RuntimeException("该技能已解锁");
        }

        // 获取用户等级
        User user = userService.getUserById(userId);
        Integer userLevel = user != null ? user.getPlayerLevel() : 1;

        // 获取已解锁技能ID集合（用于检查前置技能，直接查询 user_player_character_skills 表）
        QueryWrapper<UserPlayerCharacterSkill> unlockedWrapper = new QueryWrapper<>();
        unlockedWrapper.eq("user_id", userId)
                      .eq("player_character_id", playerCharacterId);
        List<UserPlayerCharacterSkill> unlockedSkills = userPlayerCharacterSkillMapper.selectList(unlockedWrapper);
        Set<Long> unlockedSkillIds = unlockedSkills.stream()
                .map(UserPlayerCharacterSkill::getSkillId)
                .collect(Collectors.toSet());

        // 检查解锁条件
        if (!canUnlockSkill(skill, userLevel, unlockedSkillIds)) {
            throw new RuntimeException("不满足解锁条件：等级不足或前置技能未解锁");
        }

        // 创建解锁记录（直接插入 user_player_character_skills 表）
        UserPlayerCharacterSkill userSkill = new UserPlayerCharacterSkill();
        userSkill.setUserId(userId);
        userSkill.setPlayerCharacterId(playerCharacterId);
        userSkill.setSkillId(request.getSkillId());
        userSkill.setUnlockedAt(LocalDateTime.now());
        userPlayerCharacterSkillMapper.insert(userSkill);

        // 返回技能DTO
        return toSkillDTO(skill, true, true);
    }

    /**
     * 判断技能是否可以解锁
     * @param skill 技能模板
     * @param userLevel 用户等级
     * @param unlockedSkillIds 已解锁技能ID集合
     * @return 是否可以解锁
     */
    private boolean canUnlockSkill(Skill skill, Integer userLevel, Set<Long> unlockedSkillIds) {
        // 检查等级要求
        if (skill.getRequiredLevel() != null && userLevel < skill.getRequiredLevel()) {
            return false;
        }

        // 检查前置技能
        if (skill.getUnlockPath() != null && !skill.getUnlockPath().isEmpty()) {
            try {
                JSONArray unlockPathArray = JSONArray.parseArray(skill.getUnlockPath());
                if (unlockPathArray != null && !unlockPathArray.isEmpty()) {
                    // 查询前置技能ID
                    List<String> prerequisiteCodes = unlockPathArray.toJavaList(String.class);
                    QueryWrapper<Skill> prerequisiteWrapper = new QueryWrapper<>();
                    prerequisiteWrapper.in("code", prerequisiteCodes);
                    List<Skill> prerequisiteSkills = skillMapper.selectList(prerequisiteWrapper);
                    
                    // 检查所有前置技能是否已解锁
                    for (Skill prerequisite : prerequisiteSkills) {
                        if (!unlockedSkillIds.contains(prerequisite.getId())) {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                // JSON解析失败，忽略前置技能检查
            }
        }

        return true;
    }

    /**
     * 技能实体转DTO
     */
    private SkillDTO toSkillDTO(Skill skill, boolean isUnlocked, boolean canUnlock) {
        SkillDTO dto = new SkillDTO();
        BeanUtils.copyProperties(skill, dto);
        dto.setIsUnlocked(isUnlocked);
        dto.setCanUnlock(canUnlock);
        
        // 解析effectPayload，判断技能类型（主动/被动）
        String skillType = determineSkillType(skill.getEffectPayload());
        dto.setSkillType(skillType);
        
        return dto;
    }
    
    /**
     * 根据effectPayload判断技能类型
     * @param effectPayload 技能效果配置（JSON字符串）
     * @return "active" 或 "passive"
     */
    private String determineSkillType(String effectPayload) {
        if (effectPayload == null || effectPayload.trim().isEmpty()) {
            return "active"; // 默认为主动技能
        }
        
        try {
            JSONObject effect = JSONObject.parseObject(effectPayload);
            String type = effect.getString("type");
            
            // 如果type为"passive"或"passive_trigger"，则为被动技能
            if ("passive".equals(type) || "passive_trigger".equals(type)) {
                return "passive";
            }
            
            // 其他情况（attack、heal、buff、debuff等）为主动技能
            return "active";
        } catch (Exception e) {
            // JSON解析失败，默认为主动技能
            return "active";
        }
    }

    /**
     * 用户技能实体转DTO
     */
    private UserSkillDTO toUserSkillDTO(UserPlayerCharacterSkill userSkill) {
        UserSkillDTO dto = new UserSkillDTO();
        dto.setId(userSkill.getId());
        dto.setSkillId(userSkill.getSkillId());
        dto.setUnlockedAt(userSkill.getUnlockedAt());

        // 查询技能模板信息
        Skill skill = skillMapper.selectById(userSkill.getSkillId());
        if (skill != null) {
            dto.setSkillCode(skill.getCode());
            dto.setSkillName(skill.getName());
            dto.setSkillDescription(skill.getDescription());
        }

        return dto;
    }

    // ============================================
    // 技能效果执行方法
    // ============================================

    /**
     * 执行技能效果
     * @param skill 技能DTO
     * @param context 战斗上下文
     * @param target 目标类型：hero-英雄, enemy-敌人, all_allies-所有友方, all_enemies-所有敌人
     */
    public void executeSkillEffect(SkillDTO skill, 
                                   CardEffectService.BattleContext context, 
                                   String target) {
        if (skill == null || skill.getEffectPayload() == null || skill.getEffectPayload().trim().isEmpty()) {
            return;
        }

        // 复用 CardEffectService 的逻辑
        cardEffectService.executeCardEffect(
            skill.getEffectPayload(), 
            skill.getName(), 
            context, 
            target
        );
    }

    /**
     * 应用被动技能效果（战斗开始时）
     * 被动技能（type: "passive"）在战斗开始时自动应用
     * 
     * @param passiveSkills 被动技能列表
     * @param context 战斗上下文
     */
    public void applyPassiveSkills(List<SkillDTO> passiveSkills, 
                                   CardEffectService.BattleContext context) {
        if (passiveSkills == null || passiveSkills.isEmpty()) {
            return;
        }

        for (SkillDTO skill : passiveSkills) {
            if ("passive".equals(skill.getSkillType())) {
                // 应用被动效果（针对自己）
                executeSkillEffect(skill, context, "self");
                context.addLog("被动技能：" + skill.getName() + " 已激活");
            }
        }
    }

    /**
     * 触发被动技能（回合开始时或满足条件时）
     * 条件触发被动技能（type: "passive_trigger"）在满足条件时触发
     * 
     * @param passiveSkills 被动技能列表
     * @param context 战斗上下文
     * @param round 当前回合数
     */
    public void triggerPassiveSkills(List<SkillDTO> passiveSkills, 
                                     CardEffectService.BattleContext context, 
                                     int round) {
        if (passiveSkills == null || passiveSkills.isEmpty()) {
            return;
        }

        for (SkillDTO skill : passiveSkills) {
            if ("passive".equals(skill.getSkillType())) {
                // 检查是否是条件触发被动技能
                try {
                    JSONObject effect = JSONObject.parseObject(skill.getEffectPayload());
                    if ("passive_trigger".equals(effect.getString("type"))) {
                        // 检查触发条件
                        if (shouldTriggerPassiveSkill(skill, effect, context)) {
                            executeSkillEffect(skill, context, "self");
                            context.addLog("被动技能：" + skill.getName() + " 已触发");
                        }
                    }
                } catch (Exception e) {
                    // 解析失败，跳过
                }
            }
        }
    }

    /**
     * 判断条件触发被动技能是否应该触发
     * @param skill 技能DTO
     * @param effect 效果配置
     * @param context 战斗上下文
     * @return 是否应该触发
     */
    private boolean shouldTriggerPassiveSkill(SkillDTO skill, JSONObject effect, 
                                              CardEffectService.BattleContext context) {
        // 检查生命值阈值触发（trigger_hp_threshold）
        if (effect.containsKey("trigger_hp_threshold")) {
            double threshold = effect.getDoubleValue("trigger_hp_threshold");
            double currentHpRatio = (double) context.getHeroHp() / context.getHeroMaxHp();
            
            if (currentHpRatio <= threshold) {
                return true;
            }
        }
        
        // 可以添加其他触发条件
        // 例如：回合数触发、敌人数量触发等
        
        return false;
    }

    /**
     * 执行主动技能
     * 主动技能需要玩家选择使用，消耗法力值
     * 
     * @param skill 技能DTO
     * @param context 战斗上下文
     * @param currentMana 当前法力值
     * @return 消耗的法力值（如果法力值不足，返回-1）
     */
    public int executeActiveSkill(SkillDTO skill, 
                                 CardEffectService.BattleContext context, 
                                 int currentMana) {
        if (skill == null || !"active".equals(skill.getSkillType())) {
            return -1;
        }

        try {
            JSONObject effect = JSONObject.parseObject(skill.getEffectPayload());
            
            // 检查法力值消耗（优先使用mana_cost，如果没有则使用action_cost作为兼容）
            int manaCost = 0;
            if (effect.containsKey("mana_cost")) {
                manaCost = effect.getIntValue("mana_cost");
            } else if (effect.containsKey("action_cost")) {
                // 兼容旧的action_cost字段（将其视为mana_cost）
                manaCost = effect.getIntValue("action_cost");
            } else {
                // 默认消耗1点法力值
                manaCost = 1;
            }
            
            if (currentMana < manaCost) {
                context.addLog("技能：" + skill.getName() + " 需要 " + manaCost + " 点法力值，当前只有 " + currentMana + " 点");
                return -1;
            }

            // 确定目标
            String target = effect.getString("target");
            if (target == null) {
                // 根据技能类型确定默认目标
                String skillType = effect.getString("type");
                if ("attack".equals(skillType) || "debuff".equals(skillType)) {
                    target = "enemy";
                } else if ("heal".equals(skillType) || "buff".equals(skillType)) {
                    target = "ally";
                } else {
                    target = "self";
                }
            }

            // 执行技能效果
            executeSkillEffect(skill, context, target);
            
            // 记录日志
            context.addLog("玩家使用技能：" + skill.getName() + "（消耗 " + manaCost + " 点法力值）");
            
            return manaCost;
        } catch (Exception e) {
            context.addLog("技能：" + skill.getName() + " 执行失败：" + e.getMessage());
            return -1;
        }
    }

    /**
     * 获取用户已解锁的技能列表（用于战斗）
     * 根据职业代码过滤，只返回该职业的技能
     * 
     * @param userId 用户ID
     * @param playerCharacterCode 职业代码（如：warrior, occultist, ranger, warden）
     * @return 已解锁技能列表（包含主动和被动技能）
     */
    public List<SkillDTO> getUnlockedSkillsForBattle(Long userId, String playerCharacterCode) {
        // 获取该职业的技能树
        List<SkillDTO> skillTree = getSkillTreeByPlayerCharacterCode(playerCharacterCode, userId);
        
        // 只返回已解锁的技能
        return skillTree.stream()
                .filter(SkillDTO::getIsUnlocked)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取用户已解锁的主动技能列表（用于战斗）
     * 
     * @param userId 用户ID
     * @param playerCharacterCode 职业代码
     * @return 已解锁的主动技能列表
     */
    public List<SkillDTO> getActiveSkillsForBattle(Long userId, String playerCharacterCode) {
        return getUnlockedSkillsForBattle(userId, playerCharacterCode).stream()
                .filter(skill -> "active".equals(skill.getSkillType()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取用户已解锁的被动技能列表（用于战斗）
     * 
     * @param userId 用户ID
     * @param playerCharacterCode 职业代码
     * @return 已解锁的被动技能列表
     */
    public List<SkillDTO> getPassiveSkillsForBattle(Long userId, String playerCharacterCode) {
        return getUnlockedSkillsForBattle(userId, playerCharacterCode).stream()
                .filter(skill -> "passive".equals(skill.getSkillType()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 根据技能ID获取技能信息（用于验证技能是否已解锁）
     * 
     * @param skillId 技能ID
     * @param userId 用户ID
     * @return 技能DTO，如果未解锁或不存在则返回null
     */
    public SkillDTO getSkillById(Long skillId, Long userId) {
        // 查询技能模板
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            return null;
        }
        
        // 获取职业模板ID
        QueryWrapper<com.dungeon.entity.PlayerCharacter> characterWrapper = new QueryWrapper<>();
        characterWrapper.eq("code", skill.getPlayerCharacterCode());
        com.dungeon.entity.PlayerCharacter playerCharacter = playerCharacterMapper.selectOne(characterWrapper);
        
        if (playerCharacter == null) {
            return null;
        }
        
        Long playerCharacterId = playerCharacter.getId();
        
        // 检查是否已解锁
        QueryWrapper<UserPlayerCharacterSkill> unlockedWrapper = new QueryWrapper<>();
        unlockedWrapper.eq("user_id", userId)
                      .eq("player_character_id", playerCharacterId)
                      .eq("skill_id", skillId);
        UserPlayerCharacterSkill unlockedSkill = userPlayerCharacterSkillMapper.selectOne(unlockedWrapper);
        
        boolean isUnlocked = unlockedSkill != null;
        
        // 获取用户等级
        User user = userService.getUserById(userId);
        Integer userLevel = user != null ? user.getPlayerLevel() : 1;
        
        // 获取已解锁技能ID集合（用于判断canUnlock）
        QueryWrapper<UserPlayerCharacterSkill> allUnlockedWrapper = new QueryWrapper<>();
        allUnlockedWrapper.eq("user_id", userId)
                          .eq("player_character_id", playerCharacterId);
        List<UserPlayerCharacterSkill> allUnlockedSkills = userPlayerCharacterSkillMapper.selectList(allUnlockedWrapper);
        Set<Long> unlockedSkillIds = allUnlockedSkills.stream()
                .map(UserPlayerCharacterSkill::getSkillId)
                .collect(Collectors.toSet());
        
        boolean canUnlock = canUnlockSkill(skill, userLevel, unlockedSkillIds);
        
        return toSkillDTO(skill, isUnlocked, canUnlock);
    }
}
