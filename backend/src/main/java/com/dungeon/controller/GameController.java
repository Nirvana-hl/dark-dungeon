package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.service.EnemyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 游戏相关接口控制器
 * 提供游戏过程中需要的各种数据接口
 */
@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private EnemyService enemyService;

    /**
     * 获取敌人卡牌列表
     * 根据关卡编号和难度返回该关卡可能出现的敌人卡牌
     * 
     * GET /api/game/enemy-cards?stage=1&difficulty=普通
     * 
     * @param stage 关卡编号（可选，如果不提供则仅根据难度查询）
     * @param difficulty 难度（支持中文：普通/困难/噩梦，或英文：easy/normal/hard/boss）
     * @param request HTTP请求（用于获取用户信息，虽然此接口不需要认证，但保留参数以保持一致性）
     * @return 敌人卡牌列表
     */
    @GetMapping("/enemy-cards")
    public Result<List<Map<String, Object>>> getEnemyCards(
            @RequestParam(required = false) Integer stage,
            @RequestParam(required = false) String difficulty,
            HttpServletRequest request) {
        try {
            // 如果没有提供难度，默认使用 normal
            if (difficulty == null || difficulty.trim().isEmpty()) {
                difficulty = "normal";
            }
            
            List<Map<String, Object>> enemyCards = enemyService.getEnemyCardsByStageAndDifficulty(stage, difficulty);
            return Result.success(enemyCards);
        } catch (Exception e) {
            return Result.error("获取敌人卡牌失败: " + e.getMessage());
        }
    }
}

