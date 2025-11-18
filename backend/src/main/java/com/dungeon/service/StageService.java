package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.entity.Stage;
import com.dungeon.mapper.StageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}

