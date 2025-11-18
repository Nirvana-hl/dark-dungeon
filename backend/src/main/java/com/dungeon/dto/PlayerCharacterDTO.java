package com.dungeon.dto;

import lombok.Data;

/**
 * 玩家角色模板DTO
 */
@Data
public class PlayerCharacterDTO {
    private Long id;
    private String code;
    private String name;
    private Integer baseHp;
    private Integer hpPerLevel;
    private String lore;
}

