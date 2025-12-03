package com.dungeon.dto;

import lombok.Data;

/**
 * 部署卡牌角色请求DTO
 */
@Data
public class DeployCardCharacterRequest {
    /**
     * 用户卡牌角色ID（前端可能传递字符串）
     */
    private Object userCardCharacterId;
    
    /**
     * 是否部署：true-部署, false-撤下
     */
    private Boolean deploy;
    
    /**
     * 获取用户卡牌角色ID（转换为Long）
     */
    public Long getUserCardCharacterIdAsLong() {
        if (userCardCharacterId == null) {
            return null;
        }
        if (userCardCharacterId instanceof Long) {
            return (Long) userCardCharacterId;
        }
        if (userCardCharacterId instanceof String) {
            try {
                return Long.parseLong((String) userCardCharacterId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (userCardCharacterId instanceof Number) {
            return ((Number) userCardCharacterId).longValue();
        }
        return null;
    }
}

