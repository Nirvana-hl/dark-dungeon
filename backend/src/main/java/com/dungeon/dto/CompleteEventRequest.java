package com.dungeon.dto;

import lombok.Data;

/**
 * 完成任务事件请求DTO
 */
@Data
public class CompleteEventRequest {
    /**
     * 事件ID（前端可能传递字符串）
     */
    private Object eventId;
    
    /**
     * 获取事件ID（转换为Long）
     */
    public Long getEventIdAsLong() {
        if (eventId == null) {
            return null;
        }
        if (eventId instanceof Long) {
            return (Long) eventId;
        }
        if (eventId instanceof String) {
            try {
                return Long.parseLong((String) eventId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (eventId instanceof Number) {
            return ((Number) eventId).longValue();
        }
        return null;
    }
}

