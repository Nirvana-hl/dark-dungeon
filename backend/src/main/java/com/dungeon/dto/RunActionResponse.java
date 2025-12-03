package com.dungeon.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

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
    private Long pendingEnemyId;
    private String pendingEnemyName;
    private String pendingEnemyDifficulty;
    private List<Map<String, Object>> pendingEnemyCards;
}

