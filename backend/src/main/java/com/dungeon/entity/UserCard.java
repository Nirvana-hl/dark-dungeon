package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户手牌实例实体
 * 对应数据库表：user_cards
 */
@Data
@TableName("user_cards")
public class UserCard {
    /**
     * 用户卡牌ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 卡牌模板ID
     */
    private String cardId;
    
    /**
     * 持有数量（角色卡一般为1）
     */
    private Integer quantity;
    
    /**
     * 卡牌强化等级（用于法术/装备强化）
     */
    private Integer level;
    
    /**
     * 牌组方案标识（用于多套构筑）
     */
    private String loadoutId;
    
    /**
     * 装备归属的角色卡实例ID（法术为空）
     */
    private String equippedToUserCardCharacterId;
    
    /**
     * 获得时间
     */
    private LocalDateTime acquiredAt;
    
    /**
     * 掉落或购买来源
     */
    private String acquiredSource;
    
    /**
     * 最近使用时间（AI推荐参考）
     */
    private LocalDateTime lastUsedAt;
}

