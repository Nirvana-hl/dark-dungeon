package com.dungeon.dto;

import lombok.Data;

/**
 * 商城商品 DTO
 */
@Data
public class ShopOfferDTO {
    private Long id;
    private String offerType;
    private Long targetId;
    private Long price;
    private Integer displayOrder;
    private String refreshRule;
}

