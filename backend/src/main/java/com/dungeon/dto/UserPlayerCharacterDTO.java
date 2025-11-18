package com.dungeon.dto;

import lombok.Data;
import java.util.List;

/**
 * 玩家角色实例DTO
 */
@Data
public class UserPlayerCharacterDTO {
    private Long id;
    private Long userId;
    private Long playerCharacterId;
    private String playerCharacterName;  // 角色名称（关联查询）
    private String playerCharacterCode;  // 角色代码（关联查询）
    private Integer maxHp;
    private Integer currentHp;
    private Integer maxActionPoints;
    private Integer currentActionPoints;
    private Integer currentStress;
    private Integer stressLevel;
    private List<StressDebuffDTO> stressDebuffs;  // 压力debuff列表
}

