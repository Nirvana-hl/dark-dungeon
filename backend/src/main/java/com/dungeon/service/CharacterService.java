package com.dungeon.service;

import com.dungeon.dto.CharacterDTO;
import com.dungeon.entity.Character;
import com.dungeon.mapper.CharacterMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务
 */
@Service
public class CharacterService {

    @Autowired
    private CharacterMapper characterMapper;

    /**
     * 获取用户的所有角色
     */
    public List<CharacterDTO> getCharactersByUserId(Long userId) {
        List<Character> characters = characterMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Character>()
                .eq("user_id", userId)
                .orderByAsc("created_at")
        );
        return characters.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 创建角色
     */
    public CharacterDTO createCharacter(Long userId, CharacterDTO dto) {
        Character character = new Character();
        character.setUserId(userId);
        character.setName(dto.getName() != null ? dto.getName() : "新角色");
        character.setClassType(dto.getClassType() != null ? dto.getClassType() : "战士");
        character.setLevel(1);
        character.setStars(dto.getStars() != null ? dto.getStars() : 1);
        character.setHp(dto.getHp() != null ? dto.getHp() : 60);
        character.setMp(dto.getMp() != null ? dto.getMp() : 30);
        character.setStress(0);
        character.setStatus("idle");
        character.setCreatedAt(LocalDateTime.now());
        character.setUpdatedAt(LocalDateTime.now());
        character.setDeleted(0);
        
        characterMapper.insert(character);
        return toDTO(character);
    }

    /**
     * 删除角色
     */
    public void deleteCharacter(Long characterId, Long userId) {
        Character character = characterMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Character>()
                .eq("id", characterId)
                .eq("user_id", userId)
        );
        if (character != null) {
            characterMapper.deleteById(characterId);
        }
    }

    /**
     * 角色升星
     */
    public CharacterDTO starUp(Long characterId, Long userId) {
        Character character = characterMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Character>()
                .eq("id", characterId)
                .eq("user_id", userId)
        );
        if (character == null) {
            throw new RuntimeException("角色不存在");
        }
        if (character.getStars() >= 5) {
            throw new RuntimeException("角色已达到最高星级");
        }
        
        character.setStars(character.getStars() + 1);
        character.setHp(character.getHp() + 10);
        character.setMp(character.getMp() + 5);
        character.setUpdatedAt(LocalDateTime.now());
        characterMapper.updateById(character);
        
        return toDTO(character);
    }

    /**
     * 实体转DTO
     */
    private CharacterDTO toDTO(Character character) {
        CharacterDTO dto = new CharacterDTO();
        BeanUtils.copyProperties(character, dto);
        dto.setClassType(character.getClassType());
        return dto;
    }
}

