package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.InventoryItemDTO;
import com.dungeon.dto.ItemDTO;
import com.dungeon.dto.UseItemRequest;
import com.dungeon.service.ItemService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 道具控制器
 * 提供道具模板查询、背包管理、道具使用等功能
 */
@RestController
@RequestMapping("/api")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取道具模板列表
     * GET /api/items
     * 
     * @param itemType 道具类型（可选）：consumable-消耗品, material-材料, blueprint-蓝图, currency_bundle-货币包, cosmetic-装饰
     * @return 道具模板列表
     */
    @GetMapping("/items")
    public Result<List<ItemDTO>> getItems(@RequestParam(required = false) String itemType) {
        try {
            List<ItemDTO> items = itemService.getItems(itemType);
            return Result.success(items);
        } catch (Exception e) {
            return Result.error("获取道具模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户背包
     * GET /api/inventory
     * 
     * @param request HTTP请求（用于获取当前用户）
     * @return 背包道具列表
     */
    @GetMapping("/inventory")
    public Result<List<InventoryItemDTO>> getUserInventory(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<InventoryItemDTO> inventory = itemService.getUserInventory(userId);
            return Result.success(inventory);
        } catch (Exception e) {
            return Result.error("获取背包失败: " + e.getMessage());
        }
    }

    /**
     * 使用道具
     * POST /api/inventory/use
     * 
     * @param useRequest 使用请求（包含道具ID和数量）
     * @param request HTTP请求（用于获取当前用户）
     * @return 使用结果信息
     */
    @PostMapping("/inventory/use")
    public Result<String> useItem(@RequestBody UseItemRequest useRequest,
                                   HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            // 验证请求参数
            if (useRequest == null || useRequest.getItemId() == null) {
                return Result.error(400, "道具ID不能为空");
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

