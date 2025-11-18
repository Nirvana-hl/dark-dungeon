package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.Dungeon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地牢模板 Mapper
 */
@Mapper
public interface DungeonMapper extends BaseMapper<Dungeon> {
    
    /**
     * 根据难度查询地牢
     * @param difficulty 难度：easy-简单, normal-普通, hard-困难, expert-专家
     * @return 地牢列表
     */
    List<Dungeon> selectByDifficulty(@Param("difficulty") String difficulty);
}

