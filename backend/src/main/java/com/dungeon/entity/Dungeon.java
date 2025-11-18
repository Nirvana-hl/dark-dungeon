package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 地牢模板实体
 * 对应数据库表：dungeons
 */
@Data
@TableName("dungeons")
public class Dungeon {
    /**
     * 地牢ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 地牢名称
     */
    private String name;
    
    /**
     * 难度等级：easy-简单, normal-普通, hard-困难, expert-专家
     */
    private String difficulty;
    
    /**
     * 推荐卡牌（JSON格式）
     */
    private String recommendedCards;
    
    /**
     * 地牢主题（如：森林、洞穴、废墟等）
     */
    private String theme;
    
    /**
     * 地牢描述
     */
    private String description;
}

