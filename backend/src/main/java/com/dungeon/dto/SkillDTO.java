package com.dungeon.dto;

import lombok.Data;

/**
 * 技能模板DTO
 * 用于返回技能树信息
 */
@Data
public class SkillDTO {
    /**
     * 技能模板ID
     */
    private Long id;
    
    /**
     * 策划短码
     */
    private String code;
    
    /**
     * 对应的职业短码
     */
    private String playerCharacterCode;
    
    /**
     * 技能名称
     */
    private String name;
    
    /**
     * 技能描述
     */
    private String description;
    
    /**
     * 技能效果配置（JSON格式）
     */
    private String effectPayload;
    
    /**
     * 解锁所需职业等级
     */
    private Integer requiredLevel;
    
    /**
     * 技能树坐标（JSON格式：{row: INT, column: INT}）
     */
    private String positionInTree;
    
    /**
     * 前置技能依赖（JSON格式：["skill_code1", "skill_code2"]）
     */
    private String unlockPath;
    
    /**
     * 是否已解锁（用于技能树展示）
     */
    private Boolean isUnlocked;
    
    /**
     * 是否可以解锁（用于技能树展示，检查等级和前置技能）
     */
    private Boolean canUnlock;
}

