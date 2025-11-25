package com.dungeon.dto;

import lombok.Data;

/**
 * 房间DTO
 */
@Data
public class RoomDTO {
    /**
     * 房间ID
     */
    private Integer id;
    
    /**
     * 房间类型：start/normal/event/boss/end
     */
    private String type;
    
    /**
     * 房间名称
     */
    private String name;
    
    /**
     * X坐标（可选，前端可自动计算）
     */
    private Integer x;
    
    /**
     * Y坐标（可选，前端可自动计算）
     */
    private Integer y;
    
    /**
     * 房间描述
     */
    private String description;
}

