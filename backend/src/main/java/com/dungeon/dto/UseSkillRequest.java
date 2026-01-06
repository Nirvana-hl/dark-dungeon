package com.dungeon.dto;

import lombok.Data;

/**
 * 使用技能请求
 */
@Data
public class UseSkillRequest {
    /**
     * 技能ID
     */
    private Long skillId;
    
    /**
     * 当前法力值（用于验证是否足够）
     */
    private Integer currentMana;
}

