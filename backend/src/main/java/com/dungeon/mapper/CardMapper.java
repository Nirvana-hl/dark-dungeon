package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.Card;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡牌模板 Mapper
 */
@Mapper
public interface CardMapper extends BaseMapper<Card> {
    
    /**
     * 根据卡牌类型查询
     * @param cardType 卡牌类型：spell-法术, equipment-装备
     * @return 卡牌列表
     */
    List<Card> selectByCardType(@Param("cardType") String cardType);
    
    /**
     * 根据稀有度查询
     * @param rarity 稀有度
     * @return 卡牌列表
     */
    List<Card> selectByRarity(@Param("rarity") String rarity);
}

