package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.CardCharacter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡牌角色模板 Mapper
 */
@Mapper
public interface CardCharacterMapper extends BaseMapper<CardCharacter> {
    
    /**
     * 根据职业查询卡牌角色
     * @param classType 职业
     * @return 卡牌角色列表
     */
    List<CardCharacter> selectByClass(@Param("classType") String classType);
    
    /**
     * 根据阵营查询卡牌角色
     * @param faction 阵营
     * @return 卡牌角色列表
     */
    List<CardCharacter> selectByFaction(@Param("faction") String faction);
}

