package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.CardCharacterDTO;
import com.dungeon.dto.CardDTO;
import com.dungeon.dto.ItemDTO;
import com.dungeon.dto.PurchaseRequest;
import com.dungeon.dto.ShopOfferDetailDTO;
import com.dungeon.entity.Card;
import com.dungeon.entity.CardCharacter;
import com.dungeon.entity.Inventory;
import com.dungeon.entity.Item;
import com.dungeon.entity.ShopOffer;
import com.dungeon.entity.UserCard;
import com.dungeon.entity.UserCardCharacter;
import com.dungeon.entity.UserWallet;
import com.dungeon.mapper.CardCharacterMapper;
import com.dungeon.mapper.CardMapper;
import com.dungeon.mapper.InventoryMapper;
import com.dungeon.mapper.ItemMapper;
import com.dungeon.mapper.ShopOfferMapper;
import com.dungeon.mapper.UserCardCharacterMapper;
import com.dungeon.mapper.UserCardMapper;
import com.dungeon.mapper.UserWalletMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 商城服务
 * 负责商城商品查询、购买逻辑
 */
@Service
public class ShopService {

    @Autowired
    private ShopOfferMapper shopOfferMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private UserWalletMapper userWalletMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private UserCardMapper userCardMapper;

    @Autowired
    private CardCharacterMapper cardCharacterMapper;

    @Autowired
    private UserCardCharacterMapper userCardCharacterMapper;

    /**
     * 获取商城商品列表
     * 注意：会验证并更新价格，确保价格来自数据库的shopPrice字段
     * @return 商品列表（按displayOrder排序）
     */
    public List<ShopOfferDetailDTO> getShopOffers() {
        QueryWrapper<ShopOffer> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("display_order");
        List<ShopOffer> offers = shopOfferMapper.selectList(wrapper);

        // 验证并更新价格，确保价格来自数据库
        for (ShopOffer offer : offers) {
            updateOfferPriceFromDatabase(offer);
        }

        return offers.stream()
                .map(this::toShopOfferDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定商店类型的商品列表（最多8个）
     * @param shopType 商店类型：item-道具商店, card_character-卡牌商店
     * @return 商品列表（最多8个）
     */
    public List<ShopOfferDetailDTO> getShopOffersByType(String shopType) {
        QueryWrapper<ShopOffer> wrapper = new QueryWrapper<>();
        wrapper.eq("offer_type", shopType)
               .orderByAsc("display_order")
               .last("LIMIT 8");
        List<ShopOffer> offers = shopOfferMapper.selectList(wrapper);

        // 如果商品不足8个，用空位填充
        List<ShopOfferDetailDTO> result = offers.stream()
                .map(this::toShopOfferDetailDTO)
                .collect(Collectors.toList());
        
        // 确保返回8个位置（不足的用null填充，前端会显示为空位）
        while (result.size() < 8) {
            result.add(null);
        }
        
        return result;
    }

    /**
     * 刷新指定商店类型的商品（随机生成8个商品）
     * @param shopType 商店类型：item-道具商店, card_character-卡牌商店
     * @return 刷新后的商品列表（8个）
     */
    @Transactional
    public List<ShopOfferDetailDTO> refreshShop(String shopType) {
        // 删除该商店类型的所有现有商品
        QueryWrapper<ShopOffer> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("offer_type", shopType);
        shopOfferMapper.delete(deleteWrapper);

        // 根据商店类型获取所有可用的商品/角色（只选择有shopPrice且shopPrice > 0的）
        List<Long> availableIds;
        if ("item".equals(shopType)) {
            // 只获取有shopPrice且shopPrice > 0的道具
            QueryWrapper<Item> itemWrapper = new QueryWrapper<>();
            itemWrapper.isNotNull("shop_price")
                    .gt("shop_price", 0);
            List<Item> items = itemMapper.selectList(itemWrapper);
            availableIds = items.stream()
                    .map(Item::getId)
                    .collect(Collectors.toList());
        } else if ("card_character".equals(shopType)) {
            // 只获取有shopPrice且shopPrice > 0的卡牌角色
            QueryWrapper<CardCharacter> characterWrapper = new QueryWrapper<>();
            characterWrapper.isNotNull("shop_price")
                    .gt("shop_price", 0);
            List<CardCharacter> characters = cardCharacterMapper.selectList(characterWrapper);
            availableIds = characters.stream()
                    .map(CardCharacter::getId)
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("不支持的商店类型: " + shopType);
        }

        if (availableIds.isEmpty()) {
            throw new RuntimeException("没有可用的商品");
        }

        // 随机选择8个（如果可用商品少于8个，则全部选择）
        // 注意：这里只是随机选择商品，价格完全来自数据库
        Collections.shuffle(availableIds, new Random());
        int count = Math.min(8, availableIds.size());
        List<Long> selectedIds = availableIds.subList(0, count);

        // 创建新的商店商品（所有价格都来自数据库，不随机生成）
        int displayOrder = 1;
        for (int i = 0; i < selectedIds.size(); i++) {
            Long targetId = selectedIds.get(i);
            ShopOffer offer = new ShopOffer();
            offer.setOfferType(shopType);
            offer.setTargetId(targetId);
            
            // 根据商品类型设置价格（严格使用数据库中的shopPrice）
            if ("item".equals(shopType)) {
                Item item = itemMapper.selectById(targetId);
                // 道具必须使用数据库中的shopPrice，如果没有价格则跳过该道具
                if (item == null) {
                    continue; // 跳过无效的道具
                }
                if (item.getShopPrice() == null || item.getShopPrice() <= 0) {
                    // 如果没有设置shopPrice，跳过该道具，不添加到商店
                    continue;
                }
                // 使用数据库中的shopPrice
                long basePrice = item.getShopPrice().longValue();
                offer.setPrice(basePrice);
            } else if ("card_character".equals(shopType)) {
                CardCharacter character = cardCharacterMapper.selectById(targetId);
                // 卡牌角色必须使用数据库中的shopPrice，如果没有价格则跳过该角色
                if (character == null) {
                    continue; // 跳过无效的角色
                }
                if (character.getShopPrice() == null || character.getShopPrice() <= 0) {
                    // 如果没有设置shopPrice，跳过该角色，不添加到商店
                    continue;
                }
                // 使用数据库中的shopPrice
                long basePrice = character.getShopPrice().longValue();
                offer.setPrice(basePrice);
            }
            
            offer.setDisplayOrder(displayOrder++);
            offer.setRefreshRule("{}");
            shopOfferMapper.insert(offer);
        }

        // 返回刷新后的商品列表
        return getShopOffersByType(shopType);
    }

    /**
     * 购买商品
     * @param userId 用户ID
     * @param request 购买请求
     * @return 购买结果信息
     */
    @Transactional
    public String purchaseItem(Long userId, PurchaseRequest request) {
        // 验证请求参数
        if (request == null || request.getShopOfferId() == null) {
            throw new RuntimeException("商品ID不能为空");
        }

        Integer quantity = request.getQuantity() != null && request.getQuantity() > 0 
                ? request.getQuantity() : 1;

        // 查询商品信息
        ShopOffer offer = shopOfferMapper.selectById(request.getShopOfferId());
        if (offer == null) {
            throw new RuntimeException("商品不存在");
        }

        // 计算总价
        Long totalPrice = offer.getPrice() * quantity;

        // 检查用户余额（默认使用gold货币）
        UserWallet wallet = getUserWallet(userId, "gold");
        if (wallet == null || wallet.getBalance() < totalPrice) {
            throw new RuntimeException("余额不足");
        }

        // 扣款
        wallet.setBalance(wallet.getBalance() - totalPrice);
        wallet.setUpdatedAt(LocalDateTime.now());
        userWalletMapper.updateById(wallet);

        // 发放物品
        String offerType = offer.getOfferType();
        if (offerType == null || offerType.trim().isEmpty()) {
            throw new RuntimeException("商品类型不能为空");
        }
        
        // 去除空格并转为小写进行比较（防止大小写或空格问题）
        offerType = offerType.trim();
        
        if ("item".equals(offerType)) {
            // 发放道具
            addItemToInventory(userId, offer.getTargetId(), quantity);
            return String.format("成功购买 %d 个道具", quantity);
        } else if ("card".equals(offerType)) {
            // 发放卡牌（法术/装备）
            addCardToUser(userId, offer.getTargetId(), quantity);
            return String.format("成功购买 %d 张卡牌", quantity);
        } else if ("card_character".equals(offerType)) {
            // 发放角色（卡牌角色）
            addCardCharacterToUser(userId, offer.getTargetId(), quantity);
            return String.format("成功购买 %d 个角色", quantity);
        } else {
            throw new RuntimeException("不支持的商品类型: " + offerType + " (实际值: [" + offer.getOfferType() + "])");
        }
    }

    /**
     * 获取用户钱包（如果不存在则创建）
     */
    private UserWallet getUserWallet(Long userId, String currencyType) {
        QueryWrapper<UserWallet> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("currency_type", currencyType);
        UserWallet wallet = userWalletMapper.selectOne(wrapper);

        if (wallet == null) {
            // 创建新钱包
            wallet = new UserWallet();
            wallet.setUserId(userId);
            wallet.setCurrencyType(currencyType);
            wallet.setBalance(0L);
            wallet.setUpdatedAt(LocalDateTime.now());
            userWalletMapper.insert(wallet);
        }

        return wallet;
    }

    /**
     * 添加道具到背包
     */
    private void addItemToInventory(Long userId, Long itemId, Integer quantity) {
        // 查询是否已有该道具
        QueryWrapper<Inventory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("item_id", itemId);
        Inventory inventory = inventoryMapper.selectOne(wrapper);

        if (inventory != null) {
            // 增加数量
            inventory.setQuantity(inventory.getQuantity() + quantity);
            inventory.setLastUpdatedAt(LocalDateTime.now());
            inventoryMapper.updateById(inventory);
        } else {
            // 创建新记录
            inventory = new Inventory();
            inventory.setUserId(userId);
            inventory.setItemId(itemId);
            inventory.setQuantity(quantity);
            inventory.setBindStatus("unbound");
            inventory.setLastUpdatedAt(LocalDateTime.now());
            inventoryMapper.insert(inventory);
        }
    }

    /**
     * 添加卡牌到用户（法术/装备）
     */
    private void addCardToUser(Long userId, Long cardId, Integer quantity) {
        // 查询是否已有该卡牌
        QueryWrapper<UserCard> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("card_id", cardId);
        UserCard userCard = userCardMapper.selectOne(wrapper);

        if (userCard != null) {
            // 增加数量
            userCard.setQuantity(userCard.getQuantity() + quantity);
            userCardMapper.updateById(userCard);
        } else {
            // 创建新记录
            userCard = new UserCard();
            userCard.setUserId(userId);
            userCard.setCardId(cardId);
            userCard.setQuantity(quantity);
            userCard.setLevel(1);
            userCard.setAcquiredAt(LocalDateTime.now());
            userCard.setAcquiredSource("shop");
            userCardMapper.insert(userCard);
        }
    }

    /**
     * 添加角色到用户（卡牌角色）
     * 购买角色后，如果已有该角色则增加数量，否则创建新记录
     * 这样便于后续实现升星功能（消耗数量来升星）
     */
    private void addCardCharacterToUser(Long userId, Long cardCharacterId, Integer quantity) {
        // 查询角色模板
        CardCharacter template = cardCharacterMapper.selectById(cardCharacterId);
        if (template == null) {
            throw new RuntimeException("角色模板不存在");
        }

        // 查询是否已有该角色
        QueryWrapper<UserCardCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("card_character_id", cardCharacterId);
        UserCardCharacter existing = userCardCharacterMapper.selectOne(wrapper);

        if (existing != null) {
            // 如果已有该角色，增加数量
            existing.setQuantity(existing.getQuantity() != null ? existing.getQuantity() + quantity : quantity);
            userCardCharacterMapper.updateById(existing);
        } else {
            // 如果没有，创建新记录
            UserCardCharacter userCardCharacter = new UserCardCharacter();
            userCardCharacter.setUserId(userId);
            userCardCharacter.setCardCharacterId(cardCharacterId);
            // 初始化属性
            userCardCharacter.setCurrentHp(template.getBaseHp());
            userCardCharacter.setCurrentArmor(0);
            userCardCharacter.setIsDeployed(false);
            userCardCharacter.setDeployedRound(0);
            userCardCharacter.setCurrentStarLevel(template.getBaseStarLevel() != null ? template.getBaseStarLevel() : 1);
            userCardCharacter.setQuantity(quantity);
            userCardCharacterMapper.insert(userCardCharacter);
        }
    }

    /**
     * 商城商品转DTO（包含详细信息）
     */
    private ShopOfferDetailDTO toShopOfferDetailDTO(ShopOffer offer) {
        ShopOfferDetailDTO dto = new ShopOfferDetailDTO();
        BeanUtils.copyProperties(offer, dto);

        // 根据商品类型加载详细信息
        if ("item".equals(offer.getOfferType())) {
            Item item = itemMapper.selectById(offer.getTargetId());
            if (item != null) {
                ItemDTO itemDTO = new ItemDTO();
                BeanUtils.copyProperties(item, itemDTO);
                dto.setItem(itemDTO);
            }
        } else if ("card".equals(offer.getOfferType())) {
            Card card = cardMapper.selectById(offer.getTargetId());
            if (card != null) {
                CardDTO cardDTO = new CardDTO();
                BeanUtils.copyProperties(card, cardDTO);
                dto.setCard(cardDTO);
            }
        } else if ("card_character".equals(offer.getOfferType())) {
            CardCharacter cardCharacter = cardCharacterMapper.selectById(offer.getTargetId());
            if (cardCharacter != null) {
                CardCharacterDTO cardCharacterDTO = new CardCharacterDTO();
                BeanUtils.copyProperties(cardCharacter, cardCharacterDTO);
                dto.setCardCharacter(cardCharacterDTO);
            }
        }

        return dto;
    }

    /**
     * 更新商店商品的价格，确保价格来自数据库的shopPrice字段
     * 如果数据库中的shopPrice与当前价格不一致，则更新
     * @param offer 商店商品
     */
    private void updateOfferPriceFromDatabase(ShopOffer offer) {
        if (offer == null || offer.getTargetId() == null) {
            return;
        }

        try {
            Long correctPrice = null;
            String itemName = "未知";
            
            if ("item".equals(offer.getOfferType())) {
                Item item = itemMapper.selectById(offer.getTargetId());
                if (item != null) {
                    itemName = item.getName() != null ? item.getName() : "未知道具";
                    if (item.getShopPrice() != null && item.getShopPrice() > 0) {
                        correctPrice = item.getShopPrice().longValue();
                        System.out.println("[ShopService] 检查道具价格: offerId=" + offer.getId() + 
                                         ", itemName=" + itemName + 
                                         ", dbShopPrice=" + correctPrice + 
                                         ", currentPrice=" + offer.getPrice());
                    } else {
                        System.out.println("[ShopService] 道具没有shopPrice: offerId=" + offer.getId() + 
                                         ", itemName=" + itemName + 
                                         ", shopPrice=" + item.getShopPrice());
                    }
                }
            } else if ("card_character".equals(offer.getOfferType())) {
                CardCharacter character = cardCharacterMapper.selectById(offer.getTargetId());
                if (character != null) {
                    itemName = character.getName() != null ? character.getName() : "未知角色";
                    if (character.getShopPrice() != null && character.getShopPrice() > 0) {
                        correctPrice = character.getShopPrice().longValue();
                        System.out.println("[ShopService] 检查角色价格: offerId=" + offer.getId() + 
                                         ", characterName=" + itemName + 
                                         ", dbShopPrice=" + correctPrice + 
                                         ", currentPrice=" + offer.getPrice());
                    } else {
                        System.out.println("[ShopService] 角色没有shopPrice: offerId=" + offer.getId() + 
                                         ", characterName=" + itemName + 
                                         ", shopPrice=" + character.getShopPrice());
                    }
                }
            }

            // 如果找到了正确的价格，且与当前价格不一致，则更新
            if (correctPrice != null) {
                Long oldPrice = offer.getPrice();
                if (!correctPrice.equals(oldPrice)) {
                    offer.setPrice(correctPrice);
                    shopOfferMapper.updateById(offer);
                    System.out.println("[ShopService] ✓ 更新商品价格: offerId=" + offer.getId() + 
                                     ", name=" + itemName +
                                     ", oldPrice=" + oldPrice + 
                                     ", newPrice=" + correctPrice);
                } else {
                    System.out.println("[ShopService] ✓ 价格已正确: offerId=" + offer.getId() + 
                                     ", name=" + itemName +
                                     ", price=" + correctPrice);
                }
            } else {
                System.out.println("[ShopService] ⚠ 无法获取正确价格: offerId=" + offer.getId() + 
                                 ", name=" + itemName);
            }
        } catch (Exception e) {
            System.err.println("[ShopService] ✗ 更新商品价格失败: offerId=" + offer.getId() + 
                             ", error=" + e.getMessage());
            e.printStackTrace();
        }
    }
}

