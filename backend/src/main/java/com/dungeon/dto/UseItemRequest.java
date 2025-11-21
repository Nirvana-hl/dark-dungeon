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
     * 使用数量（默认为1）
     */
    private Integer quantity;
}

