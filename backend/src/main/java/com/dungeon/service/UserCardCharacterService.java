package com.dungeon.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dungeon.dto.UserCardCharacterDTO;
import com.dungeon.entity.CardCharacter;
import com.dungeon.entity.UserCardCharacter;
import com.dungeon.mapper.CardCharacterMapper;
import com.dungeon.mapper.UserCardCharacterMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户卡牌角色实例服务
 */
@Service
public class UserCardCharacterService {

    @Autowired
    private UserCardCharacterMapper userCardCharacterMapper;

    @Autowired
    private CardCharacterMapper cardCharacterMapper;

    public List<UserCardCharacterDTO> getUserCardCharacters(Long userId) {
        LambdaQueryWrapper<UserCardCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCardCharacter::getUserId, userId);
        return toDTOList(userCardCharacterMapper.selectList(wrapper));
    }

    public List<UserCardCharacterDTO> getDeployedCardCharacters(Long userId) {
        LambdaQueryWrapper<UserCardCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCardCharacter::getUserId, userId)
                .eq(UserCardCharacter::getIsDeployed, true);
        return toDTOList(userCardCharacterMapper.selectList(wrapper));
    }

    public UserCardCharacterDTO getUserCardCharacterById(Long id, Long userId) {
        LambdaQueryWrapper<UserCardCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCardCharacter::getId, id)
                .eq(UserCardCharacter::getUserId, userId);
        UserCardCharacter entity = userCardCharacterMapper.selectOne(wrapper);
        if (entity == null) {
            return null;
        }
        return toDTO(entity);
    }

    public UserCardCharacterDTO createUserCardCharacter(Long userId, Long cardCharacterId) {
        CardCharacter template = cardCharacterMapper.selectById(cardCharacterId);
        if (template == null) {
            throw new RuntimeException("卡牌角色模板不存在");
        }
        UserCardCharacter entity = new UserCardCharacter();
        entity.setUserId(userId);
        entity.setCardCharacterId(cardCharacterId);
        entity.setCurrentHp(template.getBaseHp());
        entity.setCurrentArmor(0);
        entity.setIsDeployed(false);
        entity.setDeployedRound(0);
        entity.setCurrentStarLevel(template.getBaseStarLevel() != null ? template.getBaseStarLevel() : 1);
        entity.setQuantity(1); // 默认数量为1
        userCardCharacterMapper.insert(entity);
        return toDTO(entity);
    }

    public UserCardCharacterDTO updateUserCardCharacter(Long id, Long userId, UserCardCharacterDTO dto) {
        LambdaQueryWrapper<UserCardCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCardCharacter::getId, id)
                .eq(UserCardCharacter::getUserId, userId);
        UserCardCharacter entity = userCardCharacterMapper.selectOne(wrapper);
        if (entity == null) {
            return null;
        }

        if (dto.getCurrentHp() != null) {
            entity.setCurrentHp(dto.getCurrentHp());
        }
        if (dto.getCurrentArmor() != null) {
            entity.setCurrentArmor(dto.getCurrentArmor());
        }
        if (dto.getIsDeployed() != null) {
            entity.setIsDeployed(dto.getIsDeployed());
        }
        if (dto.getDeployedRound() != null) {
            entity.setDeployedRound(dto.getDeployedRound());
        }
        if (dto.getCurrentStarLevel() != null) {
            entity.setCurrentStarLevel(dto.getCurrentStarLevel());
        }
        if (dto.getQuantity() != null) {
            entity.setQuantity(dto.getQuantity());
        }

        userCardCharacterMapper.updateById(entity);
        return toDTO(entity);
    }

    public void deleteUserCardCharacter(Long id, Long userId) {
        LambdaQueryWrapper<UserCardCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCardCharacter::getId, id)
                .eq(UserCardCharacter::getUserId, userId);
        userCardCharacterMapper.delete(wrapper);
    }

    /**
     * 升星功能
     * 消耗3个当前星级的角色，创建一个新的更高星级的角色记录
     * 
     * @param userId 用户ID
     * @param userCardCharacterId 要升星的角色记录ID
     * @return 升星后的角色记录（新的更高星级记录）
     */
    @Transactional
    public UserCardCharacterDTO upgradeStarLevel(Long userId, Long userCardCharacterId) {
        // 1. 查询当前角色记录
        LambdaQueryWrapper<UserCardCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCardCharacter::getId, userCardCharacterId)
               .eq(UserCardCharacter::getUserId, userId);
        UserCardCharacter current = userCardCharacterMapper.selectOne(wrapper);
        
        if (current == null) {
            throw new RuntimeException("角色不存在或不属于当前用户");
        }
        
        // 2. 检查数量是否足够（需要3个）
        if (current.getQuantity() == null || current.getQuantity() < 3) {
            throw new RuntimeException("角色数量不足，需要3个才能升星");
        }
        
        // 3. 查询角色模板
        CardCharacter template = cardCharacterMapper.selectById(current.getCardCharacterId());
        if (template == null) {
            throw new RuntimeException("角色模板不存在");
        }
        
        // 4. 检查是否已达到最大星级
        int maxStarLevel = template.getMaxStarLevel() != null ? template.getMaxStarLevel() : 5;
        if (current.getCurrentStarLevel() >= maxStarLevel) {
            throw new RuntimeException("已达到最大星级 " + maxStarLevel);
        }
        
        // 5. 消耗3个当前星级的角色
        current.setQuantity(current.getQuantity() - 3);
        if (current.getQuantity() <= 0) {
            // 如果数量为0，删除记录
            userCardCharacterMapper.deleteById(userCardCharacterId);
        } else {
            userCardCharacterMapper.updateById(current);
        }
        
        // 6. 创建新的更高星级角色记录
        int newStarLevel = current.getCurrentStarLevel() + 1;
        
        // 查询是否已有该星级的记录
        LambdaQueryWrapper<UserCardCharacter> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(UserCardCharacter::getUserId, userId)
                      .eq(UserCardCharacter::getCardCharacterId, current.getCardCharacterId())
                      .eq(UserCardCharacter::getCurrentStarLevel, newStarLevel);
        UserCardCharacter existingHigher = userCardCharacterMapper.selectOne(existingWrapper);
        
        if (existingHigher != null) {
            // 如果已有该星级记录，增加数量
            existingHigher.setQuantity(existingHigher.getQuantity() + 1);
            userCardCharacterMapper.updateById(existingHigher);
            return toDTO(existingHigher);
        } else {
            // 创建新的更高星级记录
            UserCardCharacter newCharacter = new UserCardCharacter();
            newCharacter.setUserId(userId);
            newCharacter.setCardCharacterId(current.getCardCharacterId());
            newCharacter.setCurrentStarLevel(newStarLevel);
            newCharacter.setQuantity(1);
            newCharacter.setIsDeployed(false);
            newCharacter.setDeployedRound(0);
            newCharacter.setCurrentArmor(0);
            
            // 根据 star_upgrade_payload 计算属性增幅
            applyStarUpgrade(newCharacter, template, newStarLevel);
            
            userCardCharacterMapper.insert(newCharacter);
            return toDTO(newCharacter);
        }
    }

    /**
     * 应用星级提升带来的属性增幅
     * 根据 star_upgrade_payload 计算属性增幅
     */
    /**
     * 应用星级提升带来的属性增幅
     * 根据 star_upgrade_payload 计算属性增幅
     * 注意：攻击力增幅在战斗时根据星级动态计算，这里只设置血量
     */
    private void applyStarUpgrade(UserCardCharacter character, CardCharacter template, int starLevel) {
        // 基础属性（从模板获取）
        int baseHp = template.getBaseHp() != null ? template.getBaseHp() : 0;
        
        // 解析 star_upgrade_payload
        if (StringUtils.hasText(template.getStarUpgradePayload())) {
            try {
                JSONObject upgradePayload = JSON.parseObject(template.getStarUpgradePayload());
                
                // 计算属性增幅（每升一级增加的属性）
                JSONObject stats = upgradePayload.getJSONObject("stats");
                if (stats != null) {
                    // 计算从1星到当前星级的累计增幅
                    int hpBonus = 0;
                    
                    // 从2星开始，每升一级增加属性
                    for (int i = 2; i <= starLevel; i++) {
                        hpBonus += stats.getIntValue("hp", 0);
                    }
                    
                    character.setCurrentHp(baseHp + hpBonus);
                    // 注意：攻击力增幅在战斗时根据星级动态计算，不需要在这里存储
                } else {
                    character.setCurrentHp(baseHp);
                }
            } catch (Exception e) {
                // 解析失败，使用基础属性
                character.setCurrentHp(baseHp);
            }
        } else {
            // 如果没有配置，使用基础属性
            character.setCurrentHp(baseHp);
        }
        
        // 特性增幅在战斗时根据星级动态计算，不需要在这里存储
    }

    /**
     * 部署/撤下卡牌角色
     * @param userId 用户ID
     * @param userCardCharacterId 卡牌角色ID
     * @param deploy 是否部署
     * @return 更新后的卡牌角色
     */
    public UserCardCharacterDTO deployCardCharacter(Long userId, Long userCardCharacterId, Boolean deploy) {
        LambdaQueryWrapper<UserCardCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCardCharacter::getId, userCardCharacterId)
                .eq(UserCardCharacter::getUserId, userId);
        UserCardCharacter entity = userCardCharacterMapper.selectOne(wrapper);
        
        if (entity == null) {
            throw new RuntimeException("卡牌角色不存在或不属于当前用户");
        }
        
        entity.setIsDeployed(deploy);
        if (deploy) {
            // 部署时记录轮次（如果需要）
            entity.setDeployedRound(0);
        } else {
            // 撤下时重置轮次
            entity.setDeployedRound(0);
        }
        
        userCardCharacterMapper.updateById(entity);
        return toDTO(entity);
    }

    private UserCardCharacterDTO toDTO(UserCardCharacter entity) {
        UserCardCharacterDTO dto = new UserCardCharacterDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 确保 quantity 字段有值（至少为1）
        if (dto.getQuantity() == null || dto.getQuantity() < 1) {
            dto.setQuantity(1);
        }
        
        // 加载角色模板的详细信息
        if (entity.getCardCharacterId() != null) {
            CardCharacter character = cardCharacterMapper.selectById(entity.getCardCharacterId());
            if (character != null) {
                // 将角色信息添加到DTO中
                dto.setCharacterName(character.getName());
                // CardCharacter实体使用lore作为description
                dto.setCharacterDescription(character.getLore() != null ? character.getLore() : "");
                dto.setCharacterRarity(character.getRarity());
                dto.setCharacterClassType(character.getClassType());
                dto.setCharacterCode(character.getCode());
                dto.setBaseHp(character.getBaseHp());
                dto.setBaseAttack(character.getBaseAttack());
                // CardCharacter实体没有baseArmor字段，使用0
                dto.setBaseArmor(0);
            }
        }
        
        return dto;
    }

    private List<UserCardCharacterDTO> toDTOList(List<UserCardCharacter> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}


