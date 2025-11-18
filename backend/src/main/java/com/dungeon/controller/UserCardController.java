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
        Long userId = getUserId(request);
        return Result.success(userCardService.getUserCards(userId));
    }

    @GetMapping("/equipped")
    public Result<List<UserCardDTO>> getEquippedCards(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(userCardService.getEquippedUserCards(userId));
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


