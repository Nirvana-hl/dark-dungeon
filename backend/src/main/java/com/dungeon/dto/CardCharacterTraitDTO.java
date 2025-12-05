package com.dungeon.dto;

import lombok.Data;

/**
 * 卡牌角色特性DTO
 * 用于返回卡牌角色的特性信息，包括效果参数和星级缩放配置
 */
@Data
public class CardCharacterTraitDTO {
    /**
     * 特性ID
     */
    private Long id;
    
    /**
     * 卡牌角色模板ID
     */
    private Long cardCharacterId;
    
    /**
     * 特性名称（如：星辉祝福、钢铁护盾）
     */
    private String name;
    
    /**
     * 特性类型：positive-正面, negative-负面, neutral-中性
     */
    private String type;
    
    /**
     * 基础效果参数（JSON格式字符串）
     * 例如：{"heal_allies": 2} 或 {"armor_bonus": 3}
     */
    private String effectPayload;
    
    /**
     * 星级缩放配置（JSON格式字符串）
     * 例如：{"2": {"heal_allies": 3}, "3": {"heal_allies": 4}, "4": {"heal_allies": 5}}
     */
    private String scalingPayload;
    
    /**
     * 特性描述
     */
    private String description;
}

