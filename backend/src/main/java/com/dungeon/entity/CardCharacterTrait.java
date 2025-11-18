package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 卡牌角色特性实体
 * 对应数据库表：card_character_traits
 */
@Data
@TableName("card_character_traits")
public class CardCharacterTrait {
    /**
     * 特性ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 卡牌角色模板ID
     */
    private String cardCharacterId;
    
    /**
     * 特性名称
     */
    private String name;
    
    /**
     * 特性类型：positive-正面, negative-负面, neutral-中性
     */
    private String type;
    
    /**
     * 基础效果参数（JSON格式）
     */
    private String effectPayload;
    
    /**
     * 星级缩放配置（JSON格式）
     */
    private String scalingPayload;
    
    /**
     * 展示描述
     */
    private String description;
}

