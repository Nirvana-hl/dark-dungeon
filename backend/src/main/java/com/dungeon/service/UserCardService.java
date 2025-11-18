package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.UserCardDTO;
import com.dungeon.entity.UserCard;
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

    public List<UserCardDTO> getUserCards(Long userId) {
        return userCardMapper.selectByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserCardDTO> getEquippedUserCards(Long userId) {
        return userCardMapper.selectEquippedByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserCardDTO getUserCard(Long id, Long userId) {
        QueryWrapper<UserCard> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
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
        QueryWrapper<UserCard> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
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
        if (dto.getLoadoutId() != null) {
            entity.setLoadoutId(dto.getLoadoutId());
        }
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
        QueryWrapper<UserCard> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
        userCardMapper.delete(wrapper);
    }

    private UserCardDTO toDTO(UserCard entity) {
        UserCardDTO dto = new UserCardDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}


