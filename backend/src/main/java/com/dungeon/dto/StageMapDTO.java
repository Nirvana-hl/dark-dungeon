package com.dungeon.dto;

import lombok.Data;

/**
 * 关卡地图DTO
 */
@Data
public class StageMapDTO {
    /**
     * 关卡编号
     */
    private Integer stageNumber;
    
    /**
     * 关卡名称
     */
    private String stageName;
    
    /**
     * 地图配置
     */
    private MapDTO map;
}

