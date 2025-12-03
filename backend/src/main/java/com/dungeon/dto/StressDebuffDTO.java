package com.dungeon.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 压力debuff DTO
 */
@Data
public class StressDebuffDTO {
    private String debuffName;
    private String debuffType;
    private String effectDescription;
    private String effectPayload;
    private Boolean isPersistent;
    private Integer stressLevel;  // 压力层级（1-4）
    private BigDecimal triggerChance;  // 触发概率
}

