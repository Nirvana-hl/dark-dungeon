package com.dungeon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关卡进度DTO
 */
@Data
public class UserStageProgressDTO {
    private Long id;
    private Long userId;
    private Integer stageNumber;
    private Integer chapterNumber;
    private Boolean isPassed;
    private Boolean isUnlocked;
    private String bestResult;
    private LocalDateTime passedAt;
    private Integer attemptCount;
    private Integer bestScore;
    
    // 关联信息
    private String stageName;
    private String difficulty;
    private Boolean isBossStage;
}

