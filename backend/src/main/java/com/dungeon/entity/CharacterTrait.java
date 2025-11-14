package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色特性实体
 */
@Data
@TableName("character_traits")
public class CharacterTrait {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name; // 角色名称
    private String traitKey; // 特性键：priest_bless/shield_guard等
    private Integer basePower;
    private Integer powerPerStar;
    private String description;
    private LocalDateTime createdAt;
}

