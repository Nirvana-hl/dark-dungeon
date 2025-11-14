package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.EnemyCard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敌方卡牌 Mapper
 */
@Mapper
public interface EnemyCardMapper extends BaseMapper<EnemyCard> {
}

