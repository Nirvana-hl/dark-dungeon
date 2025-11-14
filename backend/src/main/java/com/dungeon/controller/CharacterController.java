package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.CharacterDTO;
import com.dungeon.service.CharacterService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/characters")
public class CharacterController {

    @Autowired
    private CharacterService characterService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户的所有角色
     */
    @GetMapping
    public Result<List<CharacterDTO>> getCharacters(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<CharacterDTO> characters = characterService.getCharactersByUserId(userId);
            return Result.success(characters);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建角色
     */
    @PostMapping
    public Result<CharacterDTO> createCharacter(@RequestBody CharacterDTO dto, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            CharacterDTO character = characterService.createCharacter(userId, dto);
            return Result.success("创建成功", character);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCharacter(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            characterService.deleteCharacter(id, userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 角色升星
     */
    @PostMapping("/{id}/star-up")
    public Result<CharacterDTO> starUp(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            CharacterDTO character = characterService.starUp(id, userId);
            return Result.success("升星成功", character);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从请求头获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未授权");
        }
        token = token.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }
}

