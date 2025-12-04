package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.EnemyCardCharacter;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敌人角色卡关联 Mapper
 * 使用 MyBatis Plus 的 BaseMapper，通过 LambdaQueryWrapper 进行条件查询
 */
@Mapper
public interface EnemyCardCharacterMapper extends BaseMapper<EnemyCardCharacter> {
    // 在 Service 层使用：enemyCardCharacterMapper.selectList(new LambdaQueryWrapper<EnemyCardCharacter>().eq(EnemyCardCharacter::getEnemyId, enemyId))
}

