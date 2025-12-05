package com.dungeon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 敌人面板 DTO
 * 用于向前端返回敌人的基础属性和攻击特性
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnemyPanelDTO {

    /**
     * 敌人ID
     */
    private Long id;

    /**
     * 敌人名称
     */
    private String name;

    /**
     * 敌人难度：easy/normal/hard/boss
     */
    private String difficulty;

    /**
     * 最大生命值（来自 base_stats.hp）
     */
    private Integer hp;

    /**
     * 攻击力（来自 base_stats.attack）
     */
    private Integer attack;

    /**
     * 护甲/防御（来自 base_stats.armor，可选）
     */
    private Integer armor;

    /**
     * 攻击特性标签（例如：fire_attack、strike_poison）
     * 从 behavior_script.pattern 或 behavior_script.attack_traits 中解析
     */
    private String[] attackTraits;
}


