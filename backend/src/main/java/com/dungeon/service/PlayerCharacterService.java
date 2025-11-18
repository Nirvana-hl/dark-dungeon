package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.PlayerCharacterDTO;
import com.dungeon.entity.PlayerCharacter;
import com.dungeon.mapper.PlayerCharacterMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 玩家角色模板服务
 */
@Service
public class PlayerCharacterService {

    @Autowired
    private PlayerCharacterMapper playerCharacterMapper;

    /**
     * 获取所有玩家角色模板
     */
    public List<PlayerCharacterDTO> getAllPlayerCharacters() {
        List<PlayerCharacter> characters = playerCharacterMapper.selectList(null);
        return characters.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取玩家角色模板
     */
    public PlayerCharacterDTO getPlayerCharacterById(Long id) {
        PlayerCharacter character = playerCharacterMapper.selectById(id);
        return character != null ? toDTO(character) : null;
    }

    /**
     * 根据代码获取玩家角色模板
     */
    public PlayerCharacterDTO getPlayerCharacterByCode(String code) {
        QueryWrapper<PlayerCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("code", code);
        PlayerCharacter character = playerCharacterMapper.selectOne(wrapper);
        return character != null ? toDTO(character) : null;
    }

    /**
     * 实体转DTO
     */
    private PlayerCharacterDTO toDTO(PlayerCharacter character) {
        PlayerCharacterDTO dto = new PlayerCharacterDTO();
        BeanUtils.copyProperties(character, dto);
        return dto;
    }
}

