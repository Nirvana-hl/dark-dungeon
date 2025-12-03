package com.dungeon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 背包道具 DTO（包含道具详细信息）
 */
@Data
public class InventoryItemDTO {
    private Long id;
    private Long userId;
    private Long itemId;
    private Integer quantity;
    private String bindStatus;
    private LocalDateTime lastUpdatedAt;
    
    // 道具详细信息（从item表关联查询）
    private String itemName;
    private String itemDescription;
    private String itemType;
    private String itemRarity;
    private String itemCode;
}

