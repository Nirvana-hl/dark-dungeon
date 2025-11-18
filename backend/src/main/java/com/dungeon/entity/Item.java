package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 道具模板实体
 * 对应数据库表：items
 */
@Data
@TableName("items")
public class Item {
    /**
     * 道具ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 策划短码
     */
    private String code;
    
    /**
     * 道具名称
     */
    private String name;
    
    /**
     * 道具类型：consumable-消耗品, material-材料, blueprint-蓝图, currency_bundle-货币包, cosmetic-装饰
     */
    private String itemType;
    
    /**
     * 稀有度：common-普通, rare-稀有, epic-史诗, legendary-传说
     */
    private String rarity;
    
    /**
     * 使用效果（JSON格式）
     */
    private String effectPayload;
    
    /**
     * 单格堆叠上限
     */
    private Integer stackLimit;
    
    /**
     * 商品价格
     */
    private Integer shopPrice;
    
    /**
     * 说明
     */
    private String description;
}

