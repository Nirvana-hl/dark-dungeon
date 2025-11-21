package com.dungeon.service;

import com.alibaba.fastjson2.JSONArray;
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
 * 负责技能树的查询、解锁逻辑
 * 
 * 设计说明：
 * - 技能解锁关联到"用户+职业模板"，而不是"角色实例"
 * - 直接查询 user_player_character_skills 表，无需关联 user_player_characters 表
 * - 这样即使删除重建角色实例，技能解锁也不会丢失
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

    /**
     * 根据职业短码获取技能树
     * @param Code 职业短码
     * @param userId 用户ID（用于判断技能是否已解锁）
     * @return 技能树列表
     */
    public List<SkillDTO> getSkillTreeByPlayerCharacterCode(String Code, Long userId) {
        // 查询该职业的所有技能
        QueryWrapper<Skill> wrapper = new QueryWrapper<>();
        wrapper.eq("code", Code);
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
        characterWrapper.eq("code", skill.getCode());
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
        return dto;
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
}
