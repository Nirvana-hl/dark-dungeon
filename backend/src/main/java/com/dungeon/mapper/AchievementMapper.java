package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.Achievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成就 Mapper
 */
@Mapper
public interface AchievementMapper extends BaseMapper<Achievement> {
    
    /**
     * 根据分类查询成就
     * @param category 分类：progression-进度, mastery-精通, collection-收集, social-社交
     * @return 成就列表
     */
    List<Achievement> selectByCategory(@Param("category") String category);
}

