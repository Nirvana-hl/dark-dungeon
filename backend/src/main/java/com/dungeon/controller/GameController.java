package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.service.GameService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 游戏控制器
 */
@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户卡牌
     */
    @GetMapping("/cards")
    public Result<List<Map<String, Object>>> getUserCards(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<Map<String, Object>> cards = gameService.getUserCards(userId);
            return Result.success(cards);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取敌方卡牌
     */
    @GetMapping("/enemy-cards")
    public Result<List<Map<String, Object>>> getEnemyCards(
            @RequestParam Integer stage,
            @RequestParam String difficulty) {
        try {
            List<Map<String, Object>> cards = gameService.getEnemyCards(stage, difficulty);
            return Result.success(cards);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取角色特性
     */
    @GetMapping("/character-traits")
    public Result<Map<String, Map<String, Object>>> getCharacterTraits() {
        try {
            Map<String, Map<String, Object>> traits = gameService.getCharacterTraits();
            return Result.success(traits);
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
}

