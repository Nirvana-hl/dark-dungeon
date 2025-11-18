package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        return userCardCharacterMapper.selectByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserCardCharacterDTO> getDeployedCardCharacters(Long userId) {
        return userCardCharacterMapper.selectDeployedByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserCardCharacterDTO getUserCardCharacterById(Long id, Long userId) {
        QueryWrapper<UserCardCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
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
        userCardCharacterMapper.insert(entity);
        return toDTO(entity);
    }

    public UserCardCharacterDTO updateUserCardCharacter(Long id, Long userId, UserCardCharacterDTO dto) {
        QueryWrapper<UserCardCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
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

        userCardCharacterMapper.updateById(entity);
        return toDTO(entity);
    }

    public void deleteUserCardCharacter(Long id, Long userId) {
        QueryWrapper<UserCardCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
        userCardCharacterMapper.delete(wrapper);
    }

    private UserCardCharacterDTO toDTO(UserCardCharacter entity) {
        UserCardCharacterDTO dto = new UserCardCharacterDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}


