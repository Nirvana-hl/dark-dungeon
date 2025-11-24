package com.dungeon.dto;

import lombok.Data;

/**
 * 地牢探索/战斗操作响应
 */
@Data
public class RunActionResponse {
    private RunDTO run;
    private String message;
    private String eventSummary;
    private BattleResultDTO battleResult;
    private boolean battlePending;
}

