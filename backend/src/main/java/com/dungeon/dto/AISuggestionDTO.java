package com.dungeon.dto;

import lombok.Data;

/**
 * AI建议DTO
 */
@Data
public class AISuggestionDTO {
    /**
     * 建议ID
     */
    private Long id;
    
    /**
     * 建议类型：strategy-策略, upgrade-升级, explore-探索等
     */
    private String type;
    
    /**
     * 建议标题
     */
    private String title;
    
    /**
     * 建议内容
     */
    private String content;
    
    /**
     * 优先级（1-5，5最高）
     */
    private Integer priority;
}

