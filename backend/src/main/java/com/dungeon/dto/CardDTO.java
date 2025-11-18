package com.dungeon.dto;

import lombok.Data;

/**
 * 卡牌模板 DTO（法术/装备）
 */
@Data
public class CardDTO {
    private Long id;
    private String code;
    private String name;
    private String cardType;
    private String rarity;
    private String slotType;
    private Integer actionPointCost;
    private String statModifiers;
    private String effectPayload;
    private String campUnlockCondition;
    private String shopPrice;
    private String description;
}


