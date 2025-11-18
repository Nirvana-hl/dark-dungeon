package com.dungeon.controller;

import com.dungeon.common.Result;
import com.dungeon.dto.CardDTO;
import com.dungeon.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 卡牌（法术/装备）接口
 */
@RestController
@RequestMapping("/cards")
public class CardController {

    @Autowired
    private CardService cardService;

    @GetMapping
    public Result<List<CardDTO>> getCards(@RequestParam(required = false) String cardType,
                                          @RequestParam(required = false) String rarity) {
        try {
            if (cardType != null) {
                return Result.success(cardService.getByType(cardType));
            }
            if (rarity != null) {
                return Result.success(cardService.getByRarity(rarity));
            }
            return Result.success(cardService.getAllCards());
        } catch (Exception e) {
            return Result.error("获取卡牌列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<CardDTO> getCard(@PathVariable Long id) {
        CardDTO dto = cardService.getById(id);
        if (dto == null) {
            return Result.error(404, "卡牌不存在");
        }
        return Result.success(dto);
    }

    @PostMapping
    public Result<CardDTO> createCard(@RequestBody CardDTO dto) {
        try {
            return Result.success("创建成功", cardService.create(dto));
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<CardDTO> updateCard(@PathVariable Long id, @RequestBody CardDTO dto) {
        CardDTO updated = cardService.update(id, dto);
        if (updated == null) {
            return Result.error(404, "卡牌不存在");
        }
        return Result.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCard(@PathVariable Long id) {
        cardService.delete(id);
        return Result.success("删除成功", null);
    }
}


