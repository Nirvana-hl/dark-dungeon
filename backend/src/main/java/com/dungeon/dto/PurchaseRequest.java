package com.dungeon.dto;

import lombok.Data;

/**
 * 购买商品请求DTO
 */
@Data
public class PurchaseRequest {
    /**
     * 商品类型：card-卡牌, item-道具, card_character-角色
     */
    private String offerType;
    
    /**
     * 目标ID（指向cards.id、items.id或card_characters.id）
     */
    private Long targetId;
    
    /**
     * 购买数量（默认为1）
     */
    private Integer quantity;
    
    /**
     * 兼容旧版本：商城商品ID（shop_offers表的id）
     * 如果提供了此字段，会先查询shop_offers表获取offerType和targetId
     */
    private Long shopOfferId;
}

