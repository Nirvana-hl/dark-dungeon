package com.dungeon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户手牌 DTO（包含卡牌详细信息）
 */
@Data
public class UserCardDTO {
    private Long id;
    private Long userId;
    private Long cardId;
    private Integer quantity;
    private Integer level;
    private Long loadoutId;
    private Long equippedToUserCardCharacterId;
    private LocalDateTime acquiredAt;
    private String acquiredSource;
    private LocalDateTime lastUsedAt;
    
    // 卡牌详细信息（从card表关联查询）
    private String cardName;
    private String cardDescription;
    private String cardType;
    private String cardRarity;
    private String cardCode;
    private Integer manaCost;
    private Integer attack;
    private Integer hp;
    /**
     * 属性变动（从 cards.statModifiers 透传），JSON格式：
     * 例如：{"attack":1,"hp":3,"defense":2}
     */
    private String statModifiers;
}


