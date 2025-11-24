package com.dungeon.dto;

import lombok.Data;

import java.util.List;

/**
 * 战斗结果 DTO
 */
@Data
public class BattleResultDTO {
    private String outcome;
    private String enemyName;
    private int heroRemainingHp;
    private int enemyRemainingHp;
    private List<String> battleLog;
}

