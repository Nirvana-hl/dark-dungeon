package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.Card;
import org.apache.ibatis.annotations.Mapper;

/**
 * 卡牌模板 Mapper
 */
@Mapper
public interface CardMapper extends BaseMapper<Card> {
}

