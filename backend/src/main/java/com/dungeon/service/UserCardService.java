package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dungeon.dto.UserCardDTO;
import com.dungeon.entity.Card;
import com.dungeon.entity.UserCard;
import com.dungeon.mapper.CardMapper;
import com.dungeon.mapper.UserCardMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户卡牌（法术/装备）服务
 */
@Service
public class UserCardService {

    @Autowired
    private UserCardMapper userCardMapper;

    @Autowired
    private CardMapper cardMapper;

    public List<UserCardDTO> getUserCards(Long userId) {
        LambdaQueryWrapper<UserCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCard::getUserId, userId);
        return toDTOList(userCardMapper.selectList(wrapper));
    }

    public List<UserCardDTO> getEquippedUserCards(Long userId) {
        LambdaQueryWrapper<UserCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCard::getUserId, userId)
                .isNotNull(UserCard::getEquippedToUserCardCharacterId);
        return toDTOList(userCardMapper.selectList(wrapper));
    }

    /**
     * 根据卡组ID获取卡组中的卡牌
     * @param userId 用户ID
     * @param loadoutId 卡组ID（如果为null，则返回所有有loadoutId的卡牌）
     * @return 卡组中的卡牌列表
     */
    public List<UserCardDTO> getDeckCards(Long userId, Long loadoutId) {
        LambdaQueryWrapper<UserCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCard::getUserId, userId)
                .isNotNull(UserCard::getLoadoutId);
        if (loadoutId != null) {
            wrapper.eq(UserCard::getLoadoutId, loadoutId);
        }
        return toDTOList(userCardMapper.selectList(wrapper));
    }

    public UserCardDTO getUserCard(Long id, Long userId) {
        LambdaQueryWrapper<UserCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCard::getId, id)
                .eq(UserCard::getUserId, userId);
        UserCard entity = userCardMapper.selectOne(wrapper);
        if (entity == null) {
            return null;
        }
        return toDTO(entity);
    }

    public UserCardDTO createUserCard(Long userId, UserCardDTO dto) {
        UserCard entity = new UserCard();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        entity.setUserId(userId);
        if (entity.getQuantity() == null) {
            entity.setQuantity(1);
        }
        if (entity.getLevel() == null) {
            entity.setLevel(1);
        }
        entity.setAcquiredAt(LocalDateTime.now());
        userCardMapper.insert(entity);
        return toDTO(entity);
    }

    public UserCardDTO updateUserCard(Long id, Long userId, UserCardDTO dto) {
        LambdaQueryWrapper<UserCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCard::getId, id)
                .eq(UserCard::getUserId, userId);
        UserCard entity = userCardMapper.selectOne(wrapper);
        if (entity == null) {
            return null;
        }
        if (dto.getQuantity() != null) {
            entity.setQuantity(dto.getQuantity());
        }
        if (dto.getLevel() != null) {
            entity.setLevel(dto.getLevel());
        }
        // loadoutId 可以设置为 null（移出卡组）或具体值（加入卡组）
        // 使用 Optional 或者直接判断，如果 DTO 中包含 loadoutId 字段（即使是 null），也要更新
        // 这里简化处理：如果 DTO 中有 loadoutId 字段，就更新（包括 null）
        // 由于前端会明确传递 loadoutId，我们可以直接设置
        entity.setLoadoutId(dto.getLoadoutId());
        if (dto.getEquippedToUserCardCharacterId() != null) {
            entity.setEquippedToUserCardCharacterId(dto.getEquippedToUserCardCharacterId());
        }
        if (dto.getAcquiredSource() != null) {
            entity.setAcquiredSource(dto.getAcquiredSource());
        }
        if (dto.getLastUsedAt() != null) {
            entity.setLastUsedAt(dto.getLastUsedAt());
        }
        userCardMapper.updateById(entity);
        return toDTO(entity);
    }

    public void deleteUserCard(Long id, Long userId) {
        LambdaQueryWrapper<UserCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCard::getId, id)
                .eq(UserCard::getUserId, userId);
        userCardMapper.delete(wrapper);
    }

    private UserCardDTO toDTO(UserCard entity) {
        UserCardDTO dto = new UserCardDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 加载卡牌模板的详细信息
        if (entity.getCardId() != null) {
            Card card = cardMapper.selectById(entity.getCardId());
            if (card != null) {
                // 将卡牌信息添加到DTO中
                dto.setCardName(card.getName());
                dto.setCardDescription(card.getDescription());
                dto.setCardType(card.getCardType());
                dto.setCardRarity(card.getRarity());
                dto.setCardCode(card.getCode());
                dto.setStatModifiers(card.getStatModifiers());
                // Card实体中没有manaCost、attack、hp字段，这些信息可能在statModifiers中
                // 如果需要，可以从statModifiers JSON中解析
                dto.setManaCost(card.getActionPointCost()); // 使用actionPointCost作为manaCost
                dto.setAttack(null); // Card实体中没有attack字段
                dto.setHp(null); // Card实体中没有hp字段
            }
        }
        
        return dto;
    }

    private List<UserCardDTO> toDTOList(List<UserCard> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}


