package com.dungeon.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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

        state.setExploredRooms(state.getExploredRooms() + 1);
        state.setCurrentRoom("Room-" + state.getExploredRooms());

        String action = request != null && StringUtils.hasText(request.getAction())
                ? request.getAction() : "explore";

        String message;
        String eventSummary = null;

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
        List<Enemy> candidates = enemyMapper.selectByDifficulty(enemyDifficulty);
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
    }
}

