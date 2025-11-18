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
     * 关联ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 敌人ID
     */
    private String enemyId;
    
    /**
     * 卡牌ID
     */
    private String cardId;
    
    /**
     * 权重（用于随机抽取）
     */
    private Integer weight;
}
