package com.dungeon.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.InventoryItemDTO;
import com.dungeon.dto.ItemDTO;
import com.dungeon.dto.UseItemRequest;
import com.dungeon.entity.Inventory;
import com.dungeon.entity.Item;
import com.dungeon.entity.UserPlayerCharacter;
import com.dungeon.mapper.InventoryMapper;
import com.dungeon.mapper.ItemMapper;
import com.dungeon.mapper.UserPlayerCharacterMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 道具服务
 * 负责道具模板查询、背包管理、道具使用逻辑
 */
@Service
public class ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private UserPlayerCharacterMapper userPlayerCharacterMapper;

    /**
     * 获取所有道具模板
     * @param itemType 道具类型（可选，为空则返回所有）
     * @return 道具模板列表
     */
    public List<ItemDTO> getItems(String itemType) {
        QueryWrapper<Item> wrapper = new QueryWrapper<>();
        if (itemType != null && !itemType.trim().isEmpty()) {
            wrapper.eq("item_type", itemType);
        }
        List<Item> items = itemMapper.selectList(wrapper);

        return items.stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户背包
     * @param userId 用户ID
     * @return 背包道具列表
     */
    public List<InventoryItemDTO> getUserInventory(Long userId) {
        QueryWrapper<Inventory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<Inventory> inventories = inventoryMapper.selectList(wrapper);

        return inventories.stream()
                .map(this::toInventoryItemDTO)
                .collect(Collectors.toList());
    }

    /**
     * 使用道具
     * @param userId 用户ID
     * @param request 使用请求
     * @return 使用结果信息
     */
    @Transactional
    public String useItem(Long userId, UseItemRequest request) {
        // 验证请求参数
        if (request == null) {
            throw new RuntimeException("请求参数不能为空");
        }

        Integer quantity = request.getQuantity() != null && request.getQuantity() > 0 
                ? request.getQuantity() : 1;

        Inventory inventory;
        Item item;

        // 支持通过 inventoryId 或 itemId 使用物品
        if (request.getInventoryId() != null) {
            // 通过背包记录ID查找
            inventory = inventoryMapper.selectById(request.getInventoryId());
            if (inventory == null) {
                throw new RuntimeException("背包记录不存在");
            }
            // 验证背包记录属于当前用户
            if (!inventory.getUserId().equals(userId)) {
                throw new RuntimeException("无权使用该道具");
            }
            // 查询道具模板
            item = itemMapper.selectById(inventory.getItemId());
        } else if (request.getItemId() != null) {
            // 通过道具模板ID查找（原有逻辑）
            item = itemMapper.selectById(request.getItemId());
            if (item == null) {
                throw new RuntimeException("道具不存在");
            }
            // 查询用户背包
            QueryWrapper<Inventory> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .eq("item_id", request.getItemId());
            inventory = inventoryMapper.selectOne(wrapper);
        } else {
            throw new RuntimeException("道具ID或背包记录ID不能为空");
        }

        if (inventory == null || inventory.getQuantity() < quantity) {
            throw new RuntimeException("道具数量不足");
        }

        // 检查道具类型（只有消耗品可以使用）
        if (!"consumable".equals(item.getItemType())) {
            throw new RuntimeException("该道具不能使用");
        }

        // 获取用户角色实例（用于应用效果）
        QueryWrapper<UserPlayerCharacter> characterWrapper = new QueryWrapper<>();
        characterWrapper.eq("user_id", userId)
                       .last("LIMIT 1");
        UserPlayerCharacter character = userPlayerCharacterMapper.selectOne(characterWrapper);

        if (character == null) {
            throw new RuntimeException("未创建角色，无法使用道具");
        }

        // 应用道具效果
        applyItemEffect(character, item, quantity);

        // 扣除道具数量
        inventory.setQuantity(inventory.getQuantity() - quantity);
        if (inventory.getQuantity() <= 0) {
            // 如果数量为0，删除记录
            inventoryMapper.deleteById(inventory.getId());
        } else {
            inventory.setLastUpdatedAt(LocalDateTime.now());
            inventoryMapper.updateById(inventory);
        }

        return String.format("成功使用 %d 个 %s", quantity, item.getName());
    }

    /**
     * 应用道具效果
     */
    private void applyItemEffect(UserPlayerCharacter character, Item item, Integer quantity) {
        if (item.getEffectPayload() == null || item.getEffectPayload().isEmpty()) {
            return;
        }

        try {
            JSONObject effect = JSON.parseObject(item.getEffectPayload());

            // 治疗效果
            if (effect.containsKey("heal")) {
                Integer healAmount = effect.getInteger("heal") * quantity;
                int newHp = Math.min(character.getCurrentHp() + healAmount, character.getMaxHp());
                character.setCurrentHp(newHp);
            }

            // 压力减少效果
            if (effect.containsKey("stress_reduce")) {
                Integer stressReduce = effect.getInteger("stress_reduce") * quantity;
                int newStress = Math.max(character.getCurrentStress() - stressReduce, 0);
                character.setCurrentStress(newStress);
                
                // 更新压力层级
                int stressLevel = calculateStressLevel(newStress);
                character.setStressLevel(stressLevel);
            }

            // 更新角色状态
            userPlayerCharacterMapper.updateById(character);
        } catch (Exception e) {
            // JSON解析失败，忽略效果
        }
    }

    /**
     * 计算压力层级
     */
    private int calculateStressLevel(int stress) {
        if (stress <= 25) return 1;
        if (stress <= 50) return 2;
        if (stress <= 75) return 3;
        return 4;
    }

    /**
     * 道具实体转DTO
     */
    private ItemDTO toItemDTO(Item item) {
        ItemDTO dto = new ItemDTO();
        BeanUtils.copyProperties(item, dto);
        return dto;
    }

    /**
     * 背包实体转DTO
     */
    private InventoryItemDTO toInventoryItemDTO(Inventory inventory) {
        InventoryItemDTO dto = new InventoryItemDTO();
        BeanUtils.copyProperties(inventory, dto);
        return dto;
    }
}

