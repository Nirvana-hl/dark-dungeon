package com.dungeon.service;

import com.dungeon.entity.CharacterTrait;
import com.dungeon.entity.EnemyCard;
import com.dungeon.entity.UserCard;
import com.dungeon.mapper.CharacterTraitMapper;
import com.dungeon.mapper.EnemyCardMapper;
import com.dungeon.mapper.UserCardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 游戏服务
 */
@Service
public class GameService {

    @Autowired
    private UserCardMapper userCardMapper;
    
    @Autowired
    private EnemyCardMapper enemyCardMapper;
    
    @Autowired
    private CharacterTraitMapper characterTraitMapper;

    /**
     * 获取用户卡牌
     */
    public List<Map<String, Object>> getUserCards(Long userId) {
        List<UserCard> cards = userCardMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserCard>()
                .eq("user_id", userId)
        );
        return cards.stream().map(card -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", card.getName());
            map.put("type", card.getType());
            map.put("quantity", card.getQuantity());
            map.put("attack", card.getAttack());
            map.put("health", card.getHealth());
            map.put("effect", card.getEffect());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 获取敌方卡牌
     */
    public List<Map<String, Object>> getEnemyCards(Integer stage, String difficulty) {
        List<EnemyCard> cards = enemyCardMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<EnemyCard>()
                .eq("stage", stage)
                .eq("difficulty", difficulty)
        );
        return cards.stream().map(card -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", card.getName());
            map.put("type", card.getType());
            map.put("attack", card.getAttack());
            map.put("health", card.getHealth());
            map.put("effect", card.getEffect());
            map.put("unique_play", card.getUniquePlay() == 1);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 获取角色特性
     */
    public Map<String, Map<String, Object>> getCharacterTraits() {
        List<CharacterTrait> traits = characterTraitMapper.selectList(null);
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (CharacterTrait trait : traits) {
            Map<String, Object> map = new HashMap<>();
            map.put("trait_key", trait.getTraitKey());
            map.put("base_power", trait.getBasePower());
            map.put("power_per_star", trait.getPowerPerStar());
            map.put("description", trait.getDescription());
            result.put(trait.getName(), map);
        }
        return result;
    }
}

