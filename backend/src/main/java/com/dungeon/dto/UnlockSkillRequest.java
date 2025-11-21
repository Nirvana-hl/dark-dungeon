package com.dungeon.dto;

import lombok.Data;

/**
 * 解锁技能请求DTO
 * 
 * 注意：技能解锁总是针对当前用户的当前职业
 * 不再需要指定角色实例ID，系统会自动从当前用户获取角色实例
 */
@Data
public class UnlockSkillRequest {
    /**
     * 技能ID（技能模板ID）
     */
    private Long skillId;
}

