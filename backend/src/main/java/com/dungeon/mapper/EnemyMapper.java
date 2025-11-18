package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.Enemy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 敌人模板 Mapper
 */
@Mapper
public interface EnemyMapper extends BaseMapper<Enemy> {
    
    /**
     * 根据难度查询敌人
     * @param difficulty 难度：easy-简单, normal-普通, hard-困难, boss-首领
     * @return 敌人列表
     */
    List<Enemy> selectByDifficulty(@Param("difficulty") String difficulty);
}

