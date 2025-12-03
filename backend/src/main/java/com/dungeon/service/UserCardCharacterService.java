package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dungeon.dto.UserCardCharacterDTO;
import com.dungeon.entity.CardCharacter;
import com.dungeon.entity.UserCardCharacter;
import com.dungeon.mapper.CardCharacterMapper;
import com.dungeon.mapper.UserCardCharacterMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


