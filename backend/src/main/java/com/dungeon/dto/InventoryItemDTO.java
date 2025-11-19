package com.dungeon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 背包道具 DTO
 */
@Data
public class InventoryItemDTO {
    private Long id;
    private Long userId;
    private Long itemId;
    private Integer quantity;
    private String bindStatus;
    private LocalDateTime lastUpdatedAt;
}

