package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商城上架配置实体
 * 对应数据库表：shop_offers
 */
@Data
@TableName("shop_offers")
public class ShopOffer {
    /**
     * 商城商品ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商品类型：card-卡牌, item-道具, bundle-礼包
     */
    private String offerType;
    
    /**
     * 指向cards.id或items.id
     */
    private Long targetId;
    
    /**
     * 价格
     */
    private Long price;
    
    /**
     * 前端排序
     */
    private Integer displayOrder;
    
    /**
     * 刷新与限购规则（JSON格式）
     */
    private String refreshRule;
}

