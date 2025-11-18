package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.GameMetric;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 游戏指标 Mapper
 */
@Mapper
public interface GameMetricMapper extends BaseMapper<GameMetric> {
    
    /**
     * 根据指标类型查询
     * @param metricType 指标类型
     * @return 指标列表
     */
    List<GameMetric> selectByMetricType(@Param("metricType") String metricType);
    
    /**
     * 根据日期范围查询
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 指标列表
     */
    List<GameMetric> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

