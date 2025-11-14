package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.service.WalletService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
     * 获取金币
     */
    @GetMapping("/gold")
    public Result<Integer> getGold(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Integer gold = walletService.getGold(userId);
            return Result.success(gold);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 消费金币
     */
    @PostMapping("/spend")
    public Result<Boolean> spend(@RequestBody SpendRequest requestBody, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            boolean success = walletService.spend(userId, requestBody.getAmount());
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 增加金币
     */
    @PostMapping("/add")
    public Result<Integer> add(@RequestBody AddRequest requestBody, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Integer gold = walletService.add(userId, requestBody.getAmount());
            return Result.success(gold);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未授权");
        }
        token = token.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }

    // 内部类
    public static class SpendRequest {
        private Integer amount;
        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }
    }

    public static class AddRequest {
        private Integer amount;
        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }
    }
}

