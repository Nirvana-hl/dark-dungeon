package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.EnemyCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 敌人卡组关联 Mapper
 */
@Mapper
public interface EnemyCardMapper extends BaseMapper<EnemyCard> {
    
    /**
     * 根据敌人ID查询卡组
     * @param enemyId 敌人ID
     * @return 卡组列表
     */
    List<EnemyCard> selectByEnemyId(@Param("enemyId") String enemyId);
}

