package com.dungeon.dto;

import lombok.Data;

import java.util.List;

/**
 * 地牢探索进度 DTO
 */
@Data
public class RunProgressDTO {
    private String status;
    private String currentRoom;
    private int exploredRooms;
    private int defeatedEnemies;
    private List<String> eventLog;
    private List<String> battleLog;
    private String pendingEnemyName;
    private String pendingEnemyDifficulty;
}

