package com.dungeon.dto;

import lombok.Data;

/**
 * 关卡DTO
 */
@Data
public class StageDTO {
    private Long id;
    private Integer stageNumber;
    private Integer chapterNumber;
    private Long dungeonId;
    private String stageName;
    private String difficulty;
    private Boolean isBossStage;
    private String enemyPool;
    private String eventPool;
    private String rewardPool;
    private String explorationMap;
    private String description;
    
    // 关联信息
    private String dungeonName;
    private String dungeonTheme;
}

