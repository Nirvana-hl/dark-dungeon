package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.UserCardCharacterDTO;
import com.dungeon.service.UserCardCharacterService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户卡牌角色实例接口
 */
@RestController
@RequestMapping("/user-card-characters")
public class UserCardCharacterController {

    @Autowired
    private UserCardCharacterService userCardCharacterService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<UserCardCharacterDTO>> getUserCardCharacters(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(userCardCharacterService.getUserCardCharacters(userId));
    }

    @GetMapping("/deployed")
    public Result<List<UserCardCharacterDTO>> getDeployedCardCharacters(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(userCardCharacterService.getDeployedCardCharacters(userId));
    }

    @GetMapping("/{id}")
    public Result<UserCardCharacterDTO> getUserCardCharacter(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        UserCardCharacterDTO dto = userCardCharacterService.getUserCardCharacterById(id, userId);
        if (dto == null) {
            return Result.error(404, "卡牌角色不存在");
        }
        return Result.success(dto);
    }

    @PostMapping
    public Result<UserCardCharacterDTO> createUserCardCharacter(@RequestBody Map<String, Object> body,
                                                                HttpServletRequest request) {
        Long userId = getUserId(request);
        Object cardCharacterIdObj = body.get("cardCharacterId");
        if (cardCharacterIdObj == null) {
            return Result.error(400, "cardCharacterId 不能为空");
        }
        Long cardCharacterId;
        if (cardCharacterIdObj instanceof Number) {
            cardCharacterId = ((Number) cardCharacterIdObj).longValue();
        } else {
            return Result.error(400, "cardCharacterId 必须是数字");
        }
        try {
            return Result.success("创建成功",
                    userCardCharacterService.createUserCardCharacter(userId, cardCharacterId));
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<UserCardCharacterDTO> updateUserCardCharacter(@PathVariable Long id,
                                                                @RequestBody UserCardCharacterDTO dto,
                                                                HttpServletRequest request) {
        Long userId = getUserId(request);
        UserCardCharacterDTO updated = userCardCharacterService.updateUserCardCharacter(id, userId, dto);
        if (updated == null) {
            return Result.error(404, "卡牌角色不存在");
        }
        return Result.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUserCardCharacter(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        userCardCharacterService.deleteUserCardCharacter(id, userId);
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


