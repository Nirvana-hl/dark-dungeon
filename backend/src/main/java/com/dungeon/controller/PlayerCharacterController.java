package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.PlayerCharacterDTO;
import com.dungeon.service.PlayerCharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 玩家角色模板控制器
 */
@RestController
@RequestMapping("/player-characters")
public class PlayerCharacterController {

    @Autowired
    private PlayerCharacterService playerCharacterService;

    /**
     * 获取所有玩家角色模板列表
     * GET /player-characters
     */
    @GetMapping
    public Result<List<PlayerCharacterDTO>> getAllPlayerCharacters() {
        try {
            List<PlayerCharacterDTO> characters = playerCharacterService.getAllPlayerCharacters();
            return Result.success(characters);
        } catch (Exception e) {
            return Result.error("获取角色模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取玩家角色模板
     * GET /player-characters/{id}
     */
    @GetMapping("/{id}")
    public Result<PlayerCharacterDTO> getPlayerCharacterById(@PathVariable Long id) {
        try {
            PlayerCharacterDTO character = playerCharacterService.getPlayerCharacterById(id);
            if (character == null) {
                return Result.error(404, "角色模板不存在");
            }
            return Result.success(character);
        } catch (Exception e) {
            return Result.error("获取角色模板失败: " + e.getMessage());
        }
    }

    /**
     * 根据代码获取玩家角色模板
     * GET /player-characters/code/{code}
     */
    @GetMapping("/code/{code}")
    public Result<PlayerCharacterDTO> getPlayerCharacterByCode(@PathVariable String code) {
        try {
            PlayerCharacterDTO character = playerCharacterService.getPlayerCharacterByCode(code);
            if (character == null) {
                return Result.error(404, "角色模板不存在");
            }
            return Result.success(character);
        } catch (Exception e) {
            return Result.error("获取角色模板失败: " + e.getMessage());
        }
    }
}

