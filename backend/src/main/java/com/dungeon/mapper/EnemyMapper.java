package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.Enemy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敌人模板 Mapper
 * 使用 MyBatis Plus 的 BaseMapper，通过 LambdaQueryWrapper 进行条件查询
 */
@Mapper
public interface EnemyMapper extends BaseMapper<Enemy> {
    // 已移除 selectByDifficulty 方法，改用 MyBatis Plus 的 LambdaQueryWrapper
    // 在 Service 层使用：enemyMapper.selectList(new LambdaQueryWrapper<Enemy>().eq(Enemy::getDifficulty, difficulty))
}

