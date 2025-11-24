package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.entity.Achievement;
import com.dungeon.mapper.AchievementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 成就服务
 * 提供成就查询和管理功能
 */
@Service
public class AchievementService {
    
    @Autowired
    private AchievementMapper achievementMapper;
    
    /**
     * 获取所有成就列表
     * @return 成就列表
     */
    public List<Achievement> getAllAchievements() {
        QueryWrapper<Achievement> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("category", "id");
        return achievementMapper.selectList(wrapper);
    }
    
    /**
     * 根据分类查询成就
     * @param category 分类：progression-进度, mastery-精通, collection-收集, social-社交
     * @return 成就列表
     */
    public List<Achievement> getAchievementsByCategory(String category) {
        return achievementMapper.selectByCategory(category);
    }
    
    /**
     * 根据ID获取成就详情
     * @param id 成就ID
     * @return 成就信息
     */
    public Achievement getAchievementById(Long id) {
        return achievementMapper.selectById(id);
    }
    
    /**
     * 根据名称模糊查询成就
     * @param name 成就名称（支持模糊查询）
     * @return 成就列表
     */
    public List<Achievement> searchAchievementsByName(String name) {
        QueryWrapper<Achievement> wrapper = new QueryWrapper<>();
        wrapper.like("name", name);
        wrapper.orderByAsc("category", "id");
        return achievementMapper.selectList(wrapper);
    }
}

