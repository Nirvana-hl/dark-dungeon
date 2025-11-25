package com.dungeon.dto;

import lombok.Data;

/**
 * 地牢探索操作请求
 */
@Data
public class ExploreRequest {
    /**
     * 操作类型：explore/move/event
     */
    private String action = "explore";
    
    /**
     * 目标房间ID（移动探索时使用）
     */
    private Integer targetRoomId;
    
    /**
     * 选择项（事件选择时使用）
     */
    private String choice;
}

