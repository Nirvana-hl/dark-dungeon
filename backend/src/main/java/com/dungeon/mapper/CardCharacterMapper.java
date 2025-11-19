package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.CardCharacter;
import org.apache.ibatis.annotations.Mapper;
/**
 * 卡牌角色模板 Mapper
 */
@Mapper
public interface CardCharacterMapper extends BaseMapper<CardCharacter> {
}

