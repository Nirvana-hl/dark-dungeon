package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.*;
import com.dungeon.service.AISuggestionService;
import com.dungeon.service.CampService;
import com.dungeon.service.EventService;
import com.dungeon.service.ItemService;
import com.dungeon.service.ShopService;
import com.dungeon.service.UserCardCharacterService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 营地数据聚合接口
 */
@RestController
@RequestMapping("/camp")
public class CampController {

    @Autowired
    private CampService campService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserCardCharacterService userCardCharacterService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AISuggestionService aiSuggestionService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取营地仪表盘数据
     */
    @GetMapping("/dashboard")
    public Result<CampDashboardDTO> getDashboard(HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            return Result.success(campService.getDashboard(userId));
        } catch (Exception e) {
            return Result.error("获取营地数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定商店类型的商品列表（最多8个）
     * GET /camp/shop-offers/{shopType}
     * 注意：这个路由必须在 /shop-offers 之前定义，以确保路径变量能正确匹配
     * 
     * @param shopType 商店类型：item-道具商店, card-法术/装备卡牌商店, card_character-卡牌角色商店
     * @return 商品列表（最多8个）
     */
    @GetMapping("/shop-offers/{shopType}")
    public Result<List<ShopOfferDetailDTO>> getShopOffersByType(@PathVariable("shopType") String shopType) {
        try {
            if (shopType == null || shopType.trim().isEmpty()) {
                return Result.error(400, "商店类型不能为空");
            }
            shopType = shopType.trim();
            if (!"item".equals(shopType) && !"card_character".equals(shopType) && !"card".equals(shopType)) {
                return Result.error(400, "不支持的商店类型: " + shopType + "，支持的类型: item, card, card_character");
            }
            List<ShopOfferDetailDTO> offers = shopService.getShopOffersByType(shopType);
            return Result.success(offers);
        } catch (Exception e) {
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            return Result.error("获取商店商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取商城商品列表（兼容旧接口）
     * GET /camp/shop-offers
     * 
     * @return 商品列表（按displayOrder排序）
     */
    @GetMapping("/shop-offers")
    public Result<List<ShopOfferDetailDTO>> getShopOffers() {
        try {
            List<ShopOfferDetailDTO> offers = shopService.getShopOffers();
            return Result.success(offers);
        } catch (Exception e) {
            return Result.error("获取商城商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 刷新指定商店类型的商品
     * POST /camp/refresh-shop/{shopType}
     * 
     * @param shopType 商店类型：item-道具商店, card-法术/装备卡牌商店, card_character-卡牌角色商店
     * @return 刷新后的商品列表（8个）
     */
    @PostMapping("/refresh-shop/{shopType}")
    public Result<List<ShopOfferDetailDTO>> refreshShop(@PathVariable("shopType") String shopType,
                                                         HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (shopType == null || shopType.trim().isEmpty()) {
                return Result.error(400, "商店类型不能为空");
            }
            shopType = shopType.trim();
            if (!"item".equals(shopType) && !"card_character".equals(shopType) && !"card".equals(shopType)) {
                return Result.error(400, "不支持的商店类型: " + shopType);
            }
            List<ShopOfferDetailDTO> offers = shopService.refreshShop(shopType);
            return Result.success(offers);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("刷新商店失败: " + e.getMessage());
        }
    }

    /**
     * 购买商品
     * POST /camp/purchase
     * 
     * @param purchaseRequest 购买请求（包含商品ID和数量）
     * @param request HTTP请求（用于获取当前用户）
     * @return 购买结果信息
     */
    @PostMapping("/purchase")
    public Result<String> purchaseItem(@RequestBody PurchaseRequest purchaseRequest,
                                        HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            System.out.println(String.format("[CampController] ========== 收到购买请求 =========="));
            System.out.println(String.format("[CampController] userId=%d, purchaseRequest=%s", userId, purchaseRequest));
            
            // 验证请求参数
            if (purchaseRequest == null || purchaseRequest.getShopOfferId() == null) {
                System.out.println(String.format("[CampController] ✗ 参数验证失败: purchaseRequest=%s", purchaseRequest));
                return Result.error(400, "商品ID不能为空");
            }

            System.out.println(String.format("[CampController] 调用 ShopService.purchaseItem: userId=%d, shopOfferId=%d, quantity=%d", 
                userId, purchaseRequest.getShopOfferId(), purchaseRequest.getQuantity()));
            
            String message = shopService.purchaseItem(userId, purchaseRequest);
            
            System.out.println(String.format("[CampController] ✓ 购买成功: userId=%d, message=%s", userId, message));
            return Result.success(message, null);
        } catch (RuntimeException e) {
            System.err.println(String.format("[CampController] ✗ 购买失败 (RuntimeException): userId=%d, error=%s", 
                getUserId(request), e.getMessage()));
            e.printStackTrace();
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            System.err.println(String.format("[CampController] ✗ 购买失败 (Exception): userId=%d, error=%s", 
                getUserId(request), e.getMessage()));
            e.printStackTrace();
            return Result.error("购买失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户背包
     * GET /camp/inventory
     * 
     * @param request HTTP请求（用于获取当前用户）
     * @return 背包道具列表
     */
    @GetMapping("/inventory")
    public Result<List<InventoryItemDTO>> getUserInventory(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            List<InventoryItemDTO> inventory = itemService.getUserInventory(userId);
            return Result.success(inventory);
        } catch (Exception e) {
            return Result.error("获取背包失败: " + e.getMessage());
        }
    }

    /**
     * 使用道具
     * POST /camp/use-item
     * 
     * @param useRequest 使用请求（包含背包记录ID和数量）
     * @param request HTTP请求（用于获取当前用户）
     * @return 使用结果信息
     */
    @PostMapping("/use-item")
    public Result<String> useItem(@RequestBody UseItemRequest useRequest,
                                   HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            
            // 验证请求参数
            if (useRequest == null) {
                return Result.error(400, "请求参数不能为空");
            }
            
            // 前端传递的是 inventoryId（字符串），需要转换为 Long
            // 如果前端传递的是字符串格式的 inventoryId，需要处理
            if (useRequest.getInventoryId() == null) {
                return Result.error(400, "背包记录ID不能为空");
            }

            String message = itemService.useItem(userId, useRequest);
            return Result.success(message, null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("使用道具失败: " + e.getMessage());
        }
    }

    /**
     * 部署/撤下卡牌角色
     * POST /camp/deploy-card-character
     * 
     * @param deployRequest 部署请求
     * @param request HTTP请求（用于获取当前用户）
     * @return 更新后的卡牌角色
     */
    @PostMapping("/deploy-card-character")
    public Result<UserCardCharacterDTO> deployCardCharacter(@RequestBody DeployCardCharacterRequest deployRequest,
                                                             HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            
            // 验证请求参数
            if (deployRequest == null || deployRequest.getUserCardCharacterId() == null || deployRequest.getDeploy() == null) {
                return Result.error(400, "卡牌角色ID和部署状态不能为空");
            }

            Long userCardCharacterId = deployRequest.getUserCardCharacterIdAsLong();
            if (userCardCharacterId == null) {
                return Result.error(400, "卡牌角色ID格式错误");
            }

            UserCardCharacterDTO character = userCardCharacterService.deployCardCharacter(
                userId, 
                userCardCharacterId, 
                deployRequest.getDeploy()
            );
            return Result.success(character);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("部署卡牌角色失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务事件
     * GET /camp/events
     * 
     * @return 事件列表
     */
    @GetMapping("/events")
    public Result<List<EventDTO>> getEvents() {
        try {
            List<EventDTO> events = eventService.getCampEvents();
            return Result.success(events);
        } catch (Exception e) {
            return Result.error("获取任务事件失败: " + e.getMessage());
        }
    }

    /**
     * 完成任务事件
     * POST /camp/complete-event
     * 
     * @param completeRequest 完成事件请求
     * @param request HTTP请求（用于获取当前用户）
     * @return 完成结果信息
     */
    @PostMapping("/complete-event")
    public Result<String> completeEvent(@RequestBody CompleteEventRequest completeRequest,
                                        HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            
            // 验证请求参数
            if (completeRequest == null || completeRequest.getEventId() == null) {
                return Result.error(400, "事件ID不能为空");
            }

            Long eventId = completeRequest.getEventIdAsLong();
            if (eventId == null) {
                return Result.error(400, "事件ID格式错误");
            }

            String message = eventService.completeEvent(userId, eventId);
            return Result.success(message, null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("完成任务事件失败: " + e.getMessage());
        }
    }


    /**
     * 刷新AI建议
     * POST /camp/refresh-ai-suggestions
     * 
     * @param request HTTP请求（用于获取当前用户）
     * @return 新的AI建议列表
     */
    @PostMapping("/refresh-ai-suggestions")
    public Result<List<AISuggestionDTO>> refreshAISuggestions(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            List<AISuggestionDTO> suggestions = aiSuggestionService.refreshAISuggestions(userId);
            return Result.success(suggestions);
        } catch (Exception e) {
            return Result.error("刷新AI建议失败: " + e.getMessage());
        }
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未授权");
        }
        return jwtUtil.getUserIdFromTokenAsLong(token.substring(7));
    }
}

