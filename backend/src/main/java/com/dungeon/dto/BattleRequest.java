package com.dungeon.dto;

import lombok.Data;

@Data
public class BattleRequest {
    /**
     * 前端回传的战斗结果（权威）
     * outcome: victory/defeat
     * heroRemainingHp: 战斗结束时玩家剩余血量
     * enemyRemainingHp: 敌人剩余血量（用于前端展示/日志）
     * battleLog: 前端生成的战斗日志
     * stressDelta: 本场战斗对压力值的增量（可选，默认 3）
     */
    private String outcome;
    private Integer heroRemainingHp;
    private Integer enemyRemainingHp;
    private java.util.List<String> battleLog;
    private Integer stressDelta;

    // 兼容旧字段（不再使用随机模拟，但保留以免前端还在传）
    private String strategy;
}

