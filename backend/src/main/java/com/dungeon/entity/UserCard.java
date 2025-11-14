package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户卡牌实体
 */
@Data
@TableName("user_cards")
public class UserCard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String type; // character/spell/equipment
    private Integer quantity;
    private Integer attack;
    private Integer health;
    private String effect; // fireball3/teamBuffAtk1等
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

