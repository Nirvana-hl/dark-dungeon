package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.EnemyCard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敌人卡组关联 Mapper
 * 使用 MyBatis Plus 的 BaseMapper，通过 LambdaQueryWrapper 进行条件查询
 */
@Mapper
public interface EnemyCardMapper extends BaseMapper<EnemyCard> {
    // 已移除 selectByEnemyId 方法，改用 MyBatis Plus 的 LambdaQueryWrapper
    // 在 Service 层使用：enemyCardMapper.selectList(new LambdaQueryWrapper<EnemyCard>().eq(EnemyCard::getEnemyId, enemyId))
}

