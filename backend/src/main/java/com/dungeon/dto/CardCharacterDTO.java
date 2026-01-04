package com.dungeon.dto;

import lombok.Data;

/**
 * 卡牌角色模板 DTO
 */
@Data
public class CardCharacterDTO {
    private Long id;
    private String code;
    private String name;
    private String classType;
    private String faction;
    private String rarity;
    private Integer baseHp;
    private Integer baseAttack;
    private Integer actionPointCost;
    private Integer baseStarLevel;
    private Integer maxStarLevel;
    private String starUpgradePayload;
    private String traits;
    private Integer shopPrice;
    private String lore;
    /**
     * 卡牌类型：player-玩家角色卡, enemy-敌人角色卡
     */
    private String cardType;
}


