package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.UserCardDTO;
import com.dungeon.service.UserCardService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户手牌接口
 * 支持两种路径：
 * - /user-cards (原有路径)
 * - /card/user-cards (前端调用路径)
 */
@RestController
@RequestMapping("/user-cards")
public class UserCardController {

    @Autowired
    private UserCardService userCardService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<UserCardDTO>> getUserCards(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            return Result.success(userCardService.getUserCards(userId));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("获取用户卡牌失败: " + e.getMessage());
        }
    }

    @GetMapping("/equipped")
    public Result<List<UserCardDTO>> getEquippedCards(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            return Result.success(userCardService.getEquippedUserCards(userId));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("获取已装备卡牌失败: " + e.getMessage());
        }
    }

    /**
     * 获取卡组中的卡牌
     * GET /user-cards/deck?loadoutId={loadoutId}
     * 如果不传loadoutId，返回所有有loadoutId的卡牌（默认卡组）
     */
    @GetMapping("/deck")
    public Result<List<UserCardDTO>> getDeckCards(@RequestParam(required = false) Long loadoutId,
                                                    HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            // 如果没有指定loadoutId，默认使用1
            if (loadoutId == null) {
                loadoutId = 1L;
            }
            System.out.println(String.format("[UserCardController] 获取卡组卡牌: userId=%d, loadoutId=%d", userId, loadoutId));
            List<UserCardDTO> deckCards = userCardService.getDeckCards(userId, loadoutId);
            System.out.println(String.format("[UserCardController] 卡组卡牌查询完成: userId=%d, loadoutId=%d, count=%d", 
                userId, loadoutId, deckCards.size()));
            return Result.success(deckCards);
        } catch (RuntimeException e) {
            System.err.println(String.format("[UserCardController] 获取卡组卡牌失败 (RuntimeException): %s", e.getMessage()));
            e.printStackTrace();
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            System.err.println(String.format("[UserCardController] 获取卡组卡牌失败 (Exception): %s", e.getMessage()));
            e.printStackTrace();
            return Result.error("获取卡组卡牌失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<UserCardDTO> getUserCard(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        UserCardDTO dto = userCardService.getUserCard(id, userId);
        if (dto == null) {
            return Result.error(404, "卡牌不存在");
        }
        return Result.success(dto);
    }

    @PostMapping
    public Result<UserCardDTO> createUserCard(@RequestBody UserCardDTO dto, HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            return Result.success("创建成功", userCardService.createUserCard(userId, dto));
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<UserCardDTO> updateUserCard(@PathVariable Long id,
                                              @RequestBody UserCardDTO dto,
                                              HttpServletRequest request) {
        Long userId = getUserId(request);
        UserCardDTO updated = userCardService.updateUserCard(id, userId, dto);
        if (updated == null) {
            return Result.error(404, "卡牌不存在");
        }
        return Result.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUserCard(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        userCardService.deleteUserCard(id, userId);
        return Result.success("删除成功", null);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未授权");
        }
        return jwtUtil.getUserIdFromTokenAsLong(token.substring(7));
    }
}


