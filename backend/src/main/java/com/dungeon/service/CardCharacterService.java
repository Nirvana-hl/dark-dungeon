package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dungeon.dto.CardCharacterDTO;
import com.dungeon.dto.CardCharacterTraitDTO;
import com.dungeon.entity.CardCharacter;
import com.dungeon.entity.CardCharacterTrait;
import com.dungeon.mapper.CardCharacterMapper;
import com.dungeon.mapper.CardCharacterTraitMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 卡牌角色模板服务
 */
@Service
public class CardCharacterService {

    @Autowired
    private CardCharacterMapper cardCharacterMapper;

    @Autowired
    private CardCharacterTraitMapper cardCharacterTraitMapper;

    /**
     * 查询全部卡牌角色模板（默认只返回玩家角色）
     */
    public List<CardCharacterDTO> getAllCardCharacters() {
        LambdaQueryWrapper<CardCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CardCharacter::getCardType, "player");
        return toDTOList(cardCharacterMapper.selectList(wrapper));
    }
    
    /**
     * 根据卡牌类型查询（player或enemy）
     */
    public List<CardCharacterDTO> getByCardType(String cardType) {
        if (cardType == null || cardType.trim().isEmpty()) {
            return getAllCardCharacters(); // 默认返回玩家角色
        }
        LambdaQueryWrapper<CardCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CardCharacter::getCardType, cardType);
        return toDTOList(cardCharacterMapper.selectList(wrapper));
    }
    
    /**
     * 查询全部卡牌角色模板（包括玩家和敌人，用于特殊场景）
     */
    public List<CardCharacterDTO> getAllCardCharactersIncludeEnemy() {
        return toDTOList(cardCharacterMapper.selectList(null));
    }

    /**
     * 根据职业筛选（默认只返回玩家角色）
     */
    public List<CardCharacterDTO> getByClass(String classType) {
        if (classType == null || classType.trim().isEmpty()) {
            return getAllCardCharacters();
        }
        LambdaQueryWrapper<CardCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CardCharacter::getClassType, classType);
        wrapper.eq(CardCharacter::getCardType, "player");
        return toDTOList(cardCharacterMapper.selectList(wrapper));
    }

    /**
     * 根据阵营筛选（默认只返回玩家角色）
     */
    public List<CardCharacterDTO> getByFaction(String faction) {
        if (faction == null || faction.trim().isEmpty()) {
            return getAllCardCharacters();
        }
        LambdaQueryWrapper<CardCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CardCharacter::getFaction, faction);
        wrapper.eq(CardCharacter::getCardType, "player");
        return toDTOList(cardCharacterMapper.selectList(wrapper));
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
        BeanUtils.copyProperties(dto, entity, buildIgnoreProperties(dto, "id"));
        cardCharacterMapper.updateById(entity);
        return toDTO(entity);
    }

    /**
     * 删除卡牌角色模板
     */
    public void delete(Long id) {
        cardCharacterMapper.deleteById(id);
    }

    /**
     * 根据卡牌角色ID查询特性列表
     * @param cardCharacterId 卡牌角色ID
     * @return 特性列表
     */
    public List<CardCharacterTraitDTO> getTraitsByCardCharacterId(Long cardCharacterId) {
        if (cardCharacterId == null) {
            return List.of();
        }
        
        // 使用LambdaQueryWrapper查询
        LambdaQueryWrapper<CardCharacterTrait> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CardCharacterTrait::getCardCharacterId, cardCharacterId);
        List<CardCharacterTrait> traits = cardCharacterTraitMapper.selectList(wrapper);
        
        // 转换为DTO
        return traits.stream()
                .map(this::toTraitDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将CardCharacterTrait实体转换为DTO
     */
    private CardCharacterTraitDTO toTraitDTO(CardCharacterTrait entity) {
        CardCharacterTraitDTO dto = new CardCharacterTraitDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private CardCharacterDTO toDTO(CardCharacter entity) {
        CardCharacterDTO dto = new CardCharacterDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private List<CardCharacterDTO> toDTOList(List<CardCharacter> list) {
        return list.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 构造需要忽略复制的属性列表，默认忽略 null 值与额外指定字段
     */
    private String[] buildIgnoreProperties(CardCharacterDTO source, String... extraIgnore) {
        BeanWrapper wrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = wrapper.getPropertyDescriptors();
        Set<String> ignoreNames = new HashSet<>();
        if (pds != null) {
            for (PropertyDescriptor pd : pds) {
                Object srcValue = wrapper.getPropertyValue(pd.getName());
                if (srcValue == null) {
                    ignoreNames.add(pd.getName());
                    continue;
                }
                if (srcValue instanceof String && ((String) srcValue).trim().isEmpty()) {
                    ignoreNames.add(pd.getName());
                }
            }
        }
        if (extraIgnore != null) {
            for (String name : extraIgnore) {
                ignoreNames.add(name);
            }
        }
        return ignoreNames.toArray(new String[0]);
    }
}


