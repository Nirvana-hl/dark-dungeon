package com.dungeon.dto;

import lombok.Data;

import java.util.List;

/**
 * 营地仪表盘聚合数据
 */
@Data
public class CampDashboardDTO {

    /**
     * 玩家主角状态
     */
    private UserPlayerCharacterDTO userPlayerCharacter;

    /**
     * 玩家拥有的角色卡
     */
    private List<UserCardCharacterDTO> userCardCharacters;

    /**
     * 玩家拥有的法术/装备卡
     */
    private List<UserCardDTO> userCards;

    /**
     * 背包道具
     */
    private List<InventoryItemDTO> inventory;

    /**
     * 钱包余额（多种货币）
     */
    private List<UserWalletDTO> wallets;

    /**
     * 营地商城商品
     */
    private List<ShopOfferDTO> shopOffers;
}

