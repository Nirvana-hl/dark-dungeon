package com.dungeon.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.EventDTO;
import com.dungeon.entity.Event;
import com.dungeon.mapper.EventMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 事件服务
 */
@Service
public class EventService {

    @Autowired
    private EventMapper eventMapper;

    private static final double DEFAULT_CHANCE = 1.0d;

    /**
     * 获取营地事件列表
     * @return 事件列表
     */
    public List<EventDTO> getCampEvents() {
        QueryWrapper<Event> wrapper = new QueryWrapper<>();
        wrapper.eq("location_type", "camp");
        List<Event> events = eventMapper.selectList(wrapper);
        
        return events.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据关卡信息随机触发一个地牢事件
     *
     * @param stageNumber   当前关卡编号
     * @param chapterNumber 当前章节
     * @return 匹配的事件，若无匹配则返回 null
     */
    public Event triggerRandomDungeonEvent(Integer stageNumber, Integer chapterNumber) {
        LambdaQueryWrapper<Event> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Event::getLocationType, "dungeon");
        List<Event> dungeonEvents = eventMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(dungeonEvents)) {
            return null;
        }

        List<Event> candidates = dungeonEvents.stream()
                .filter(event -> matchesCondition(event, stageNumber, chapterNumber))
                .collect(Collectors.toList());

        List<Event> pool = CollectionUtils.isEmpty(candidates) ? dungeonEvents : candidates;
        double totalWeight = pool.stream()
                .mapToDouble(event -> event.getTriggerChance() != null
                        ? event.getTriggerChance().doubleValue()
                        : DEFAULT_CHANCE)
                .filter(value -> value > 0)
                .sum();

        if (totalWeight <= 0) {
            return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        }

        double randomPick = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cumulative = 0;
        for (Event event : pool) {
            double weight = event.getTriggerChance() != null
                    ? Math.max(event.getTriggerChance().doubleValue(), 0)
                    : DEFAULT_CHANCE;
            cumulative += weight;
            if (randomPick <= cumulative) {
                return event;
            }
        }
        return pool.get(pool.size() - 1);
    }


    /**
     * 完成任务事件
     * @param userId 用户ID
     * @param eventId 事件ID
     * @return 事件效果信息
     */
    @Transactional
    public String completeEvent(Long userId, Long eventId) {
        // 查询事件
        Event event = eventMapper.selectById(eventId);
        if (event == null) {
            throw new RuntimeException("事件不存在");
        }

        // 验证是否为营地事件
        if (!"camp".equals(event.getLocationType())) {
            throw new RuntimeException("该事件不是营地事件");
        }

        // 这里可以应用事件效果（如增加金币、减少压力等）
        // 简化实现：返回成功消息
        return String.format("成功完成事件：%s", event.getName());
    }

    /**
     * 实体转DTO
     */
    private EventDTO toDTO(Event event) {
        EventDTO dto = new EventDTO();
        BeanUtils.copyProperties(event, dto);
        return dto;
    }

    private boolean matchesCondition(Event event, Integer stageNumber, Integer chapterNumber) {
        if (!StringUtils.hasText(event.getTriggerCondition())) {
            return true;
        }
        try {
            JSONObject condition = JSON.parseObject(event.getTriggerCondition());
            if (condition == null) {
                return true;
            }
            if (stageNumber != null) {
                if (condition.containsKey("minStageNumber")
                        && stageNumber < condition.getIntValue("minStageNumber")) {
                    return false;
                }
                if (condition.containsKey("maxStageNumber")
                        && stageNumber > condition.getIntValue("maxStageNumber")) {
                    return false;
                }
                if (condition.containsKey("stageNumbers")) {
                    JSONArray stages = condition.getJSONArray("stageNumbers");
                    if (stages != null && !stages.isEmpty()) {
                        boolean match = stages.stream()
                                .filter(Number.class::isInstance)
                                .map(Number.class::cast)
                                .anyMatch(num -> num.intValue() == stageNumber);
                        if (!match) {
                            return false;
                        }
                    }
                }
            }

            if (chapterNumber != null) {
                if (condition.containsKey("chapters")) {
                    JSONArray chapters = condition.getJSONArray("chapters");
                    if (chapters != null && !chapters.isEmpty()) {
                        boolean match = chapters.stream()
                                .filter(Number.class::isInstance)
                                .map(Number.class::cast)
                                .anyMatch(num -> num.intValue() == chapterNumber);
                        if (!match) {
                            return false;
                        }
                    }
                }
                if (condition.containsKey("chapter")
                        && chapterNumber.intValue() != condition.getIntValue("chapter")) {
                    return false;
                }
            }
            return true;
        } catch (Exception ignored) {
            // 解析失败时，默认允许触发，避免因为配置错误导致功能失效
            return true;
        }
    }
}
