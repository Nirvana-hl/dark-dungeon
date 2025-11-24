package com.dungeon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 地牢探索记录 DTO
 */
@Data
public class RunDTO {
    private Long id;
    private Long dungeonId;
    private String dungeonName;
    private Integer stageNumber;
    private Integer chapterNumber;
    private String stageName;
    private String difficulty;
    private String result;
    private String preparationSnapshot;
    private String rewardSnapshot;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private RunProgressDTO progress;
}

