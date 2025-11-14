package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.CharacterTrait;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色特性 Mapper
 */
@Mapper
public interface CharacterTraitMapper extends BaseMapper<CharacterTrait> {
}

