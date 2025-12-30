package com.dungeon.dto;

import lombok.Data;

/**
 * 缓解压力请求DTO
 */
@Data
public class RelieveStressRequest {
    /**
     * 营地设施类型：tavern-酒馆, chapel-教堂, sanctum-圣所
     */
    private String facilityType;
}

