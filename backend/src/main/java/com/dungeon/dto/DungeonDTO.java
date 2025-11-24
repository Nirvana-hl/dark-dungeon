package com.dungeon.dto;

import lombok.Data;

/**
 * 地牢数据传输对象
 */
@Data
public class DungeonDTO {
    private Long id;
    private String name;
    private String difficulty;
    private String theme;
    private String description;
    private String recommendedCards;
}

