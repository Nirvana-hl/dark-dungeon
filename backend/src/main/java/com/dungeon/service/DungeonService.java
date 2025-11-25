package com.dungeon.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.*;
import com.dungeon.dto.UserPlayerCharacterDTO;
import com.dungeon.entity.Dungeon;
import com.dungeon.entity.Enemy;
import com.dungeon.entity.Event;
import com.dungeon.entity.Run;
import com.dungeon.entity.Stage;
import com.dungeon.entity.UserStageProgress;
import com.dungeon.mapper.DungeonMapper;
import com.dungeon.mapper.EnemyMapper;
import com.dungeon.mapper.RunMapper;
import com.dungeon.mapper.UserStageProgressMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 地牢/探索/战斗业务
 */
@Service
public class DungeonService {

    @Autowired
    private DungeonMapper dungeonMapper;

    @Autowired
    private RunMapper runMapper;

    @Autowired
    private StageService stageService;

    @Autowired
    private EnemyMapper enemyMapper;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserStageProgressService userStageProgressService;

    @Autowired
    private UserStageProgressMapper userStageProgressMapper;

    @Autowired
    private UserPlayerCharacterService userPlayerCharacterService;

    private static final Random RANDOM = new Random();

    public List<DungeonDTO> getAllDungeons() {
        return dungeonMapper.selectList(null).stream()
                .map(this::toDungeonDTO)
                .collect(Collectors.toList());
    }

    public RunDTO getCurrentRun(Long userId) {
        Run run = runMapper.selectCurrentRun(userId);
        if (run == null) {
            return null;
        }
        return toRunDTO(run);
    }

    public RunDTO getRunDetail(Long userId, Long runId) {
        Run run = runMapper.selectById(runId);
        if (run == null || !Objects.equals(run.getUserId(), userId)) {
            throw new RuntimeException("探索记录不存在");
        }
        return toRunDTO(run);
    }

    @Transactional
    public RunDTO startRun(Long userId, StartRunRequest request) {
        if (request == null || request.getStageNumber() == null || request.getUserPlayerCharacterId() == null) {
            throw new RuntimeException("关卡编号和角色不能为空");
        }

        Run existing = runMapper.selectCurrentRun(userId);
        if (existing != null) {
            throw new RuntimeException("当前已有进行中的探索，请先结束");
        }

        Stage stage = stageService.getStageByNumber(request.getStageNumber());
        if (stage == null) {
            throw new RuntimeException("关卡不存在");
        }

        // 校验关卡是否解锁
        UserStageProgress progress = userStageProgressMapper.selectByUserIdAndStageNumber(userId, request.getStageNumber());
        if (progress == null || !Boolean.TRUE.equals(progress.getIsUnlocked())) {
            throw new RuntimeException("关卡尚未解锁");
        }

        // 校验角色归属
        if (userPlayerCharacterService.getUserPlayerCharacterById(request.getUserPlayerCharacterId(), userId) == null) {
            throw new RuntimeException("角色不存在或不属于当前用户");
        }

        RunProgressState state = RunProgressState.initial(stage);

        Run run = new Run();
        run.setUserId(userId);
        run.setUserPlayerCharacterId(request.getUserPlayerCharacterId());
        run.setDungeonId(stage.getDungeonId());
        run.setStageNumber(stage.getStageNumber());
        run.setChapterNumber(stage.getChapterNumber());
        run.setDifficulty(stage.getDifficulty());
        run.setPreparationSnapshot(JSON.toJSONString(buildPreparationSnapshot(request)));
        run.setCurrentStageProgress(JSON.toJSONString(state));
        run.setStartedAt(LocalDateTime.now());
        runMapper.insert(run);
        return toRunDTO(run);
    }

    @Transactional
    public RunActionResponse explore(Long userId, Long runId, ExploreRequest request) {
        Run run = getActiveRunOrThrow(userId, runId);
        Stage stage = stageService.getStageByNumber(run.getStageNumber());
        RunProgressState state = parseProgress(run);

        if ("completed".equalsIgnoreCase(state.getStatus())) {
            throw new RuntimeException("探索已完成，请先结束");
        }
        if (state.isAwaitingBattle()) {
            throw new RuntimeException("仍有未处理的战斗，请先解决战斗");
        }

        String action = request != null && StringUtils.hasText(request.getAction())
                ? request.getAction() : "explore";

        String message;
        String eventSummary = null;
        Integer targetRoomId = request != null ? request.getTargetRoomId() : null;

        // 解析地图配置
        Map<String, Object> mapConfig = parseMapConfig(stage.getExplorationMap());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rooms = (List<Map<String, Object>>) mapConfig.get("rooms");
        @SuppressWarnings("unchecked")
        List<List<Integer>> paths = (List<List<Integer>>) mapConfig.get("paths");

        // 如果指定了目标房间ID，进行移动探索
        if (targetRoomId != null && "move".equalsIgnoreCase(action)) {
            // 验证路径是否可达
            Integer currentRoomId = state.getCurrentRoomId();
            if (currentRoomId == null) {
                // 如果没有当前房间，找到起始房间
                currentRoomId = findStartRoom(rooms);
                state.setCurrentRoomId(currentRoomId);
            }

            if (!isPathReachable(paths, currentRoomId, targetRoomId)) {
                throw new RuntimeException("无法到达目标房间，路径不存在");
            }

            // 移动到目标房间
            state.setCurrentRoomId(targetRoomId);
            state.setExploredRooms(state.getExploredRooms() + 1);
            
            // 添加到已访问列表
            if (!state.getVisitedRooms().contains(targetRoomId)) {
                state.getVisitedRooms().add(targetRoomId);
            }

            // 获取目标房间信息
            Map<String, Object> targetRoom = findRoomById(rooms, targetRoomId);
            if (targetRoom != null) {
                String roomType = (String) targetRoom.get("type");
                String roomName = (String) targetRoom.get("name");
                state.setCurrentRoom(roomName != null ? roomName : "Room-" + targetRoomId);

                // 根据房间类型决定触发内容
                if ("start".equalsIgnoreCase(roomType)) {
                    message = "到达入口：" + roomName;
                } else if ("end".equalsIgnoreCase(roomType)) {
                    // 到达出口，完成探索
                    state.setStatus("completed");
                    message = "到达出口：" + roomName + "，探索完成！";
                } else if ("boss".equalsIgnoreCase(roomType)) {
                    // Boss房间，必定触发Boss战斗
                    Enemy enemy = pickEnemyForStage(stage);
                    if (enemy != null) {
                        state.prepareBattle(enemy);
                        message = "遭遇Boss：" + enemy.getName();
                    } else {
                        message = "Boss房间，但未找到Boss敌人";
                    }
                } else if ("event".equalsIgnoreCase(roomType)) {
                    // 事件房间，必定触发事件
                    Event event = eventService.triggerRandomDungeonEvent(stage.getStageNumber(), stage.getChapterNumber());
                    if (event != null) {
                        eventSummary = buildEventSummary(event);
                        state.getEventLog().add(eventSummary);
                        message = "触发事件：" + event.getName();
                    } else {
                        message = "事件房间，但未触发事件";
                    }
                } else {
                    // normal房间，随机触发事件或敌人
                    if (RANDOM.nextBoolean()) {
                        Event event = eventService.triggerRandomDungeonEvent(stage.getStageNumber(), stage.getChapterNumber());
                        if (event != null) {
                            eventSummary = buildEventSummary(event);
                            state.getEventLog().add(eventSummary);
                            message = "触发事件：" + event.getName();
                        } else {
                            message = "空房间，未发现异常";
                        }
                    } else {
                        Enemy enemy = pickEnemyForStage(stage);
                        if (enemy != null) {
                            state.prepareBattle(enemy);
                            message = "遭遇敌人：" + enemy.getName();
                        } else {
                            message = "暂未发现敌人";
                        }
                    }
                }
            } else {
                message = "移动到房间 " + targetRoomId;
            }
        } else {
            // 兼容旧版线性探索逻辑
            state.setExploredRooms(state.getExploredRooms() + 1);
            state.setCurrentRoom("Room-" + state.getExploredRooms());

            if ("event".equalsIgnoreCase(action)) {
                Event event = eventService.triggerRandomDungeonEvent(stage.getStageNumber(), stage.getChapterNumber());
                if (event != null) {
                    eventSummary = buildEventSummary(event);
                    state.getEventLog().add(eventSummary);
                    message = "触发事件：" + event.getName();
                } else {
                    message = "未触发事件，继续探索";
                }
            } else {
                // 随机决定事件或战斗
                if (RANDOM.nextBoolean()) {
                    Event event = eventService.triggerRandomDungeonEvent(stage.getStageNumber(), stage.getChapterNumber());
                    if (event != null) {
                        eventSummary = buildEventSummary(event);
                        state.getEventLog().add(eventSummary);
                        message = "触发事件：" + event.getName();
                    } else {
                        message = "空房间，未发现异常";
                    }
                } else {
                    Enemy enemy = pickEnemyForStage(stage);
                    if (enemy != null) {
                        state.prepareBattle(enemy);
                        message = "遭遇敌人：" + enemy.getName();
                    } else {
                        message = "暂未发现敌人";
                    }
                }
            }
        }

        run.setCurrentStageProgress(JSON.toJSONString(state));
        runMapper.updateById(run);

        RunActionResponse response = new RunActionResponse();
        response.setRun(toRunDTO(run));
        response.setMessage(message);
        response.setEventSummary(eventSummary);
        response.setBattlePending(state.isAwaitingBattle());
        return response;
    }

    @Transactional
    public RunActionResponse resolveBattle(Long userId, Long runId, BattleRequest request) {
        Run run = getActiveRunOrThrow(userId, runId);
        RunProgressState state = parseProgress(run);
        if (!state.isAwaitingBattle() || state.getPendingEnemyId() == null) {
            throw new RuntimeException("当前没有待处理的战斗");
        }

        Enemy enemy = enemyMapper.selectById(state.getPendingEnemyId());
        if (enemy == null) {
            state.resetBattle();
            run.setCurrentStageProgress(JSON.toJSONString(state));
            runMapper.updateById(run);
            throw new RuntimeException("敌人数据缺失，已跳过战斗");
        }

        UserPlayerCharacterDTO hero = userPlayerCharacterService
                .getUserPlayerCharacterById(run.getUserPlayerCharacterId(), userId);
        if (hero == null) {
            throw new RuntimeException("角色不存在");
        }

        BattleResultDTO battleResult = simulateBattle(hero, enemy, request != null ? request.getStrategy() : null);

        // 更新角色血量/压力
        userPlayerCharacterService.updateUserPlayerCharacter(
                run.getUserPlayerCharacterId(),
                userId,
                Math.max(battleResult.getHeroRemainingHp(), 0),
                null,
                Math.min(hero.getCurrentStress() + 3, 100));

        state.getBattleLog().addAll(battleResult.getBattleLog());

        if ("victory".equalsIgnoreCase(battleResult.getOutcome())) {
            state.setDefeatedEnemies(state.getDefeatedEnemies() + 1);
            state.resetBattle();
            state.setStatus("exploring");
        } else {
            // 战败则结束探索
            state.setStatus("completed");
            run.setResult("defeat");
            run.setEndedAt(LocalDateTime.now());
            userStageProgressService.recordStageAttempt(run.getUserId(), run.getStageNumber(), run.getChapterNumber(), "defeat");
        }

        run.setCurrentStageProgress(JSON.toJSONString(state));
        runMapper.updateById(run);

        RunActionResponse response = new RunActionResponse();
        response.setRun(toRunDTO(run));
        response.setMessage("战斗已结算，结果：" + battleResult.getOutcome());
        response.setBattleResult(battleResult);
        response.setBattlePending(state.isAwaitingBattle());
        return response;
    }

    @Transactional
    public RunDTO finishRun(Long userId, Long runId, EndRunRequest request) {
        Run run = runMapper.selectById(runId);
        if (run == null || !Objects.equals(run.getUserId(), userId)) {
            throw new RuntimeException("探索记录不存在");
        }
        if (run.getResult() != null) {
            return toRunDTO(run);
        }

        RunProgressState state = parseProgress(run);
        String result = request != null && StringUtils.hasText(request.getResult())
                ? request.getResult() : "abandon";

        if ("victory".equalsIgnoreCase(result)) {
            state.setStatus("completed");
            Map<String, Object> rewards = request != null && request.getRewardChoice() != null
                    ? request.getRewardChoice()
                    : generateDefaultRewards(run, state);
            run.setRewardSnapshot(JSON.toJSONString(rewards));
            run.setResult("victory");
            userStageProgressService.passStage(run.getUserId(), run.getStageNumber(), run.getChapterNumber());
        } else if ("defeat".equalsIgnoreCase(result)) {
            state.setStatus("completed");
            run.setResult("defeat");
            userStageProgressService.recordStageAttempt(run.getUserId(), run.getStageNumber(), run.getChapterNumber(), "defeat");
        } else {
            state.setStatus("abandoned");
            run.setResult("abandon");
            userStageProgressService.recordStageAttempt(run.getUserId(), run.getStageNumber(), run.getChapterNumber(), "abandon");
        }

        run.setCurrentStageProgress(JSON.toJSONString(state));
        run.setEndedAt(LocalDateTime.now());
        runMapper.updateById(run);
        return toRunDTO(run);
    }

    private Run getActiveRunOrThrow(Long userId, Long runId) {
        Run run = runMapper.selectById(runId);
        if (run == null || !Objects.equals(run.getUserId(), userId)) {
            throw new RuntimeException("探索记录不存在");
        }
        if (run.getResult() != null) {
            throw new RuntimeException("探索已结束");
        }
        return run;
    }

    private Map<String, Object> buildPreparationSnapshot(StartRunRequest request) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("cardCharacterIds", request.getCardCharacterIds());
        snapshot.put("cardIds", request.getCardIds());
        snapshot.put("consumables", request.getConsumableItemIds());
        snapshot.put("notes", request.getNotes());
        return snapshot;
    }

    private RunProgressState parseProgress(Run run) {
        if (!StringUtils.hasText(run.getCurrentStageProgress())) {
            return RunProgressState.initial(null);
        }
        try {
            RunProgressState state = JSON.parseObject(run.getCurrentStageProgress(), RunProgressState.class);
            if (state.getEventLog() == null) {
                state.setEventLog(new ArrayList<>());
            }
            if (state.getBattleLog() == null) {
                state.setBattleLog(new ArrayList<>());
            }
            return state;
        } catch (Exception e) {
            return RunProgressState.initial(null);
        }
    }

    private Enemy pickEnemyForStage(Stage stage) {
        if (stage == null) {
            return null;
        }
        // 先尝试根据 enemy_pool 指定 ID
        if (StringUtils.hasText(stage.getEnemyPool())) {
            try {
                List<Long> enemyIds = new ArrayList<>();
                JSON.parseArray(stage.getEnemyPool()).forEach(obj -> {
                    if (obj instanceof Number) {
                        enemyIds.add(((Number) obj).longValue());
                    } else {
                        JSONObject json = (JSONObject) obj;
                        if (json.containsKey("enemyId")) {
                            enemyIds.add(json.getLong("enemyId"));
                        }
                    }
                });
                if (!enemyIds.isEmpty()) {
                    Long pickId = enemyIds.get(RANDOM.nextInt(enemyIds.size()));
                    Enemy enemy = enemyMapper.selectById(pickId);
                    if (enemy != null) {
                        return enemy;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        String enemyDifficulty = stage.getIsBossStage() != null && stage.getIsBossStage() ? "boss"
                : normalizeEnemyDifficulty(stage.getDifficulty());
        // 使用 MyBatis Plus 的 LambdaQueryWrapper 查询敌人
        LambdaQueryWrapper<Enemy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enemy::getDifficulty, enemyDifficulty);
        List<Enemy> candidates = enemyMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(candidates)) {
            candidates = enemyMapper.selectList(new QueryWrapper<>());
        }
        if (CollectionUtils.isEmpty(candidates)) {
            return null;
        }
        return candidates.get(RANDOM.nextInt(candidates.size()));
    }

    private String normalizeEnemyDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            return "normal";
        }
        switch (difficulty.toLowerCase()) {
            case "easy":
                return "easy";
            case "hard":
            case "expert":
                return "hard";
            default:
                return "normal";
        }
    }

    private String buildEventSummary(Event event) {
        StringBuilder builder = new StringBuilder();
        builder.append(event.getName()).append("：").append(event.getDescription());
        if (StringUtils.hasText(event.getEffectPayload())) {
            builder.append(" (效果: ").append(event.getEffectPayload()).append(")");
        }
        return builder.toString();
    }

    private BattleResultDTO simulateBattle(UserPlayerCharacterDTO hero, Enemy enemy, String strategy) {
        JSONObject enemyStats = StringUtils.hasText(enemy.getBaseStats())
                ? JSON.parseObject(enemy.getBaseStats())
                : new JSONObject();

        int heroHp = hero.getCurrentHp() != null ? hero.getCurrentHp() : hero.getMaxHp();
        int heroAttack = Math.max(8, hero.getMaxActionPoints() * 3);
        int enemyHp = enemyStats.getIntValue("hp", 80);
        int enemyAttack = enemyStats.getIntValue("attack", 10);

        if ("aggressive".equalsIgnoreCase(strategy)) {
            heroAttack += 5;
            heroHp -= 5;
        } else if ("defensive".equalsIgnoreCase(strategy)) {
            enemyAttack -= 2;
        }

        List<String> log = new ArrayList<>();
        int round = 1;
        while (heroHp > 0 && enemyHp > 0 && round <= 20) {
            int heroDamage = Math.max(5, heroAttack + RANDOM.nextInt(6) - 3);
            enemyHp -= heroDamage;
            log.add("第" + round + "回合：玩家造成 " + heroDamage + " 点伤害。敌人剩余 " + Math.max(enemyHp, 0) + " HP。");
            if (enemyHp <= 0) {
                break;
            }
            int enemyDamage = Math.max(4, enemyAttack + RANDOM.nextInt(6) - 2);
            heroHp -= enemyDamage;
            log.add("第" + round + "回合：敌人造成 " + enemyDamage + " 点伤害。玩家剩余 " + Math.max(heroHp, 0) + " HP。");
            round++;
        }

        BattleResultDTO result = new BattleResultDTO();
        result.setEnemyName(enemy.getName());
        result.setHeroRemainingHp(Math.max(heroHp, 0));
        result.setEnemyRemainingHp(Math.max(enemyHp, 0));
        result.setBattleLog(log);
        result.setOutcome(enemyHp <= 0 ? "victory" : "defeat");
        return result;
    }

    private Map<String, Object> generateDefaultRewards(Run run, RunProgressState state) {
        Map<String, Object> rewards = new HashMap<>();
        int baseGold = 100 + run.getStageNumber() * 20 + state.getDefeatedEnemies() * 15;
        rewards.put("gold", baseGold);
        rewards.put("experience", 50 + run.getStageNumber() * 10);
        rewards.put("notes", "系统默认奖励，可按 rewardChoice 覆盖");
        return rewards;
    }

    private RunDTO toRunDTO(Run run) {
        RunDTO dto = new RunDTO();
        BeanUtils.copyProperties(run, dto);
        if (run.getDungeonId() != null) {
            Dungeon dungeon = dungeonMapper.selectById(run.getDungeonId());
            if (dungeon != null) {
                dto.setDungeonName(dungeon.getName());
            }
        }
        Stage stage = stageService.getStageByNumber(run.getStageNumber());
        if (stage != null) {
            dto.setStageName(stage.getStageName());
        }
        dto.setProgress(toProgressDTO(parseProgress(run)));
        return dto;
    }

    private RunProgressDTO toProgressDTO(RunProgressState state) {
        RunProgressDTO dto = new RunProgressDTO();
        dto.setStatus(state.getStatus());
        dto.setCurrentRoom(state.getCurrentRoom());
        dto.setCurrentRoomId(state.getCurrentRoomId());
        dto.setVisitedRooms(state.getVisitedRooms() != null ? new ArrayList<>(state.getVisitedRooms()) : new ArrayList<>());
        dto.setExploredRooms(state.getExploredRooms());
        dto.setDefeatedEnemies(state.getDefeatedEnemies());
        dto.setEventLog(new ArrayList<>(state.getEventLog()));
        dto.setBattleLog(new ArrayList<>(state.getBattleLog()));
        dto.setPendingEnemyName(state.getPendingEnemyName());
        dto.setPendingEnemyDifficulty(state.getPendingEnemyDifficulty());
        return dto;
    }

    private DungeonDTO toDungeonDTO(Dungeon dungeon) {
        DungeonDTO dto = new DungeonDTO();
        BeanUtils.copyProperties(dungeon, dto);
        return dto;
    }

    /**
     * 内部探索进度结构
     */
    @SuppressWarnings("unused")
    private static class RunProgressState {
        private String status;
        private String currentRoom;
        private Integer currentRoomId;        // 当前房间ID（基于地图）
        private List<Integer> visitedRooms;   // 已访问的房间ID列表
        private int exploredRooms;
        private int defeatedEnemies;
        private List<String> eventLog;
        private List<String> battleLog;
        private boolean awaitingBattle;
        private Long pendingEnemyId;
        private String pendingEnemyName;
        private String pendingEnemyDifficulty;

        static RunProgressState initial(Stage stage) {
            RunProgressState state = new RunProgressState();
            state.status = "exploring";
            state.currentRoom = "Entrance";
            state.exploredRooms = 0;
            state.defeatedEnemies = 0;
            state.eventLog = new ArrayList<>();
            state.battleLog = new ArrayList<>();
            state.awaitingBattle = false;
            state.visitedRooms = new ArrayList<>();
            
            // 如果有地图配置，找到起始房间
            if (stage != null && StringUtils.hasText(stage.getExplorationMap())) {
                try {
                    JSONObject mapJson = JSON.parseObject(stage.getExplorationMap());
                    if (mapJson.containsKey("rooms")) {
                        JSONArray roomsArray = mapJson.getJSONArray("rooms");
                        for (int i = 0; i < roomsArray.size(); i++) {
                            JSONObject roomJson = roomsArray.getJSONObject(i);
                            if ("start".equalsIgnoreCase(roomJson.getString("type"))) {
                                state.currentRoomId = roomJson.getInteger("id");
                                String roomName = roomJson.getString("name");
                                if (StringUtils.hasText(roomName)) {
                                    state.currentRoom = roomName;
                                }
                                state.visitedRooms.add(state.currentRoomId);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // 解析失败，使用默认值
                }
            }
            
            return state;
        }

        void prepareBattle(Enemy enemy) {
            this.awaitingBattle = true;
            this.pendingEnemyId = enemy.getId();
            this.pendingEnemyName = enemy.getName();
            this.pendingEnemyDifficulty = enemy.getDifficulty();
            this.status = "awaiting_battle";
        }

        void resetBattle() {
            this.awaitingBattle = false;
            this.pendingEnemyId = null;
            this.pendingEnemyName = null;
            this.pendingEnemyDifficulty = null;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCurrentRoom() {
            return currentRoom;
        }

        public void setCurrentRoom(String currentRoom) {
            this.currentRoom = currentRoom;
        }

        public int getExploredRooms() {
            return exploredRooms;
        }

        public void setExploredRooms(int exploredRooms) {
            this.exploredRooms = exploredRooms;
        }

        public int getDefeatedEnemies() {
            return defeatedEnemies;
        }

        public void setDefeatedEnemies(int defeatedEnemies) {
            this.defeatedEnemies = defeatedEnemies;
        }

        public List<String> getEventLog() {
            return eventLog;
        }

        public void setEventLog(List<String> eventLog) {
            this.eventLog = eventLog;
        }

        public List<String> getBattleLog() {
            return battleLog;
        }

        public void setBattleLog(List<String> battleLog) {
            this.battleLog = battleLog;
        }

        public boolean isAwaitingBattle() {
            return awaitingBattle;
        }

        public void setAwaitingBattle(boolean awaitingBattle) {
            this.awaitingBattle = awaitingBattle;
        }

        public Long getPendingEnemyId() {
            return pendingEnemyId;
        }

        public void setPendingEnemyId(Long pendingEnemyId) {
            this.pendingEnemyId = pendingEnemyId;
        }

        public String getPendingEnemyName() {
            return pendingEnemyName;
        }

        public void setPendingEnemyName(String pendingEnemyName) {
            this.pendingEnemyName = pendingEnemyName;
        }

        public String getPendingEnemyDifficulty() {
            return pendingEnemyDifficulty;
        }

        public void setPendingEnemyDifficulty(String pendingEnemyDifficulty) {
            this.pendingEnemyDifficulty = pendingEnemyDifficulty;
        }

        public Integer getCurrentRoomId() {
            return currentRoomId;
        }

        public void setCurrentRoomId(Integer currentRoomId) {
            this.currentRoomId = currentRoomId;
        }

        public List<Integer> getVisitedRooms() {
            if (visitedRooms == null) {
                visitedRooms = new ArrayList<>();
            }
            return visitedRooms;
        }

        public void setVisitedRooms(List<Integer> visitedRooms) {
            this.visitedRooms = visitedRooms;
        }
    }
    
    /**
     * 解析地图配置JSON
     */
    private Map<String, Object> parseMapConfig(String explorationMapJson) {
        Map<String, Object> result = new HashMap<>();
        result.put("rooms", new ArrayList<>());
        result.put("paths", new ArrayList<>());
        
        if (!StringUtils.hasText(explorationMapJson)) {
            return result;
        }
        
        try {
            JSONObject mapJson = JSON.parseObject(explorationMapJson);
            
            // 解析房间列表
            List<Map<String, Object>> rooms = new ArrayList<>();
            if (mapJson.containsKey("rooms")) {
                JSONArray roomsArray = mapJson.getJSONArray("rooms");
                for (int i = 0; i < roomsArray.size(); i++) {
                    JSONObject roomJson = roomsArray.getJSONObject(i);
                    Map<String, Object> room = new HashMap<>();
                    room.put("id", roomJson.getInteger("id"));
                    room.put("type", roomJson.getString("type"));
                    room.put("name", roomJson.getString("name"));
                    room.put("x", roomJson.getInteger("x"));
                    room.put("y", roomJson.getInteger("y"));
                    room.put("description", roomJson.getString("description"));
                    rooms.add(room);
                }
            }
            result.put("rooms", rooms);
            
            // 解析路径列表
            List<List<Integer>> paths = new ArrayList<>();
            if (mapJson.containsKey("paths")) {
                Object pathsObj = mapJson.get("paths");
                if (pathsObj instanceof JSONArray) {
                    JSONArray pathsArray = (JSONArray) pathsObj;
                    for (int i = 0; i < pathsArray.size(); i++) {
                        Object pathObj = pathsArray.get(i);
                        List<Integer> path = new ArrayList<>();
                        
                        if (pathObj instanceof JSONArray) {
                            // 格式：[from, to]
                            JSONArray pathArray = (JSONArray) pathObj;
                            if (pathArray.size() >= 2) {
                                path.add(pathArray.getInteger(0));
                                path.add(pathArray.getInteger(1));
                            }
                        } else if (pathObj instanceof JSONObject) {
                            // 格式：{"from": 1, "to": 2}
                            JSONObject pathJson = (JSONObject) pathObj;
                            path.add(pathJson.getInteger("from"));
                            path.add(pathJson.getInteger("to"));
                        }
                        
                        if (path.size() == 2) {
                            paths.add(path);
                        }
                    }
                }
            }
            result.put("paths", paths);
        } catch (Exception e) {
            // 解析失败，返回空配置
        }
        
        return result;
    }
    
    /**
     * 从房间列表中找到起始房间ID
     */
    private Integer findStartRoom(List<Map<String, Object>> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return null;
        }
        for (Map<String, Object> room : rooms) {
            if ("start".equalsIgnoreCase((String) room.get("type"))) {
                return (Integer) room.get("id");
            }
        }
        // 如果没有找到start类型，返回第一个房间
        return (Integer) rooms.get(0).get("id");
    }
    
    /**
     * 检查从fromRoomId到toRoomId是否有路径可达
     * 使用简单的BFS算法
     */
    private boolean isPathReachable(List<List<Integer>> paths, Integer fromRoomId, Integer toRoomId) {
        if (fromRoomId == null || toRoomId == null) {
            return false;
        }
        if (fromRoomId.equals(toRoomId)) {
            return true;
        }
        
        // 构建邻接表
        Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
        for (List<Integer> path : paths) {
            if (path.size() >= 2) {
                Integer from = path.get(0);
                Integer to = path.get(1);
                adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
                // 路径是双向的
                adjacencyList.computeIfAbsent(to, k -> new ArrayList<>()).add(from);
            }
        }
        
        // BFS搜索
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        queue.offer(fromRoomId);
        visited.add(fromRoomId);
        
        while (!queue.isEmpty()) {
            Integer current = queue.poll();
            List<Integer> neighbors = adjacencyList.get(current);
            if (neighbors != null) {
                for (Integer neighbor : neighbors) {
                    if (neighbor.equals(toRoomId)) {
                        return true;
                    }
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 根据房间ID查找房间信息
     */
    private Map<String, Object> findRoomById(List<Map<String, Object>> rooms, Integer roomId) {
        if (rooms == null || roomId == null) {
            return null;
        }
        for (Map<String, Object> room : rooms) {
            if (roomId.equals(room.get("id"))) {
                return room;
            }
        }
        return null;
    }
}

