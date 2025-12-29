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
import java.util.List;
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
     * 获取指定商店类型的商品列表（直接从模板表查询）
     * @param shopType 商店类型：item-道具商店, card_character-角色商店, spell-法术商店, equipment-装备商店
     * @return 商品列表（只返回有shopPrice的商品）
     */
    public List<ShopOfferDetailDTO> getShopOffersByType(String shopType) {
        List<ShopOfferDetailDTO> result = new java.util.ArrayList<>();
        
        if ("card_character".equals(shopType)) {
            // 角色商店：从 card_characters 表查询
            QueryWrapper<CardCharacter> wrapper = new QueryWrapper<>();
            wrapper.isNotNull("shop_price")
                   .gt("shop_price", 0)
                   .eq("card_type", "player")  // 只查询玩家角色卡
                   .orderByDesc("rarity")  // 按稀有度排序
                   .orderByAsc("id");
            List<CardCharacter> characters = cardCharacterMapper.selectList(wrapper);
            
            result = characters.stream()
                    .map(this::cardCharacterToShopOfferDTO)
                    .collect(Collectors.toList());
                    
        } else if ("spell".equals(shopType)) {
            // 法术商店：从 cards 表查询，card_type = 'spell'
            QueryWrapper<Card> wrapper = new QueryWrapper<>();
            wrapper.eq("card_type", "spell")
                   .isNotNull("shop_price")
                   .orderByDesc("rarity")
                   .orderByAsc("id");
            List<Card> cards = cardMapper.selectList(wrapper);
            
            result = cards.stream()
                    .filter(card -> {
                        // 检查 shopPrice JSON 是否有有效价格
                        Long price = parseCardShopPrice(card.getShopPrice());
                        return price != null && price > 0;
                    })
                    .map(this::cardToShopOfferDTO)
                    .collect(Collectors.toList());
                    
        } else if ("equipment".equals(shopType)) {
            // 装备商店：从 cards 表查询，card_type = 'equipment'
            QueryWrapper<Card> wrapper = new QueryWrapper<>();
            wrapper.eq("card_type", "equipment")
                   .isNotNull("shop_price")
                   .orderByDesc("rarity")
                   .orderByAsc("id");
            List<Card> cards = cardMapper.selectList(wrapper);
            
            result = cards.stream()
                    .filter(card -> {
                        // 检查 shopPrice JSON 是否有有效价格
                        Long price = parseCardShopPrice(card.getShopPrice());
                        return price != null && price > 0;
                    })
                    .map(this::cardToShopOfferDTO)
                    .collect(Collectors.toList());
                    
        } else if ("item".equals(shopType)) {
            // 道具商店：从 items 表查询
            QueryWrapper<Item> wrapper = new QueryWrapper<>();
            wrapper.isNotNull("shop_price")
                   .gt("shop_price", 0)
                   .orderByDesc("rarity")
                   .orderByAsc("id");
            List<Item> items = itemMapper.selectList(wrapper);
            
            result = items.stream()
                    .map(this::itemToShopOfferDTO)
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("不支持的商店类型: " + shopType);
        }
        
        return result;
    }
    
    /**
     * 将 CardCharacter 转换为 ShopOfferDetailDTO
     */
    private ShopOfferDetailDTO cardCharacterToShopOfferDTO(CardCharacter character) {
        ShopOfferDetailDTO dto = new ShopOfferDetailDTO();
        dto.setId(character.getId());  // 使用角色ID作为商品ID
        dto.setOfferType("card_character");
        dto.setTargetId(character.getId());
        dto.setPrice(character.getShopPrice() != null ? character.getShopPrice().longValue() : 0L);
        
        CardCharacterDTO cardCharacterDTO = new CardCharacterDTO();
        BeanUtils.copyProperties(character, cardCharacterDTO);
        dto.setCardCharacter(cardCharacterDTO);
        
        return dto;
    }
    
    /**
     * 将 Card 转换为 ShopOfferDetailDTO
     */
    private ShopOfferDetailDTO cardToShopOfferDTO(Card card) {
        ShopOfferDetailDTO dto = new ShopOfferDetailDTO();
        dto.setId(card.getId());  // 使用卡牌ID作为商品ID
        dto.setOfferType("card");
        dto.setTargetId(card.getId());
        
        // 解析 shopPrice JSON
        Long price = parseCardShopPrice(card.getShopPrice());
        dto.setPrice(price != null ? price : 0L);
        
        CardDTO cardDTO = new CardDTO();
        BeanUtils.copyProperties(card, cardDTO);
        dto.setCard(cardDTO);
        
        return dto;
    }
    
    /**
     * 将 Item 转换为 ShopOfferDetailDTO
     */
    private ShopOfferDetailDTO itemToShopOfferDTO(Item item) {
        ShopOfferDetailDTO dto = new ShopOfferDetailDTO();
        dto.setId(item.getId());  // 使用道具ID作为商品ID
        dto.setOfferType("item");
        dto.setTargetId(item.getId());
        dto.setPrice(item.getShopPrice() != null ? item.getShopPrice().longValue() : 0L);
        
        ItemDTO itemDTO = new ItemDTO();
        BeanUtils.copyProperties(item, itemDTO);
        dto.setItem(itemDTO);
        
        return dto;
    }
    
    /**
     * 解析 Card 表的 shopPrice JSON 字段
     * 格式：{"currency_type": "gold", "amount": 300}
     */
    private Long parseCardShopPrice(String shopPriceJson) {
        if (shopPriceJson == null || shopPriceJson.trim().isEmpty()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>> typeRef = 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {};
            java.util.Map<String, Object> priceMap = objectMapper.readValue(shopPriceJson, typeRef);
            Object amount = priceMap.get("amount");
            if (amount instanceof Number) {
                return ((Number) amount).longValue();
            }
        } catch (Exception e) {
            System.err.println("解析 shopPrice JSON 失败: " + shopPriceJson + ", error: " + e.getMessage());
        }
        return null;
    }

    /**
     * 刷新指定商店类型的商品（由于现在直接从模板表查询，刷新功能已简化）
     * 注意：由于不再使用shop_offers表，刷新功能实际上只是重新查询，不改变数据
     * 如果需要实现真正的刷新（如随机排序），可以在前端或这里实现排序逻辑
     * @param shopType 商店类型：item-道具商店, card_character-角色商店, spell-法术商店, equipment-装备商店
     * @return 商品列表
     */
    public List<ShopOfferDetailDTO> refreshShop(String shopType) {
        // 由于现在直接从模板表查询，刷新功能实际上只是重新查询
        // 如果需要实现随机排序，可以在这里添加排序逻辑
        List<ShopOfferDetailDTO> offers = getShopOffersByType(shopType);
        
        // 随机打乱顺序（可选）
        Collections.shuffle(offers, new Random());
        
        return offers;
    }

    /**
     * 购买商品（支持直接使用模板表ID购买）
     * @param userId 用户ID
     * @param request 购买请求
     * @return 购买结果信息
     */
    @Transactional
    public String purchaseItem(Long userId, PurchaseRequest request) {
        // 验证请求参数
        if (request == null) {
            throw new RuntimeException("购买请求不能为空");
        }

        Integer quantity = request.getQuantity() != null && request.getQuantity() > 0 
                ? request.getQuantity() : 1;

        String offerType;
        Long targetId;
        Long price;

        // 兼容旧版本：如果提供了shopOfferId，先从shop_offers表查询
        if (request.getShopOfferId() != null) {
            ShopOffer offer = shopOfferMapper.selectById(request.getShopOfferId());
            if (offer == null) {
                throw new RuntimeException("商品不存在");
            }
            offerType = offer.getOfferType();
            targetId = offer.getTargetId();
            price = offer.getPrice();
        } else {
            // 新版本：直接使用模板表ID和类型
            if (request.getOfferType() == null || request.getTargetId() == null) {
                throw new RuntimeException("商品类型和目标ID不能为空");
            }
            offerType = request.getOfferType().trim();
            targetId = request.getTargetId();
            
            // 从模板表获取价格
            price = getPriceFromTemplate(offerType, targetId);
            if (price == null || price <= 0) {
                throw new RuntimeException("商品价格无效或商品不存在");
            }
        }

        // 计算总价
        Long totalPrice = price * quantity;

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
        if ("item".equals(offerType)) {
            // 发放道具
            addItemToInventory(userId, targetId, quantity);
            return String.format("成功购买 %d 个道具", quantity);
        } else if ("card".equals(offerType)) {
            // 发放卡牌（法术/装备）
            addCardToUser(userId, targetId, quantity);
            return String.format("成功购买 %d 张卡牌", quantity);
        } else if ("card_character".equals(offerType)) {
            // 发放角色（卡牌角色）
            addCardCharacterToUser(userId, targetId, quantity);
            return String.format("成功购买 %d 个角色", quantity);
        } else {
            throw new RuntimeException("不支持的商品类型: " + offerType);
        }
    }
    
    /**
     * 从模板表获取商品价格
     */
    private Long getPriceFromTemplate(String offerType, Long targetId) {
        if ("item".equals(offerType)) {
            Item item = itemMapper.selectById(targetId);
            if (item != null && item.getShopPrice() != null) {
                return item.getShopPrice().longValue();
            }
        } else if ("card".equals(offerType)) {
            Card card = cardMapper.selectById(targetId);
            if (card != null) {
                return parseCardShopPrice(card.getShopPrice());
            }
        } else if ("card_character".equals(offerType)) {
            CardCharacter character = cardCharacterMapper.selectById(targetId);
            if (character != null && character.getShopPrice() != null) {
                return character.getShopPrice().longValue();
            }
        }
        return null;
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
     * 购买角色后，只添加到1星角色的数量上
     * 这样确保购买的角色不会添加到更高星级的角色数量上
     */
    private void addCardCharacterToUser(Long userId, Long cardCharacterId, Integer quantity) {
        // 查询角色模板
        CardCharacter template = cardCharacterMapper.selectById(cardCharacterId);
        if (template == null) {
            throw new RuntimeException("角色模板不存在");
        }

        // 查询是否已有该角色的1星记录（购买时只添加到1星角色）
        QueryWrapper<UserCardCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("card_character_id", cardCharacterId)
               .eq("current_star_level", 1);  // 只查询1星角色
        UserCardCharacter existing = userCardCharacterMapper.selectOne(wrapper);

        if (existing != null) {
            // 如果已有1星角色，增加数量
            existing.setQuantity(existing.getQuantity() != null ? existing.getQuantity() + quantity : quantity);
            userCardCharacterMapper.updateById(existing);
        } else {
            // 如果没有，创建新的1星角色记录
            UserCardCharacter userCardCharacter = new UserCardCharacter();
            userCardCharacter.setUserId(userId);
            userCardCharacter.setCardCharacterId(cardCharacterId);
            // 初始化属性
            userCardCharacter.setCurrentHp(template.getBaseHp());
            userCardCharacter.setCurrentArmor(0);
            userCardCharacter.setIsDeployed(false);
            userCardCharacter.setDeployedRound(0);
            userCardCharacter.setCurrentStarLevel(1);  // 购买的角色始终是1星
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

