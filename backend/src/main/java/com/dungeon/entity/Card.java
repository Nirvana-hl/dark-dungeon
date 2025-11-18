package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 统一卡牌池实体（法术/装备）
 * 对应数据库表：cards
 */
@Data
@TableName("cards")
public class Card {
    /**
     * 卡牌ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 策划短码
     */
    private String code;
    
    /**
     * 卡牌名称
     */
    private String name;
    
    /**
     * 卡牌类型：spell-法术, equipment-装备
     */
    private String cardType;
    
    /**
     * 稀有度：common-普通, rare-稀有, epic-史诗, legendary-传说
     */
    private String rarity;
    
    /**
     * 装备适用槽位：weapon-武器, armor-护甲, trinket-饰品, none-无（法术）
     */
    private String slotType;
    
    /**
     * 法术/装备触发所需行动点
     */
    private Integer actionPointCost;
    
    /**
     * 属性变动（JSON格式）
     */
    private String statModifiers;
    
    /**
     * 生效脚本（JSON格式）
     */
    private String effectPayload;
    
    /**
     * 在营地的解锁条件（JSON格式）
     */
    private String campUnlockCondition;
    
    /**
     * 商城价格（JSON格式：{currency_type, amount}）
     */
    private String shopPrice;
    
    /**
     * 展示描述
     */
    private String description;
}

