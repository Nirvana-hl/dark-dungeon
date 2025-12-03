package com.dungeon.dto;

import lombok.Data;

/**
 * 角色特性DTO
 */
@Data
public class CharacterTraitDTO {
    /**
     * 特性键（如：priest_bless, shield_guard）
     */
    private String traitKey;
    
    /**
     * 基础威力
     */
    private Integer basePower;
    
    /**
     * 每星级增加的威力
     */
    private Integer powerPerStar;
    
    /**
     * 特性描述
     */
    private String description;
}

