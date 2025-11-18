package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.UserPlayerCharacterDTO;
import com.dungeon.service.UserPlayerCharacterService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 玩家角色实例控制器
 */
@RestController
@RequestMapping("/user-player-characters")
public class UserPlayerCharacterController {

    @Autowired
    private UserPlayerCharacterService userPlayerCharacterService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户的所有角色实例
     * GET /user-player-characters
     */
    @GetMapping
    public Result<List<UserPlayerCharacterDTO>> getUserPlayerCharacters(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<UserPlayerCharacterDTO> characters = userPlayerCharacterService.getUserPlayerCharacters(userId);
            return Result.success(characters);
        } catch (Exception e) {
            return Result.error("获取角色列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的角色实例（单个，通常只有一个）
     * GET /user-player-characters/current
     */
    @GetMapping("/current")
    public Result<UserPlayerCharacterDTO> getCurrentUserPlayerCharacter(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            UserPlayerCharacterDTO character = userPlayerCharacterService.getUserPlayerCharacter(userId);
            if (character == null) {
                return Result.error(404, "未创建角色");
            }
            return Result.success(character);
        } catch (Exception e) {
            return Result.error("获取角色失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取角色实例
     * GET /user-player-characters/{id}
     */
    @GetMapping("/{id}")
    public Result<UserPlayerCharacterDTO> getUserPlayerCharacterById(@PathVariable Long id, 
                                                                     HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            UserPlayerCharacterDTO character = userPlayerCharacterService.getUserPlayerCharacterById(id, userId);
            if (character == null) {
                return Result.error(404, "角色不存在");
            }
            return Result.success(character);
        } catch (Exception e) {
            return Result.error("获取角色失败: " + e.getMessage());
        }
    }

    /**
     * 创建角色实例
     * POST /user-player-characters
     * Body: { "playerCharacterId": 1 }
     */
    @PostMapping
    public Result<UserPlayerCharacterDTO> createUserPlayerCharacter(@RequestBody Map<String, Object> requestBody,
                                                                    HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Object playerCharacterIdObj = requestBody.get("playerCharacterId");
            
            if (playerCharacterIdObj == null) {
                return Result.error(400, "playerCharacterId不能为空");
            }
            
            Long playerCharacterId;
            if (playerCharacterIdObj instanceof Number) {
                playerCharacterId = ((Number) playerCharacterIdObj).longValue();
            } else {
                return Result.error(400, "playerCharacterId必须是数字");
            }

            UserPlayerCharacterDTO character = userPlayerCharacterService.createUserPlayerCharacter(userId, playerCharacterId);
            return Result.success("创建角色成功", character);
        } catch (Exception e) {
            return Result.error("创建角色失败: " + e.getMessage());
        }
    }

    /**
     * 更新角色状态（血量、行动点、压力等）
     * PUT /user-player-characters/{id}
     * Body: { "currentHp": 80, "currentActionPoints": 2, "currentStress": 25 }
     */
    @PutMapping("/{id}")
    public Result<UserPlayerCharacterDTO> updateUserPlayerCharacter(@PathVariable Long id,
                                                                    @RequestBody Map<String, Integer> requestBody,
                                                                    HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Integer currentHp = requestBody.get("currentHp");
            Integer currentActionPoints = requestBody.get("currentActionPoints");
            Integer currentStress = requestBody.get("currentStress");

            UserPlayerCharacterDTO character = userPlayerCharacterService.updateUserPlayerCharacter(
                    id, userId, currentHp, currentActionPoints, currentStress);
            return Result.success("更新角色状态成功", character);
        } catch (Exception e) {
            return Result.error("更新角色状态失败: " + e.getMessage());
        }
    }

    /**
     * 重置行动点（每轮开始时调用）
     * POST /user-player-characters/{id}/reset-action-points
     */
    @PostMapping("/{id}/reset-action-points")
    public Result<Void> resetActionPoints(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            userPlayerCharacterService.resetActionPoints(id, userId);
            return Result.success("重置行动点成功", null);
        } catch (Exception e) {
            return Result.error("重置行动点失败: " + e.getMessage());
        }
    }

    /**
     * 恢复血量
     * POST /user-player-characters/{id}/heal
     * Body: { "healAmount": 20 }
     */
    @PostMapping("/{id}/heal")
    public Result<UserPlayerCharacterDTO> heal(@PathVariable Long id,
                                               @RequestBody Map<String, Integer> requestBody,
                                               HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Integer healAmount = requestBody.get("healAmount");
            if (healAmount == null || healAmount <= 0) {
                return Result.error(400, "healAmount必须大于0");
            }

            userPlayerCharacterService.heal(id, userId, healAmount);
            UserPlayerCharacterDTO character = userPlayerCharacterService.getUserPlayerCharacterById(id, userId);
            return Result.success("恢复血量成功", character);
        } catch (Exception e) {
            return Result.error("恢复血量失败: " + e.getMessage());
        }
    }

    /**
     * 从请求头获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未授权，请先登录");
        }
        token = token.substring(7);
        String userIdStr = jwtUtil.getUserIdFromToken(token);
        if (userIdStr == null) {
            throw new RuntimeException("无法获取用户ID");
        }
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("用户ID格式错误");
        }
    }
}

