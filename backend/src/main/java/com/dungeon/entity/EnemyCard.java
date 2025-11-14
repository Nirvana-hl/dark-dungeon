package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敌方卡牌实体
 */
@Data
@TableName("enemy_cards")
public class EnemyCard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer stage; // 关卡数
    private String difficulty; // 普通/困难/噩梦
    private String name;
    private String type;
    private Integer attack;
    private Integer health;
    private String effect;
    private Integer uniquePlay; // 是否唯一出牌
    private LocalDateTime createdAt;
}

