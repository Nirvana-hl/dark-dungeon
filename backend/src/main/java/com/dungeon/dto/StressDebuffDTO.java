package com.dungeon.dto;

import lombok.Data;

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
}

