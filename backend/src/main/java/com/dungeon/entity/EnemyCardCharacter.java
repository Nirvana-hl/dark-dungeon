package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 敌人角色卡关联实体
 * 对应数据库表：enemy_card_characters
 */
@Data
@TableName("enemy_card_characters")
public class EnemyCardCharacter {
    /**
     * 关联ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 敌人ID
     */
    private Long enemyId;
    
    /**
     * 角色卡ID
     */
    private Long cardCharacterId;
    
    /**
     * 权重（用于随机抽取）
     */
    private Integer weight;
}

