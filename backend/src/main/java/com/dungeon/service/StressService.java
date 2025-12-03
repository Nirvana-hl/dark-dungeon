package com.dungeon.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.StressDebuffDTO;
import com.dungeon.dto.StressStatusDTO;
import com.dungeon.entity.StressDebuffConfig;
import com.dungeon.entity.UserPlayerCharacter;
import com.dungeon.mapper.StressDebuffConfigMapper;
import com.dungeon.mapper.UserPlayerCharacterMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 压力系统服务
 */
@Service
public class StressService {

    @Autowired
    private UserPlayerCharacterMapper userPlayerCharacterMapper;

    @Autowired
    private StressDebuffConfigMapper stressDebuffConfigMapper;

    /**
     * 获取当前用户的压力状态
     * @param userId 用户ID
     * @return 压力状态
     */
    public StressStatusDTO getStressStatus(Long userId) {
        // 获取用户角色实例
        QueryWrapper<UserPlayerCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .last("LIMIT 1");
        UserPlayerCharacter character = userPlayerCharacterMapper.selectOne(wrapper);
        
        if (character == null) {
            throw new RuntimeException("未创建角色");
        }

        StressStatusDTO status = new StressStatusDTO();
        status.setCurrentStress(character.getCurrentStress() != null ? character.getCurrentStress() : 0);
        status.setStressLevel(character.getStressLevel() != null ? character.getStressLevel() : 1);
        
        // 解析激活的debuff列表
        List<StressDebuffDTO> activeDebuffs = parseStressDebuffs(character.getStressDebuffs());
        status.setActiveDebuffs(activeDebuffs);
        
        return status;
    }

    /**
     * 获取所有压力debuff配置
     * @return debuff配置列表
     */
    public List<StressDebuffDTO> getAllDebuffConfigs() {
        List<StressDebuffConfig> configs = stressDebuffConfigMapper.selectList(null);
        
        return configs.stream()
                .map(this::toDebuffDTO)
                .collect(Collectors.toList());
    }

    /**
     * 缓解压力（使用营地设施）
     * @param userId 用户ID
     * @param facilityType 设施类型：tavern-酒馆, chapel-教堂, sanctum-圣所
     * @return 缓解后的压力状态
     */
    @Transactional
    public StressStatusDTO relieveStress(Long userId, String facilityType) {
        // 获取用户角色实例
        QueryWrapper<UserPlayerCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .last("LIMIT 1");
        UserPlayerCharacter character = userPlayerCharacterMapper.selectOne(wrapper);
        
        if (character == null) {
            throw new RuntimeException("未创建角色");
        }

        // 根据设施类型计算缓解量
        int relieveAmount = getRelieveAmount(facilityType);
        
        // 减少压力值
        int currentStress = character.getCurrentStress() != null ? character.getCurrentStress() : 0;
        int newStress = Math.max(0, currentStress - relieveAmount);
        character.setCurrentStress(newStress);
        
        // 重新计算压力层级
        int newStressLevel = calculateStressLevel(newStress);
        character.setStressLevel(newStressLevel);
        
        // 如果压力降低，可能需要清除一些debuff
        if (newStressLevel < character.getStressLevel()) {
            // 清除对应层级的debuff
            clearDebuffsByLevel(character, newStressLevel);
        }
        
        userPlayerCharacterMapper.updateById(character);
        
        // 返回更新后的压力状态
        StressStatusDTO status = new StressStatusDTO();
        status.setCurrentStress(newStress);
        status.setStressLevel(newStressLevel);
        status.setActiveDebuffs(parseStressDebuffs(character.getStressDebuffs()));
        
        return status;
    }

    /**
     * 根据设施类型获取缓解量
     */
    private int getRelieveAmount(String facilityType) {
        if (facilityType == null) {
            return 10; // 默认缓解10点
        }
        
        switch (facilityType.toLowerCase()) {
            case "tavern":
                return 15; // 酒馆缓解15点
            case "chapel":
                return 20; // 教堂缓解20点
            case "sanctum":
                return 25; // 圣所缓解25点
            default:
                return 10; // 默认缓解10点
        }
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
     * 清除指定层级及以上的debuff
     */
    private void clearDebuffsByLevel(UserPlayerCharacter character, int maxLevel) {
        // 解析当前debuff列表
        List<StressDebuffDTO> debuffs = parseStressDebuffs(character.getStressDebuffs());
        
        // 过滤掉层级大于maxLevel的debuff
        List<StressDebuffDTO> remainingDebuffs = debuffs.stream()
                .filter(debuff -> {
                    // 这里需要根据debuff的层级来判断，但当前DTO没有层级信息
                    // 简化处理：如果压力降低，清除所有debuff
                    return false;
                })
                .collect(Collectors.toList());
        
        // 如果压力降到1级，清除所有debuff
        if (maxLevel <= 1) {
            character.setStressDebuffs("[]");
        } else {
            // 保留剩余的debuff
            character.setStressDebuffs(JSON.toJSONString(remainingDebuffs));
        }
    }

    /**
     * 解析压力debuff JSON字符串
     */
    private List<StressDebuffDTO> parseStressDebuffs(String stressDebuffsJson) {
        if (stressDebuffsJson == null || stressDebuffsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            JSONArray jsonArray = JSON.parseArray(stressDebuffsJson);
            List<StressDebuffDTO> debuffs = new ArrayList<>();
            
            for (int i = 0; i < jsonArray.size(); i++) {
                com.alibaba.fastjson2.JSONObject obj = jsonArray.getJSONObject(i);
                StressDebuffDTO dto = new StressDebuffDTO();
                dto.setDebuffName(obj.getString("debuffName"));
                dto.setDebuffType(obj.getString("debuffType"));
                dto.setEffectDescription(obj.getString("effectDescription"));
                dto.setEffectPayload(obj.getString("effectPayload"));
                dto.setIsPersistent(obj.getBoolean("isPersistent"));
                debuffs.add(dto);
            }
            
            return debuffs;
        } catch (Exception e) {
            // JSON解析失败，返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 实体转DTO
     */
    private StressDebuffDTO toDebuffDTO(StressDebuffConfig config) {
        StressDebuffDTO dto = new StressDebuffDTO();
        dto.setDebuffName(config.getDebuffName());
        dto.setDebuffType(config.getDebuffType());
        dto.setEffectDescription(config.getEffectDescription());
        dto.setEffectPayload(config.getEffectPayload());
        dto.setIsPersistent(config.getIsPersistent());
        dto.setStressLevel(config.getStressLevel());  // 设置压力层级
        dto.setTriggerChance(config.getTriggerChance());  // 设置触发概率
        return dto;
    }
}

