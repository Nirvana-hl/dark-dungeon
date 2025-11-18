package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.CardCharacterTrait;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡牌角色特性 Mapper
 */
@Mapper
public interface CardCharacterTraitMapper extends BaseMapper<CardCharacterTrait> {
    
    /**
     * 根据卡牌角色ID查询特性列表
     * @param cardCharacterId 卡牌角色ID
     * @return 特性列表
     */
    List<CardCharacterTrait> selectByCardCharacterId(@Param("cardCharacterId") String cardCharacterId);
}

