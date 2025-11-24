package com.dungeon.dto;

import lombok.Data;

import java.util.List;

/**
 * 开启地牢探索请求
 */
@Data
public class StartRunRequest {
    private Integer stageNumber;
    private Long userPlayerCharacterId;
    private List<Long> cardCharacterIds;
    private List<Long> cardIds;
    private List<Long> consumableItemIds;
    private String notes;
}

