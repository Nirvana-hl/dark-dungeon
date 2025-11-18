package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 敌人模板实体
 * 对应数据库表：enemies
 */
@Data
@TableName("enemies")
public class Enemy {
    /**
     * 敌人ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 敌人名称
     */
    private String name;
    
    /**
     * 难度：easy-简单, normal-普通, hard-困难, boss-首领
     */
    private String difficulty;
    
    /**
     * 基础属性（JSON格式）
     */
    private String baseStats;
    
    /**
     * 行为脚本（JSON格式）
     */
    private String behaviorScript;
}

