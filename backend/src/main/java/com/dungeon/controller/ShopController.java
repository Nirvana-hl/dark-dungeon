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
     * 获取商城商品列表
     * GET /api/shop/offers
     * 
     * @return 商品列表（按displayOrder排序）
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
     * 购买商品
     * POST /api/shop/purchase
     * 
     * @param purchaseRequest 购买请求（包含商品ID和数量）
     * @param request HTTP请求（用于获取当前用户）
     * @return 购买结果信息
     */
    @PostMapping("/purchase")
    public Result<String> purchaseItem(@RequestBody PurchaseRequest purchaseRequest,
                                        HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            // 验证请求参数
            if (purchaseRequest == null || purchaseRequest.getShopOfferId() == null) {
                return Result.error(400, "商品ID不能为空");
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

