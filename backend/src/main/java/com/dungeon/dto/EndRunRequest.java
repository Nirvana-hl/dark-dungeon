package com.dungeon.dto;

import lombok.Data;

import java.util.Map;

/**
 * 结束地牢探索请求
 */
@Data
public class EndRunRequest {
    private String result; // victory / defeat / abandon
    private String notes;
    private Map<String, Object> rewardChoice;
}

