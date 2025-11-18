package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 游戏指标实体
 * 对应数据库表：game_metrics
 */
@Data
@TableName("game_metrics")
public class GameMetric {
    /**
     * 指标ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 指标类型
     */
    private String metricType;
    
    /**
     * 统计日期
     */
    private LocalDate date;
    
    /**
     * 指标值
     */
    private Long value;
    
    /**
     * 维度载荷（JSON格式）
     */
    private String dimensionPayload;
}

