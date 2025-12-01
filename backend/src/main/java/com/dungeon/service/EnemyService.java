package com.dungeon.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.entity.Card;
import com.dungeon.entity.Enemy;
import com.dungeon.entity.EnemyCard;
import com.dungeon.entity.Stage;
import com.dungeon.mapper.CardMapper;
import com.dungeon.mapper.EnemyCardMapper;
import com.dungeon.mapper.EnemyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 敌人服务
 * 负责敌人相关业务逻辑，包括敌人卡牌查询
 */
@Service
public class EnemyService {

    @Autowired
    private EnemyMapper enemyMapper;

    @Autowired
    private EnemyCardMapper enemyCardMapper;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private StageService stageService;

    private static final Random RANDOM = new Random();

    /**
     * 根据关卡和难度获取敌人卡牌列表
     * 
     * @param stageNumber 关卡编号
     * @param difficulty 难度（支持中文：普通/困难/噩梦，或英文：easy/normal/hard/boss）
     * @return 敌人卡牌列表
     */
    public List<Map<String, Object>> getEnemyCardsByStageAndDifficulty(Integer stageNumber, String difficulty) {
        // 1. 根据关卡编号查询关卡信息
        Stage stage = null;
        if (stageNumber != null) {
            stage = stageService.getStageByNumber(stageNumber);
        }

        // 2. 选择敌人
        Enemy enemy = selectEnemyForStage(stage, difficulty);
        if (enemy == null) {
            // 如果没有找到敌人，返回空列表
            return new ArrayList<>();
        }

        return getEnemyCardsByEnemyId(enemy.getId());
    }

    /**
     * 根据敌人ID获取卡牌列表
     */
    public List<Map<String, Object>> getEnemyCardsByEnemyId(Long enemyId) {
        if (enemyId == null) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<EnemyCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnemyCard::getEnemyId, enemyId);
        List<EnemyCard> enemyCards = enemyCardMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(enemyCards)) {
            return new ArrayList<>();
        }

        List<Long> cardIds = enemyCards.stream()
                .map(EnemyCard::getCardId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (cardIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Card> cards = cardMapper.selectBatchIds(cardIds);
        if (CollectionUtils.isEmpty(cards)) {
            return new ArrayList<>();
        }

        return convertToEnemyCardDTOList(cards);
    }

    /**
     * 根据关卡和难度选择敌人
     * 优先级：关卡敌人池 > 难度匹配 > 任意敌人
     */
    public Enemy selectEnemyForStage(Stage stage, String difficulty) {
        // 优先从关卡的敌人池中选择
        if (stage != null && StringUtils.hasText(stage.getEnemyPool())) {
            List<WeightedEnemy> weightedEnemies = parseEnemyPool(stage.getEnemyPool());
            if (!CollectionUtils.isEmpty(weightedEnemies)) {
                WeightedEnemy pick = pickEnemyByWeight(weightedEnemies);
                if (pick != null && pick.getEnemyId() != null) {
                    Enemy enemy = enemyMapper.selectById(pick.getEnemyId());
                    if (enemy != null) {
                        return enemy;
                    }
                }
            }
        }

        // 如果没有敌人池或解析失败，根据难度查询
        String normalizedDifficulty = normalizeDifficulty(difficulty);
        if (normalizedDifficulty != null) {
            LambdaQueryWrapper<Enemy> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enemy::getDifficulty, normalizedDifficulty);
            List<Enemy> candidates = enemyMapper.selectList(wrapper);
            
            if (!CollectionUtils.isEmpty(candidates)) {
                return candidates.get(RANDOM.nextInt(candidates.size()));
            }
        }

        // 如果仍然没有找到，返回任意一个敌人
        List<Enemy> allEnemies = enemyMapper.selectList(new QueryWrapper<>());
        if (!CollectionUtils.isEmpty(allEnemies)) {
            return allEnemies.get(RANDOM.nextInt(allEnemies.size()));
        }

        return null;
    }

    /**
     * 将难度字符串标准化为数据库中的格式
     * 支持中文：普通/困难/噩梦 -> normal/hard/boss
     * 支持英文：easy/normal/hard/boss
     */
    private String normalizeDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            return "normal";
        }

        String lower = difficulty.toLowerCase().trim();
        
        // 中文转英文
        switch (lower) {
            case "普通":
            case "normal":
                return "normal";
            case "困难":
            case "hard":
                return "hard";
            case "噩梦":
            case "expert":
                return "hard"; // 噩梦对应hard
            case "简单":
            case "easy":
                return "easy";
            case "boss":
            case "首领":
                return "boss";
            default:
                return "normal";
        }
    }

    /**
     * 将Card实体列表转换为前端期望的EnemyCard格式
     */
    private List<Map<String, Object>> convertToEnemyCardDTOList(List<Card> cards) {
        return cards.stream()
                .map(this::convertToEnemyCardDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将 enemy_pool JSON 解析为带权重的敌人列表
     * 兼容以下格式：
     * 1. [1, 2, 3]
     * 2. [{"id": 1, "weight": 3}, {"enemyId": 2}]
     * 3. {"enemies": [...]}
     */
    private List<WeightedEnemy> parseEnemyPool(String enemyPoolJson) {
        List<WeightedEnemy> result = new ArrayList<>();
        if (!StringUtils.hasText(enemyPoolJson)) {
            return result;
        }

        try {
            Object parsed = JSON.parse(enemyPoolJson.trim());
            if (parsed instanceof JSONArray) {
                appendEnemiesFromArray((JSONArray) parsed, result);
            } else if (parsed instanceof JSONObject) {
                JSONObject obj = (JSONObject) parsed;
                if (obj.containsKey("enemies")) {
                    Object enemiesNode = obj.get("enemies");
                    if (enemiesNode instanceof JSONArray) {
                        appendEnemiesFromArray((JSONArray) enemiesNode, result);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略解析异常，回退到难度匹配
        }

        return result.stream()
                .filter(item -> item.getEnemyId() != null)
                .collect(Collectors.toList());
    }

    /**
     * 将 JSONArray 中的敌人配置追加到列表
     */
    private void appendEnemiesFromArray(JSONArray array, List<WeightedEnemy> collector) {
        if (array == null) {
            return;
        }
        for (Object obj : array) {
            if (obj instanceof Number) {
                collector.add(new WeightedEnemy(((Number) obj).longValue(), 1));
            } else if (obj instanceof String) {
                try {
                    collector.add(new WeightedEnemy(Long.parseLong(((String) obj).trim()), 1));
                } catch (NumberFormatException ignored) {
                }
            } else if (obj instanceof JSONObject) {
                JSONObject json = (JSONObject) obj;
                Long enemyId = null;
                if (json.containsKey("enemyId")) {
                    enemyId = json.getLong("enemyId");
                } else if (json.containsKey("id")) {
                    enemyId = json.getLong("id");
                } else if (json.containsKey("enemy_id")) {
                    enemyId = json.getLong("enemy_id");
                }
                Integer weight = json.getInteger("weight");
                collector.add(new WeightedEnemy(enemyId, weight != null && weight > 0 ? weight : 1));
            }
        }
    }

    /**
     * 根据权重随机挑选敌人
     */
    private WeightedEnemy pickEnemyByWeight(List<WeightedEnemy> weightedEnemies) {
        if (CollectionUtils.isEmpty(weightedEnemies)) {
            return null;
        }
        int totalWeight = weightedEnemies.stream()
                .mapToInt(WeightedEnemy::getWeight)
                .sum();
        if (totalWeight <= 0) {
            return weightedEnemies.get(RANDOM.nextInt(weightedEnemies.size()));
        }

        int randomValue = RANDOM.nextInt(totalWeight);
        int current = 0;
        for (WeightedEnemy enemy : weightedEnemies) {
            current += enemy.getWeight();
            if (randomValue < current) {
                return enemy;
            }
        }

        return weightedEnemies.get(weightedEnemies.size() - 1);
    }

    /**
     * 敌人权重封装
     */
    private static class WeightedEnemy {
        private final Long enemyId;
        private final int weight;

        WeightedEnemy(Long enemyId, int weight) {
            this.enemyId = enemyId;
            this.weight = weight;
        }

        public Long getEnemyId() {
            return enemyId;
        }

        public int getWeight() {
            return weight;
        }
    }

    /**
     * 将Card实体转换为前端期望的EnemyCard格式
     * 前端期望格式：
     * {
     *   name: string,
     *   type: string (spell/equipment),
     *   attack?: number,
     *   health?: number,
     *   effect?: string,
     *   unique_play?: boolean
     * }
     */
    private Map<String, Object> convertToEnemyCardDTO(Card card) {
        Map<String, Object> dto = new HashMap<>();
        
        // 基础字段
        dto.put("name", card.getName() != null ? card.getName() : "");
        dto.put("type", card.getCardType() != null ? card.getCardType() : "spell");
        
        // 从 statModifiers JSON 中提取 attack 和 health
        if (StringUtils.hasText(card.getStatModifiers())) {
            try {
                JSONObject statModifiers = JSON.parseObject(card.getStatModifiers());
                if (statModifiers.containsKey("attack")) {
                    dto.put("attack", statModifiers.getInteger("attack"));
                }
                if (statModifiers.containsKey("health")) {
                    dto.put("health", statModifiers.getInteger("health"));
                }
            } catch (Exception e) {
                // 解析失败，忽略
            }
        }
        
        // 从 effectPayload JSON 中提取 effect
        if (StringUtils.hasText(card.getEffectPayload())) {
            try {
                JSONObject effectPayload = JSON.parseObject(card.getEffectPayload());
                // 将整个 effectPayload 转为字符串作为 effect
                dto.put("effect", card.getEffectPayload());
                
                // 检查是否有 unique_play 标记
                if (effectPayload.containsKey("unique_play")) {
                    dto.put("unique_play", effectPayload.getBoolean("unique_play"));
                }
            } catch (Exception e) {
                // 如果解析失败，直接使用字符串
                dto.put("effect", card.getEffectPayload());
            }
        }
        
        // 如果没有 effect，使用 description
        if (!dto.containsKey("effect") && StringUtils.hasText(card.getDescription())) {
            dto.put("effect", card.getDescription());
        }
        
        return dto;
    }

    /**
     * 根据敌人ID获取敌人信息
     */
    public Enemy getEnemyById(Long enemyId) {
        if (enemyId == null) {
            return null;
        }
        return enemyMapper.selectById(enemyId);
    }

    /**
     * 根据难度获取敌人列表
     */
    public List<Enemy> getEnemiesByDifficulty(String difficulty) {
        String normalizedDifficulty = normalizeDifficulty(difficulty);
        if (normalizedDifficulty == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Enemy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enemy::getDifficulty, normalizedDifficulty);
        return enemyMapper.selectList(wrapper);
    }
}

