package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.StressDebuffConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 压力debuff配置 Mapper
 */
@Mapper
public interface StressDebuffConfigMapper extends BaseMapper<StressDebuffConfig> {
    
    /**
     * 根据压力层级查询debuff配置
     * @param stressLevel 压力层级（1-4）
     * @return debuff配置列表
     */
    List<StressDebuffConfig> selectByStressLevel(@Param("stressLevel") Integer stressLevel);
}

