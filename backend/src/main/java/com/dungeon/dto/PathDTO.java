package com.dungeon.dto;

import lombok.Data;

/**
 * 路径DTO
 */
@Data
public class PathDTO {
    /**
     * 起始房间ID
     */
    private Integer from;
    
    /**
     * 目标房间ID
     */
    private Integer to;
}

