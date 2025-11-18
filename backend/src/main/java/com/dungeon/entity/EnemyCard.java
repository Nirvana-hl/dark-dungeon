package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 敌人卡组关联实体
 * 对应数据库表：enemy_cards
 */
@Data
@TableName("enemy_cards")
public class EnemyCard {
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
     * 卡牌ID
     */
    private Long cardId;
    
    /**
     * 权重（用于随机抽取）
     */
    private Integer weight;
}
