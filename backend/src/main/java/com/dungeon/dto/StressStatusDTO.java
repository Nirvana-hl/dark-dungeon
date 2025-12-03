package com.dungeon.dto;

import lombok.Data;

import java.util.List;

/**
 * 压力状态DTO
 */
@Data
public class StressStatusDTO {
    /**
     * 当前压力值
     */
    private Integer currentStress;
    
    /**
     * 压力层级（1-4）
     */
    private Integer stressLevel;
    
    /**
     * 激活的debuff列表
     */
    private List<StressDebuffDTO> activeDebuffs;
}

