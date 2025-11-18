package com.dungeon.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.StressDebuffDTO;
import com.dungeon.dto.UserPlayerCharacterDTO;
import com.dungeon.entity.PlayerCharacter;
import com.dungeon.entity.StressDebuffConfig;
import com.dungeon.entity.UserPlayerCharacter;
import com.dungeon.mapper.PlayerCharacterMapper;
import com.dungeon.mapper.StressDebuffConfigMapper;
import com.dungeon.mapper.UserPlayerCharacterMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 玩家角色实例服务
 */
@Service
public class UserPlayerCharacterService {

    @Autowired
    private UserPlayerCharacterMapper userPlayerCharacterMapper;

    @Autowired
    private PlayerCharacterMapper playerCharacterMapper;

    @Autowired
    private StressDebuffConfigMapper stressDebuffConfigMapper;

    /**
     * 获取用户的所有角色实例
     */
    public List<UserPlayerCharacterDTO> getUserPlayerCharacters(Long userId) {
        QueryWrapper<UserPlayerCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserPlayerCharacter> characters = userPlayerCharacterMapper.selectList(wrapper);
        
        return characters.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的角色实例（单个）
     */
    public UserPlayerCharacterDTO getUserPlayerCharacter(Long userId) {
        QueryWrapper<UserPlayerCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .last("LIMIT 1");
        UserPlayerCharacter character = userPlayerCharacterMapper.selectOne(wrapper);
        return character != null ? toDTO(character) : null;
    }

    /**
     * 根据ID获取角色实例
     */
    public UserPlayerCharacterDTO getUserPlayerCharacterById(Long id, Long userId) {
        QueryWrapper<UserPlayerCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id)
               .eq("user_id", userId);
        UserPlayerCharacter character = userPlayerCharacterMapper.selectOne(wrapper);
        return character != null ? toDTO(character) : null;
    }

    /**
     * 创建角色实例
     */
    @Transactional
    public UserPlayerCharacterDTO createUserPlayerCharacter(Long userId, Long playerCharacterId) {
        // 检查是否已存在
        QueryWrapper<UserPlayerCharacter> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("user_id", userId)
                   .eq("player_character_id", playerCharacterId);
        UserPlayerCharacter existing = userPlayerCharacterMapper.selectOne(checkWrapper);
        if (existing != null) {
            throw new RuntimeException("该角色已存在");
        }

        // 获取角色模板
        PlayerCharacter template = playerCharacterMapper.selectById(playerCharacterId);
        if (template == null) {
            throw new RuntimeException("角色模板不存在");
        }

        // 创建角色实例（初始等级为1）
        UserPlayerCharacter character = new UserPlayerCharacter();
        character.setUserId(userId);
        character.setPlayerCharacterId(playerCharacterId);
        
        // 计算初始属性（等级1）
        int level = 1;
        character.setMaxHp(template.getBaseHp() + template.getHpPerLevel() * (level - 1));
        character.setCurrentHp(character.getMaxHp());
        character.setMaxActionPoints(3 + level);  // 基础3点，每级+1
        character.setCurrentActionPoints(character.getMaxActionPoints());
        character.setCurrentStress(0);
        character.setStressLevel(1);
        character.setStressDebuffs("[]");  // 初始无debuff

        userPlayerCharacterMapper.insert(character);
        return toDTO(character);
    }

    /**
     * 更新角色状态（血量、行动点、压力等）
     */
    @Transactional
    public UserPlayerCharacterDTO updateUserPlayerCharacter(Long id, Long userId, 
                                                           Integer currentHp, 
                                                           Integer currentActionPoints,
                                                           Integer currentStress) {
        QueryWrapper<UserPlayerCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id)
               .eq("user_id", userId);
        UserPlayerCharacter character = userPlayerCharacterMapper.selectOne(wrapper);
        
        if (character == null) {
            throw new RuntimeException("角色不存在");
        }

        // 更新属性
        if (currentHp != null) {
            character.setCurrentHp(Math.min(currentHp, character.getMaxHp()));
        }
        if (currentActionPoints != null) {
            character.setCurrentActionPoints(Math.min(currentActionPoints, character.getMaxActionPoints()));
        }
        if (currentStress != null) {
            character.setCurrentStress(currentStress);
            // 更新压力层级（0-25为1级，26-50为2级，51-75为3级，76-100为4级）
            int newStressLevel = calculateStressLevel(currentStress);
            character.setStressLevel(newStressLevel);
            
            // 应用压力debuff
            applyStressDebuffs(character);
        }

        userPlayerCharacterMapper.updateById(character);
        return toDTO(character);
    }

    /**
     * 计算压力层级
     */
    private int calculateStressLevel(int stress) {
        if (stress <= 25) return 1;
        if (stress <= 50) return 2;
        if (stress <= 75) return 3;
        return 4;
    }

    /**
     * 应用压力debuff
     */
    private void applyStressDebuffs(UserPlayerCharacter character) {
        int stressLevel = character.getStressLevel();
        
        // 查询该压力层级的所有debuff配置
        QueryWrapper<StressDebuffConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("stress_level", stressLevel);
        List<StressDebuffConfig> debuffConfigs = stressDebuffConfigMapper.selectList(wrapper);

        List<String> activeDebuffs = new ArrayList<>();
        Random random = new Random();

        for (StressDebuffConfig config : debuffConfigs) {
            // 根据触发概率判断是否激活
            double chance = config.getTriggerChance().doubleValue();
            if (random.nextDouble() < chance) {
                activeDebuffs.add(config.getDebuffName());
            }
        }

        // 更新debuff列表（JSON格式）
        character.setStressDebuffs(JSON.toJSONString(activeDebuffs));
    }

    /**
     * 重置行动点（每轮开始时调用）
     */
    @Transactional
    public void resetActionPoints(Long id, Long userId) {
        QueryWrapper<UserPlayerCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id)
               .eq("user_id", userId);
        UserPlayerCharacter character = userPlayerCharacterMapper.selectOne(wrapper);
        
        if (character != null) {
            character.setCurrentActionPoints(character.getMaxActionPoints());
            userPlayerCharacterMapper.updateById(character);
        }
    }

    /**
     * 恢复血量（使用道具或技能时调用）
     */
    @Transactional
    public void heal(Long id, Long userId, int healAmount) {
        QueryWrapper<UserPlayerCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id)
               .eq("user_id", userId);
        UserPlayerCharacter character = userPlayerCharacterMapper.selectOne(wrapper);
        
        if (character != null) {
            int newHp = Math.min(character.getCurrentHp() + healAmount, character.getMaxHp());
            character.setCurrentHp(newHp);
            userPlayerCharacterMapper.updateById(character);
        }
    }

    /**
     * 实体转DTO
     */
    private UserPlayerCharacterDTO toDTO(UserPlayerCharacter character) {
        UserPlayerCharacterDTO dto = new UserPlayerCharacterDTO();
        BeanUtils.copyProperties(character, dto);

        // 关联查询角色模板信息
        if (character.getPlayerCharacterId() != null) {
            PlayerCharacter template = playerCharacterMapper.selectById(character.getPlayerCharacterId());
            if (template != null) {
                dto.setPlayerCharacterName(template.getName());
                dto.setPlayerCharacterCode(template.getCode());
            }
        }

        // 解析压力debuff列表
        try {
            List<String> debuffNames = JSONArray.parseArray(character.getStressDebuffs(), String.class);
            if (debuffNames != null && !debuffNames.isEmpty()) {
                List<StressDebuffDTO> debuffs = new ArrayList<>();
                for (String debuffName : debuffNames) {
                    // 查询debuff配置
                    QueryWrapper<StressDebuffConfig> wrapper = new QueryWrapper<>();
                    wrapper.eq("debuff_name", debuffName);
                    StressDebuffConfig config = stressDebuffConfigMapper.selectOne(wrapper);
                    if (config != null) {
                        StressDebuffDTO debuffDTO = new StressDebuffDTO();
                        BeanUtils.copyProperties(config, debuffDTO);
                        debuffs.add(debuffDTO);
                    }
                }
                dto.setStressDebuffs(debuffs);
            } else {
                dto.setStressDebuffs(new ArrayList<>());
            }
        } catch (Exception e) {
            dto.setStressDebuffs(new ArrayList<>());
        }

        return dto;
    }
}

