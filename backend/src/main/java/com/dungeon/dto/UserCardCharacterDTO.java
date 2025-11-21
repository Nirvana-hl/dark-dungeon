package com.dungeon.dto;

import lombok.Data;

/**
 * 用户卡牌角色实例 DTO
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
}


