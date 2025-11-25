package com.dungeon.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.dto.*;
import com.dungeon.entity.Stage;
import com.dungeon.mapper.StageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 关卡服务
 */
@Service
public class StageService {
    
    @Autowired
    private StageMapper stageMapper;
    
    /**
     * 获取所有关卡列表
     */
    public List<Stage> getAllStages() {
        QueryWrapper<Stage> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("stage_number");
        return stageMapper.selectList(wrapper);
    }
    
    /**
     * 根据关卡编号获取关卡
     */
    public Stage getStageByNumber(Integer stageNumber) {
        return stageMapper.selectByStageNumber(stageNumber);
    }
    
    /**
     * 根据章节编号获取关卡列表
     */
    public List<Stage> getStagesByChapter(Integer chapterNumber) {
        return stageMapper.selectByChapterNumber(chapterNumber);
    }
    
    /**
     * 获取所有Boss关卡
     */
    public List<Stage> getBossStages() {
        return stageMapper.selectBossStages();
    }
    
    /**
     * 根据难度获取关卡列表
     */
    public List<Stage> getStagesByDifficulty(String difficulty) {
        return stageMapper.selectByDifficulty(difficulty);
    }
    
    /**
     * 获取关卡地图配置
     * @param stageNumber 关卡编号
     * @return 地图DTO，如果关卡不存在或没有地图配置则返回null
     */
    public StageMapDTO getStageMap(Integer stageNumber) {
        Stage stage = getStageByNumber(stageNumber);
        if (stage == null) {
            return null;
        }
        
        StageMapDTO result = new StageMapDTO();
        result.setStageNumber(stage.getStageNumber());
        result.setStageName(stage.getStageName());
        
        // 解析 exploration_map JSON
        MapDTO map = parseExplorationMap(stage.getExplorationMap());
        result.setMap(map);
        
        return result;
    }
    
    /**
     * 解析 exploration_map JSON 字符串为 MapDTO
     */
    private MapDTO parseExplorationMap(String explorationMapJson) {
        if (!StringUtils.hasText(explorationMapJson)) {
            // 如果没有地图配置，返回空地图
            MapDTO map = new MapDTO();
            map.setRooms(new ArrayList<>());
            map.setPaths(new ArrayList<>());
            return map;
        }
        
        try {
            JSONObject mapJson = JSON.parseObject(explorationMapJson);
            MapDTO map = new MapDTO();
            
            // 解析房间列表
            List<RoomDTO> rooms = new ArrayList<>();
            if (mapJson.containsKey("rooms")) {
                JSONArray roomsArray = mapJson.getJSONArray("rooms");
                for (int i = 0; i < roomsArray.size(); i++) {
                    JSONObject roomJson = roomsArray.getJSONObject(i);
                    RoomDTO room = new RoomDTO();
                    room.setId(roomJson.getInteger("id"));
                    room.setType(roomJson.getString("type"));
                    room.setName(roomJson.getString("name"));
                    room.setX(roomJson.getInteger("x"));
                    room.setY(roomJson.getInteger("y"));
                    room.setDescription(roomJson.getString("description"));
                    rooms.add(room);
                }
            }
            map.setRooms(rooms);
            
            // 解析路径列表
            List<PathDTO> paths = new ArrayList<>();
            if (mapJson.containsKey("paths")) {
                Object pathsObj = mapJson.get("paths");
                if (pathsObj instanceof JSONArray) {
                    JSONArray pathsArray = (JSONArray) pathsObj;
                    for (int i = 0; i < pathsArray.size(); i++) {
                        Object pathObj = pathsArray.get(i);
                        PathDTO path = new PathDTO();
                        
                        if (pathObj instanceof JSONArray) {
                            // 格式：[from, to]
                            JSONArray pathArray = (JSONArray) pathObj;
                            if (pathArray.size() >= 2) {
                                path.setFrom(pathArray.getInteger(0));
                                path.setTo(pathArray.getInteger(1));
                            }
                        } else if (pathObj instanceof JSONObject) {
                            // 格式：{"from": 1, "to": 2}
                            JSONObject pathJson = (JSONObject) pathObj;
                            path.setFrom(pathJson.getInteger("from"));
                            path.setTo(pathJson.getInteger("to"));
                        }
                        
                        if (path.getFrom() != null && path.getTo() != null) {
                            paths.add(path);
                        }
                    }
                }
            }
            map.setPaths(paths);
            
            // 解析布局类型
            if (mapJson.containsKey("layout")) {
                map.setLayout(mapJson.getString("layout"));
            }
            
            return map;
        } catch (Exception e) {
            // 解析失败，返回空地图
            MapDTO map = new MapDTO();
            map.setRooms(new ArrayList<>());
            map.setPaths(new ArrayList<>());
            return map;
        }
    }
}

