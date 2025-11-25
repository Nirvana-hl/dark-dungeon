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
    private Integer currentRoomId;        // 当前房间ID（基于地图）
    private List<Integer> visitedRooms;   // 已访问的房间ID列表
    private int exploredRooms;
    private int defeatedEnemies;
    private List<String> eventLog;
    private List<String> battleLog;
    private String pendingEnemyName;
    private String pendingEnemyDifficulty;
}

