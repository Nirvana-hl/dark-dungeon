package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.EnemyPanelDTO;
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
     * 支持两种查询方式：
     * 1. 通过 enemyId 直接查询指定敌人的卡牌（推荐）
     * 2. 通过 stage 和 difficulty 随机选择一个敌人并返回其卡牌
     * 
     * 示例：
     * GET /api/game/enemy-cards?enemyId=1  （查询敌人ID为1的卡牌）
     * GET /api/game/enemy-cards?stage=1&difficulty=普通  （随机选择敌人）
     * 
     * @param enemyId 敌人ID（可选，如果提供则直接查询该敌人的卡牌）
     * @param stage 关卡编号（可选，如果不提供则仅根据难度查询）
     * @param difficulty 难度（支持中文：普通/困难/噩梦，或英文：easy/normal/hard/boss）
     * @param request HTTP请求（用于获取用户信息，虽然此接口不需要认证，但保留参数以保持一致性）
     * @return 敌人卡牌列表
     */
    @GetMapping("/enemy-cards")
    public Result<List<Map<String, Object>>> getEnemyCards(
            @RequestParam(required = false) Long enemyId,
            @RequestParam(required = false) Integer stage,
            @RequestParam(required = false) String difficulty,
            HttpServletRequest request) {
        try {
            // 如果提供了 enemyId，直接查询该敌人的卡牌
            if (enemyId != null) {
                List<Map<String, Object>> enemyCards = enemyService.getEnemyCardsByEnemyId(enemyId);
                return Result.success(enemyCards);
            }
            
            // 否则，使用原有的逻辑（根据关卡和难度随机选择敌人）
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

    /**
     * 获取关卡中所有可能的敌人列表
     * 用于前端展示关卡中可能出现的所有敌人，然后可以单独查询每个敌人的卡牌
     * 
     * GET /api/game/stage-enemies?stage=1&difficulty=普通
     * 
     * @param stage 关卡编号（可选）
     * @param difficulty 难度（支持中文：普通/困难/噩梦，或英文：easy/normal/hard/boss）
     * @return 敌人列表，包含敌人ID、名称、难度等信息
     */
    @GetMapping("/stage-enemies")
    public Result<List<Map<String, Object>>> getStageEnemies(
            @RequestParam(required = false) Integer stage,
            @RequestParam(required = false) String difficulty) {
        try {
            // 如果没有提供难度，默认使用 normal
            if (difficulty == null || difficulty.trim().isEmpty()) {
                difficulty = "normal";
            }
            
            List<Map<String, Object>> enemies = enemyService.getStageEnemiesList(stage, difficulty);
            return Result.success(enemies);
        } catch (Exception e) {
            return Result.error("获取关卡敌人列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取敌人面板信息
     * 返回敌人的基础属性（生命值、攻击、防御）以及攻击特性
     *
     * 示例：
     * GET /api/game/enemy-panel?enemyId=1
     *
     * 响应 data 示例：
     * {
     *   "id": 1,
     *   "name": "暗影猎手",
     *   "difficulty": "normal",
     *   "hp": 120,
     *   "attack": 18,
     *   "armor": 5,
     *   "attackTraits": ["strike_poison"]
     * }
     *
     * @param enemyId 敌人ID（必填）
     */
    @GetMapping("/enemy-panel")
    public Result<EnemyPanelDTO> getEnemyPanel(@RequestParam Long enemyId) {
        if (enemyId == null) {
            return Result.error("enemyId 不能为空");
        }
        try {
            EnemyPanelDTO panel = enemyService.getEnemyPanel(enemyId);
            if (panel == null) {
                return Result.error("未找到指定的敌人");
            }
            return Result.success(panel);
        } catch (Exception e) {
            return Result.error("获取敌人面板失败: " + e.getMessage());
        }
    }
}
