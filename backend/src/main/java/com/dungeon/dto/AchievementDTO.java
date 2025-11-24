package com.dungeon.dto;

import lombok.Data;

/**
 * 成就DTO
 * 用于返回成就信息
 */
@Data
public class AchievementDTO {
    /**
     * 成就ID
     */
    private Long id;
    
    /**
     * 成就名称
     */
    private String name;
    
    /**
     * 分类：progression-进度, mastery-精通, collection-收集, social-社交
     */
    private String category;
    
    /**
     * 成就描述
     */
    private String description;
    
    /**
     * 完成条件（JSON格式）
     */
    private String requirements;
    
    /**
     * 是否已完成（需要根据用户数据判断）
     */
    private Boolean isCompleted;
    
    /**
     * 完成进度（0-100，需要根据用户数据计算）
     */
    private Integer progress;
}

