package com.dungeon.dto;

import lombok.Data;

/**
 * 商城商品详情DTO
 * 包含商品信息和关联的道具/卡牌信息
 */
@Data
public class ShopOfferDetailDTO {
    /**
     * 商城商品ID
     */
    private Long id;
    
    /**
     * 商品类型：card-卡牌, item-道具, card_character-角色, bundle-礼包
     */
    private String offerType;
    
    /**
     * 目标ID（指向cards.id、items.id或card_characters.id）
     */
    private Long targetId;
    
    /**
     * 价格
     */
    private Long price;
    
    /**
     * 显示顺序
     */
    private Integer displayOrder;
    
    /**
     * 刷新规则（JSON格式）
     */
    private String refreshRule;
    
    /**
     * 关联的道具信息（当offerType为item时）
     */
    private ItemDTO item;
    
    /**
     * 关联的卡牌信息（当offerType为card时）
     */
    private CardDTO card;
    
    /**
     * 关联的角色信息（当offerType为card_character时）
     */
    private CardCharacterDTO cardCharacter;
}

