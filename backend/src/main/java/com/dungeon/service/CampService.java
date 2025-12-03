package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dungeon.dto.CampDashboardDTO;
import com.dungeon.dto.InventoryItemDTO;
import com.dungeon.dto.ShopOfferDTO;
import com.dungeon.dto.UserCardCharacterDTO;
import com.dungeon.dto.UserCardDTO;
import com.dungeon.dto.UserPlayerCharacterDTO;
import com.dungeon.dto.UserWalletDTO;
import com.dungeon.entity.Inventory;
import com.dungeon.entity.ShopOffer;
import com.dungeon.entity.UserWallet;
import com.dungeon.mapper.InventoryMapper;
import com.dungeon.mapper.ShopOfferMapper;
import com.dungeon.mapper.UserWalletMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 营地聚合数据服务
 */
@Service
public class CampService {

    @Autowired
    private UserPlayerCharacterService userPlayerCharacterService;

    @Autowired
    private UserCardCharacterService userCardCharacterService;

    @Autowired
    private UserCardService userCardService;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private UserWalletMapper userWalletMapper;

    @Autowired
    private ShopOfferMapper shopOfferMapper;

    @Autowired
    private ItemService itemService;

    /**
     * 获取营地仪表盘所需的全部数据
     */
    public CampDashboardDTO getDashboard(Long userId) {
        CampDashboardDTO dashboard = new CampDashboardDTO();

        UserPlayerCharacterDTO playerCharacter = userPlayerCharacterService.getUserPlayerCharacter(userId);
        List<UserCardCharacterDTO> cardCharacters = userCardCharacterService.getUserCardCharacters(userId);
        List<UserCardDTO> userCards = userCardService.getUserCards(userId);

        dashboard.setUserPlayerCharacter(playerCharacter);
        dashboard.setUserCardCharacters(cardCharacters);
        dashboard.setUserCards(userCards);
        dashboard.setInventory(getInventoryItems(userId));
        dashboard.setWallets(getWallets(userId));
        dashboard.setShopOffers(getShopOffers());

        return dashboard;
    }

    private List<InventoryItemDTO> getInventoryItems(Long userId) {
        // 使用ItemService的方法，确保包含道具详细信息
        return itemService.getUserInventory(userId);
    }

    private List<UserWalletDTO> getWallets(Long userId) {
        LambdaQueryWrapper<UserWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWallet::getUserId, userId);
        List<UserWallet> wallets = userWalletMapper.selectList(wrapper);
        if (wallets == null || wallets.isEmpty()) {
            return Collections.emptyList();
        }
        return wallets.stream().map(wallet -> {
            UserWalletDTO dto = new UserWalletDTO();
            BeanUtils.copyProperties(wallet, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    private List<ShopOfferDTO> getShopOffers() {
        LambdaQueryWrapper<ShopOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ShopOffer::getDisplayOrder);
        List<ShopOffer> offers = shopOfferMapper.selectList(wrapper);
        if (offers == null || offers.isEmpty()) {
            return Collections.emptyList();
        }
        return offers.stream().map(offer -> {
            ShopOfferDTO dto = new ShopOfferDTO();
            BeanUtils.copyProperties(offer, dto);
            return dto;
        }).collect(Collectors.toList());
    }
}

