package com.dungeon.dto;

import lombok.Data;

/**
 * 使用道具请求DTO
 */
@Data
public class UseItemRequest {
    /**
     * 道具ID（items表的id）
     */
    private Long itemId;
    
    /**
     * 背包记录ID（inventory表的id）- 前端使用此字段
     */
    private Long inventoryId;
    
    /**
     * 使用数量（默认为1）
     */
    private Integer quantity;
}

