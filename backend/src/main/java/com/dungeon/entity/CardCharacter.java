package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 卡牌角色模板实体
 * 对应数据库表：card_characters
 */
@Data
@TableName("card_characters")
public class CardCharacter {
    /**
     * 卡牌角色模板ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 策划用短码
     */
    private String code;
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 职业
     */
    private String classType;
    
    /**
     * 阵营/族群标签
     */
    private String faction;
    
    /**
     * 稀有度：common-普通, rare-稀有, epic-史诗, legendary-传说
     */
    private String rarity;
    
    /**
     * 基础血量
     */
    private Integer baseHp;
    
    /**
     * 基础攻击力
     */
    private Integer baseAttack;
    
    /**
     * 上阵所需行动点
     */
    private Integer actionPointCost;
    
    /**
     * 初始星级（1-3）
     */
    private Integer baseStarLevel;
    
    /**
     * 星级上限（默认5）
     */
    private Integer maxStarLevel;
    
    /**
     * 星级提升带来的属性/特性增幅配置（JSON格式）
     */
    private String starUpgradePayload;
    
    /**
     * 特性属性列表（JSON格式）
     */
    private String traits;
    
    /**
     * 商城售价
     */
    private Integer shopPrice;
    
    /**
     * 背景故事
     */
    private String lore;
}

