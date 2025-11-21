package com.dungeon.dto;

import lombok.Data;

/**
 * 道具模板DTO
 */
@Data
public class ItemDTO {
    private Long id;
    private String code;
    private String name;
    private String itemType;
    private String rarity;
    private String effectPayload;
    private Integer stackLimit;
    private Integer shopPrice;
    private String description;
}

