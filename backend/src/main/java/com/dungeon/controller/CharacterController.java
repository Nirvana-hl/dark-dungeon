package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.CardCharacterDTO;
import com.dungeon.dto.CharacterTraitDTO;
import com.dungeon.dto.PlayerCharacterDTO;
import com.dungeon.dto.UserCardCharacterDTO;
import com.dungeon.dto.UserPlayerCharacterDTO;
import com.dungeon.service.CardCharacterService;
import com.dungeon.service.CharacterTraitService;
import com.dungeon.service.PlayerCharacterService;
import com.dungeon.service.UserCardCharacterService;
import com.dungeon.service.UserPlayerCharacterService;
import com.dungeon.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 角色相关接口
 * 提供角色模板和实例的统一访问接口
 */
@RestController
@RequestMapping("/character")
public class CharacterController {

    @Autowired
    private CharacterTraitService characterTraitService;

    @Autowired
    private UserPlayerCharacterService userPlayerCharacterService;

    @Autowired
    private UserCardCharacterService userCardCharacterService;

    @Autowired
    private PlayerCharacterService playerCharacterService;

    @Autowired
    private CardCharacterService cardCharacterService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取角色特性列表
     * GET /character/traits
     * 
     * @return 角色特性映射表（key为traitKey，value为特性信息）
     */
    @GetMapping("/traits")
    public Result<Map<String, CharacterTraitDTO>> getCharacterTraits() {
        try {
            Map<String, CharacterTraitDTO> traits = characterTraitService.getAllTraits();
            return Result.success(traits);
        } catch (Exception e) {
            return Result.error("获取角色特性失败: " + e.getMessage());
        }
    }

    /**
     * 获取玩家角色实例
     * GET /character/player/instance
     * 
     * @param request HTTP请求
     * @return 玩家角色实例
     */
    @GetMapping("/player/instance")
    public Result<UserPlayerCharacterDTO> getPlayerCharacterInstance(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            UserPlayerCharacterDTO character = userPlayerCharacterService.getUserPlayerCharacter(userId);
            if (character == null) {
                return Result.error(404, "未创建角色");
            }
            return Result.success(character);
        } catch (Exception e) {
            return Result.error("获取玩家角色实例失败: " + e.getMessage());
        }
    }

    /**
     * 获取卡牌角色实例列表
     * GET /character/card/instance
     * 
     * @param request HTTP请求
     * @return 卡牌角色实例列表
     */
    @GetMapping("/card/instance")
    public Result<List<UserCardCharacterDTO>> getCardCharacterInstances(HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            List<UserCardCharacterDTO> characters = userCardCharacterService.getUserCardCharacters(userId);
            return Result.success(characters);
        } catch (Exception e) {
            return Result.error("获取卡牌角色实例失败: " + e.getMessage());
        }
    }

    /**
     * 获取卡牌角色模板列表
     * GET /character/card
     * 
     * @return 卡牌角色模板列表
     */
    @GetMapping("/card")
    public Result<List<CardCharacterDTO>> getCardCharacters(@RequestParam(required = false) String classType,
                                                            @RequestParam(required = false) String faction) {
        try {
            List<CardCharacterDTO> characters;
            if (classType != null) {
                characters = cardCharacterService.getByClass(classType);
            } else if (faction != null) {
                characters = cardCharacterService.getByFaction(faction);
            } else {
                characters = cardCharacterService.getAllCardCharacters();
            }
            return Result.success(characters);
        } catch (Exception e) {
            return Result.error("获取卡牌角色模板失败: " + e.getMessage());
        }
    }

    /**
     * 获取玩家角色模板列表
     * GET /character/player
     * 
     * @return 玩家角色模板列表
     */
    @GetMapping("/player")
    public Result<List<PlayerCharacterDTO>> getPlayerCharacters() {
        try {
            List<PlayerCharacterDTO> characters = playerCharacterService.getAllPlayerCharacters();
            return Result.success(characters);
        } catch (Exception e) {
            return Result.error("获取玩家角色模板失败: " + e.getMessage());
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

