package com.dungeon.service;

import com.dungeon.dto.CardCharacterDTO;
import com.dungeon.entity.CardCharacter;
import com.dungeon.mapper.CardCharacterMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 卡牌角色模板服务
 */
@Service
public class CardCharacterService {

    @Autowired
    private CardCharacterMapper cardCharacterMapper;

    /**
     * 查询全部卡牌角色模板
     */
    public List<CardCharacterDTO> getAllCardCharacters() {
        List<CardCharacter> list = cardCharacterMapper.selectList(null);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 根据职业筛选
     */
    public List<CardCharacterDTO> getByClass(String classType) {
        if (classType == null) {
            return getAllCardCharacters();
        }
        return cardCharacterMapper.selectByClass(classType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据阵营筛选
     */
    public List<CardCharacterDTO> getByFaction(String faction) {
        if (faction == null) {
            return getAllCardCharacters();
        }
        return cardCharacterMapper.selectByFaction(faction).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询
     */
    public CardCharacterDTO getById(Long id) {
        CardCharacter entity = cardCharacterMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        return toDTO(entity);
    }

    /**
     * 创建卡牌角色模板
     */
    public CardCharacterDTO create(CardCharacterDTO dto) {
        CardCharacter entity = new CardCharacter();
        BeanUtils.copyProperties(dto, entity);
        cardCharacterMapper.insert(entity);
        return toDTO(entity);
    }

    /**
     * 更新卡牌角色模板
     */
    public CardCharacterDTO update(Long id, CardCharacterDTO dto) {
        CardCharacter entity = cardCharacterMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        BeanUtils.copyProperties(dto, entity, "id");
        cardCharacterMapper.updateById(entity);
        return toDTO(entity);
    }

    /**
     * 删除卡牌角色模板
     */
    public void delete(Long id) {
        cardCharacterMapper.deleteById(id);
    }

    private CardCharacterDTO toDTO(CardCharacter entity) {
        CardCharacterDTO dto = new CardCharacterDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}


