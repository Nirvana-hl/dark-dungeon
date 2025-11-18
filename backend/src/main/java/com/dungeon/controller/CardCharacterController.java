package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.CardCharacterDTO;
import com.dungeon.service.CardCharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 卡牌角色模板接口
 */
@RestController
@RequestMapping("/card-characters")
public class CardCharacterController {

    @Autowired
    private CardCharacterService cardCharacterService;

    /**
     * 查询全部卡牌角色，可选按职业/阵营筛选
     */
    @GetMapping
    public Result<List<CardCharacterDTO>> getCardCharacters(@RequestParam(required = false) String classType,
                                                            @RequestParam(required = false) String faction) {
        try {
            if (classType != null) {
                return Result.success(cardCharacterService.getByClass(classType));
            }
            if (faction != null) {
                return Result.success(cardCharacterService.getByFaction(faction));
            }
            return Result.success(cardCharacterService.getAllCardCharacters());
        } catch (Exception e) {
            return Result.error("获取卡牌角色失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public Result<CardCharacterDTO> getCardCharacter(@PathVariable Long id) {
        CardCharacterDTO dto = cardCharacterService.getById(id);
        if (dto == null) {
            return Result.error(404, "卡牌角色不存在");
        }
        return Result.success(dto);
    }

    /**
     * 创建卡牌角色模板
     */
    @PostMapping
    public Result<CardCharacterDTO> create(@RequestBody CardCharacterDTO dto) {
        try {
            return Result.success("创建成功", cardCharacterService.create(dto));
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新卡牌角色模板
     */
    @PutMapping("/{id}")
    public Result<CardCharacterDTO> update(@PathVariable Long id, @RequestBody CardCharacterDTO dto) {
        CardCharacterDTO updated = cardCharacterService.update(id, dto);
        if (updated == null) {
            return Result.error(404, "卡牌角色不存在");
        }
        return Result.success("更新成功", updated);
    }

    /**
     * 删除卡牌角色模板
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cardCharacterService.delete(id);
        return Result.success("删除成功", null);
    }
}


