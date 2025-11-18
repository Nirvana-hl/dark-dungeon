package com.dungeon.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.dungeon.entity.Event;
import com.dungeon.mapper.EventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 事件服务
 */
@Service
public class EventService {
    
    @Autowired
    private EventMapper eventMapper;
    
    /**
     * 根据事件类型获取事件列表
     */
    public List<Event> getEventsByLocationType(String locationType) {
        return eventMapper.selectByLocationType(locationType);
    }
    
    /**
     * 获取地牢内可触发的事件
     * 根据触发条件和触发概率筛选
     */
    public List<Event> getAvailableDungeonEvents(Integer stageNumber, Integer chapterNumber) {
        // 查询所有地牢事件
        List<Event> allDungeonEvents = eventMapper.selectByLocationType("dungeon");
        List<Event> availableEvents = new ArrayList<>();
        
        Random random = new Random();
        
        for (Event event : allDungeonEvents) {
            // 检查触发条件
            if (checkTriggerCondition(event, stageNumber, chapterNumber)) {
                // 检查触发概率
                if (event.getTriggerChance() != null) {
                    double chance = event.getTriggerChance().doubleValue();
                    if (random.nextDouble() < chance) {
                        availableEvents.add(event);
                    }
                } else {
                    // 如果没有设置触发概率，默认触发
                    availableEvents.add(event);
                }
            }
        }
        
        return availableEvents;
    }
    
    /**
     * 检查事件触发条件
     */
    private boolean checkTriggerCondition(Event event, Integer stageNumber, Integer chapterNumber) {
        if (event.getTriggerCondition() == null || event.getTriggerCondition().isEmpty()) {
            return true; // 没有触发条件，默认可以触发
        }
        
        try {
            JSONObject condition = JSON.parseObject(event.getTriggerCondition());
            
            // 检查关卡编号范围
            if (condition.containsKey("stage_range")) {
                List<Integer> range = condition.getList("stage_range", Integer.class);
                if (range != null && range.size() == 2) {
                    int min = range.get(0);
                    int max = range.get(1);
                    if (stageNumber < min || stageNumber > max) {
                        return false;
                    }
                }
            }
            
            // 检查章节
            if (condition.containsKey("chapter")) {
                Integer requiredChapter = condition.getInteger("chapter");
                if (requiredChapter != null && !requiredChapter.equals(chapterNumber)) {
                    return false;
                }
            }
            
            // 检查章节范围
            if (condition.containsKey("chapter_range")) {
                List<Integer> range = condition.getList("chapter_range", Integer.class);
                if (range != null && range.size() == 2) {
                    int min = range.get(0);
                    int max = range.get(1);
                    if (chapterNumber < min || chapterNumber > max) {
                        return false;
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            // 解析失败，默认可以触发
            return true;
        }
    }
    
    /**
     * 随机触发一个地牢事件
     */
    public Event triggerRandomDungeonEvent(Integer stageNumber, Integer chapterNumber) {
        List<Event> availableEvents = getAvailableDungeonEvents(stageNumber, chapterNumber);
        if (availableEvents.isEmpty()) {
            return null;
        }
        
        Random random = new Random();
        int index = random.nextInt(availableEvents.size());
        return availableEvents.get(index);
    }
}

