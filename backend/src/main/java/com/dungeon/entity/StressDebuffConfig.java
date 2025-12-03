package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 压力debuff配置实体
 * 对应数据库表：stress_debuff_configs
 */
@Data
@TableName("stress_debuff_configs")
public class StressDebuffConfig {
    /**
     * debuff配置ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 触发压力层级（1-4）
     * 数据库字段可能是 stress_level（下划线）或 stressLevel（驼峰）
     */
    @TableField(value = "stress_level", exist = true)
    private Integer stressLevel;
    
    /**
     * debuff名称
     */
    private String debuffName;
    
    /**
     * debuff类型：mental-精神, combat-战斗, behavioral-行为
     */
    private String debuffType;
    
    /**
     * 效果描述
     */
    private String effectDescription;
    
    /**
     * 触发概率（0.00-1.00）
     */
    private BigDecimal triggerChance;
    
    /**
     * 效果参数配置（JSON格式）
     */
    private String effectPayload;
    
    /**
     * 是否为持续效果
     */
    private Boolean isPersistent;
}

