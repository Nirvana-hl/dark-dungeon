package com.dungeon.dto;

import lombok.Data;

/**
 * 用户卡牌角色实例 DTO（包含角色详细信息）
 */
@Data
public class UserCardCharacterDTO {
    private Long id;
    private Long userId;
    private Long cardCharacterId;
    private Integer currentHp;
    private Integer currentArmor;
    private Boolean isDeployed;
    private Integer deployedRound;
    private Integer currentStarLevel;
    
    /**
     * 拥有数量（用于升星）
     */
    private Integer quantity;
    
    // 角色详细信息（从card_character表关联查询）
    private String characterName;
    private String characterDescription;
    private String characterRarity;
    private String characterClassType;
    private String characterCode;
    private Integer baseHp;
    private Integer baseAttack;
    private Integer baseArmor;
}


