package com.dungeon.dto;

import lombok.Data;

/**
 * 角色 DTO
 */
@Data
public class CharacterDTO {
    private Long id;
    private String name;
    private String classType; // 战士/法师/游侠
    private Integer stars;
    private Integer hp;
    private Integer mp;
}

