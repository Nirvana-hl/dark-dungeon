package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.CharacterTraitDTO;
import com.dungeon.entity.CharacterTrait;
import com.dungeon.mapper.CharacterTraitMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色特性服务
 */
@Service
public class CharacterTraitService {

    @Autowired
    private CharacterTraitMapper characterTraitMapper;

    /**
     * 获取所有角色特性
     * 返回格式：{ traitKey: CharacterTraitDTO, ... }
     * 
     * @return 角色特性映射表
     */
    public Map<String, CharacterTraitDTO> getAllTraits() {
        QueryWrapper<CharacterTrait> wrapper = new QueryWrapper<>();
        List<CharacterTrait> traits = characterTraitMapper.selectList(wrapper);

        Map<String, CharacterTraitDTO> result = new HashMap<>();
        for (CharacterTrait trait : traits) {
            if (trait.getTraitKey() != null && !trait.getTraitKey().trim().isEmpty()) {
                CharacterTraitDTO dto = new CharacterTraitDTO();
                BeanUtils.copyProperties(trait, dto);
                result.put(trait.getTraitKey(), dto);
            }
        }

        return result;
    }
}

