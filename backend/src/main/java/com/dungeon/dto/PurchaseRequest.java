package com.dungeon.dto;

import lombok.Data;

/**
 * 购买商品请求DTO
 */
@Data
public class PurchaseRequest {
    /**
     * 商城商品ID（shop_offers表的id）
     */
    private Long shopOfferId;
    
    /**
     * 购买数量（默认为1）
     */
    private Integer quantity;
}

