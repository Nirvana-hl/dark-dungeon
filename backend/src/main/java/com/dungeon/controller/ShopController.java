package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.PurchaseRequest;
import com.dungeon.dto.ShopOfferDetailDTO;
import com.dungeon.service.ShopService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 商城控制器
 * 提供商城商品查询、购买等功能
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取商城商品列表（所有商品）
     * GET /shop/offers
     * 
     * @return 商品列表
     */
    @GetMapping("/offers")
    public Result<List<ShopOfferDetailDTO>> getShopOffers() {
        try {
            List<ShopOfferDetailDTO> offers = shopService.getShopOffers();
            return Result.success(offers);
        } catch (Exception e) {
            return Result.error("获取商城商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定商店类型的商品列表（直接从模板表查询）
     * GET /shop/offers/{shopType}
     * 
     * @param shopType 商店类型：item-道具商店, card_character-角色商店, spell-法术商店, equipment-装备商店
     * @return 商品列表（只返回有shopPrice的商品）
     */
    @GetMapping("/offers/{shopType}")
    public Result<List<ShopOfferDetailDTO>> getShopOffersByType(@PathVariable("shopType") String shopType) {
        try {
            if (shopType == null || shopType.trim().isEmpty()) {
                return Result.error(400, "商店类型不能为空");
            }
            shopType = shopType.trim();
            // 支持新的商店类型：item, card_character, spell, equipment
            if (!"item".equals(shopType) && !"card_character".equals(shopType) 
                    && !"spell".equals(shopType) && !"equipment".equals(shopType)) {
                return Result.error(400, "不支持的商店类型: " + shopType + "，支持的类型: item, card_character, spell, equipment");
            }
            List<ShopOfferDetailDTO> offers = shopService.getShopOffersByType(shopType);
            return Result.success(offers);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取商店商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 刷新指定商店类型的商品（随机打乱顺序）
     * POST /shop/refresh/{shopType}
     * 
     * @param shopType 商店类型：item-道具商店, card_character-角色商店, spell-法术商店, equipment-装备商店
     * @param request HTTP请求（用于获取当前用户）
     * @return 刷新后的商品列表（随机排序）
     */
    @PostMapping("/refresh/{shopType}")
    public Result<List<ShopOfferDetailDTO>> refreshShop(@PathVariable("shopType") String shopType,
                                                         HttpServletRequest request) {
        try {
            // 验证用户登录
            getUserIdFromRequest(request);
            if (shopType == null || shopType.trim().isEmpty()) {
                return Result.error(400, "商店类型不能为空");
            }
            shopType = shopType.trim();
            // 支持新的商店类型：item, card_character, spell, equipment
            if (!"item".equals(shopType) && !"card_character".equals(shopType) 
                    && !"spell".equals(shopType) && !"equipment".equals(shopType)) {
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
     * 购买商品（支持新版本：直接使用模板表ID和类型）
     * POST /shop/purchase
     * 
     * @param purchaseRequest 购买请求（支持新版本：offerType + targetId，或旧版本：shopOfferId）
     * @param request HTTP请求（用于获取当前用户）
     * @return 购买结果信息
     */
    @PostMapping("/purchase")
    public Result<String> purchaseItem(@RequestBody PurchaseRequest purchaseRequest,
                                        HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            // 验证请求参数（支持新版本：直接使用模板表ID和类型）
            if (purchaseRequest == null) {
                return Result.error(400, "购买请求不能为空");
            }
            // 新版本：使用 offerType 和 targetId
            if (purchaseRequest.getOfferType() != null && purchaseRequest.getTargetId() != null) {
                // 参数验证通过，继续处理
            } 
            // 兼容旧版本：使用 shopOfferId
            else if (purchaseRequest.getShopOfferId() == null) {
                return Result.error(400, "商品ID或商品类型和目标ID不能为空");
            }

            String message = shopService.purchaseItem(userId, purchaseRequest);
            return Result.success(message, null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("购买失败: " + e.getMessage());
        }
    }

    /**
     * 从请求头获取用户ID
     * 
     * @param request HTTP请求
     * @return 用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未授权，请先登录");
        }
        token = token.substring(7);
        Long userId = jwtUtil.getUserIdFromTokenAsLong(token);
        if (userId == null) {
            throw new RuntimeException("无法获取用户ID");
        }
        return userId;
    }
}

