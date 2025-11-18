package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.PlayerCharacter;
import org.apache.ibatis.annotations.Mapper;

/**
 * 玩家角色模板 Mapper
 */
@Mapper
public interface PlayerCharacterMapper extends BaseMapper<PlayerCharacter> {
}

