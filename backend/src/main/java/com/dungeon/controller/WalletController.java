package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.AddCurrencyRequest;
import com.dungeon.dto.ConsumeCurrencyRequest;
import com.dungeon.dto.UserWalletDTO;
import com.dungeon.service.WalletService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 钱包控制器
 */
@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取钱包信息
     * GET /wallet/info
     * 
     * @param request HTTP请求（用于获取当前用户）
     * @return 钱包列表
     */
    @GetMapping("/info")
    public Result<List<UserWalletDTO>> getWalletInfo(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            List<UserWalletDTO> wallets = walletService.getUserWallets(userId);
            return Result.success(wallets);
        } catch (Exception e) {
            return Result.error("获取钱包信息失败: " + e.getMessage());
        }
    }

    /**
     * 增加货币
     * POST /wallet/add
     * 
     * @param addRequest 增加货币请求
     * @param request HTTP请求（用于获取当前用户）
     * @return 更新后的钱包信息
     */
    @PostMapping("/add")
    public Result<UserWalletDTO> addCurrency(@RequestBody AddCurrencyRequest addRequest,
                                            HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            
            // 验证请求参数
            if (addRequest == null || addRequest.getCurrencyType() == null || addRequest.getAmount() == null) {
                return Result.error(400, "货币类型和数量不能为空");
            }

            // 转换金额（前端传递的是字符串）
            Long amount;
            try {
                amount = Long.parseLong(addRequest.getAmount());
            } catch (NumberFormatException e) {
                return Result.error(400, "金额格式错误");
            }

            UserWalletDTO wallet = walletService.addCurrency(userId, addRequest.getCurrencyType(), amount);
            return Result.success(wallet);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("增加货币失败: " + e.getMessage());
        }
    }

    /**
     * 消费货币
     * POST /wallet/consume
     * 
     * @param consumeRequest 消费货币请求
     * @param request HTTP请求（用于获取当前用户）
     * @return 更新后的钱包信息
     */
    @PostMapping("/consume")
    public Result<UserWalletDTO> consumeCurrency(@RequestBody ConsumeCurrencyRequest consumeRequest,
                                                  HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            
            // 验证请求参数
            if (consumeRequest == null || consumeRequest.getCurrencyType() == null || consumeRequest.getAmount() == null) {
                return Result.error(400, "货币类型和数量不能为空");
            }

            // 转换金额（前端传递的是字符串）
            Long amount;
            try {
                amount = Long.parseLong(consumeRequest.getAmount());
            } catch (NumberFormatException e) {
                return Result.error(400, "金额格式错误");
            }

            UserWalletDTO wallet = walletService.consumeCurrency(userId, consumeRequest.getCurrencyType(), amount);
            return Result.success(wallet);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("消费货币失败: " + e.getMessage());
        }
    }

    /**
     * 从请求头获取用户ID
     */
    private Long getUserId(HttpServletRequest request) {
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

