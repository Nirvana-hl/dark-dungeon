package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 玩家角色模板实体
 * 对应数据库表：player_characters
 */
@Data
@TableName("player_characters")
public class PlayerCharacter {
    /**
     * 玩家角色模板ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 策划用短码
     */
    private String code;
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 基础血量
     */
    private Integer baseHp;
    
    /**
     * 每级增加的血量
     */
    private Integer hpPerLevel;
    
    /**
     * 背景故事
     */
    private String lore;
}

