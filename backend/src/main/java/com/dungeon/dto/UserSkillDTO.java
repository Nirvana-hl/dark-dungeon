package com.dungeon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户技能实例DTO
 * 用于返回用户已解锁的技能列表
 */
@Data
public class UserSkillDTO {
    /**
     * 技能实例ID
     */
    private Long id;
    
    /**
     * 技能模板ID
     */
    private Long skillId;
    
    /**
     * 技能代码
     */
    private String skillCode;
    
    /**
     * 技能名称
     */
    private String skillName;
    
    /**
     * 技能描述
     */
    private String skillDescription;
    
    /**
     * 解锁时间
     */
    private LocalDateTime unlockedAt;
}

