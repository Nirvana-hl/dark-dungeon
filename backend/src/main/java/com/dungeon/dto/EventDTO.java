package com.dungeon.dto;

import lombok.Data;

/**
 * 事件DTO
 */
@Data
public class EventDTO {
    private Long id;
    private String name;
    private String locationType;
    private String description;
    private String effectPayload;
    private String triggerCondition;
    private String choices;
}

