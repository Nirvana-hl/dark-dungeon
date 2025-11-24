package com.dungeon.service;

import com.dungeon.entity.UserAchievement;
import com.dungeon.mapper.UserAchievementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户成就服务
 * 提供用户成就查询、更新、完成等功能
 */
@Service
public class UserAchievementService {
    
    @Autowired
    private UserAchievementMapper userAchievementMapper;
    
    /**
     * 根据用户ID查询用户成就列表
     * @param userId 用户ID
     * @return 用户成就列表
     */
    public List<UserAchievement> getUserAchievements(Long userId) {
        return userAchievementMapper.selectByUserId(userId);
    }
    
    /**
     * 根据用户ID和完成状态查询
     * @param userId 用户ID
     * @param isCompleted 是否已完成
     * @return 用户成就列表
     */
    public List<UserAchievement> getUserAchievementsByCompleted(Long userId, Boolean isCompleted) {
        return userAchievementMapper.selectByUserIdAndCompleted(userId, isCompleted);
    }
    
    /**
     * 根据用户ID和成就ID查询
     * @param userId 用户ID
     * @param achievementId 成就ID
     * @return 用户成就实例
     */
    public UserAchievement getUserAchievement(Long userId, Long achievementId) {
        return userAchievementMapper.selectByUserIdAndAchievementId(userId, achievementId);
    }
    
    /**
     * 根据用户ID和分类查询
     * @param userId 用户ID
     * @param category 成就分类
     * @return 用户成就列表
     */
    public List<UserAchievement> getUserAchievementsByCategory(Long userId, String category) {
        return userAchievementMapper.selectByUserIdAndCategory(userId, category);
    }
    
    /**
     * 创建或更新用户成就记录
     * 如果记录不存在，创建新记录；如果存在，更新进度
     * @param userId 用户ID
     * @param achievementId 成就ID
     * @param progress 完成进度（0-100）
     * @return 用户成就实例
     */
    @Transactional
    public UserAchievement createOrUpdateUserAchievement(Long userId, Long achievementId, Integer progress) {
        UserAchievement userAchievement = getUserAchievement(userId, achievementId);
        
        if (userAchievement == null) {
            // 创建新记录
            userAchievement = new UserAchievement();
            userAchievement.setUserId(userId);
            userAchievement.setAchievementId(achievementId);
            userAchievement.setIsCompleted(false);
            userAchievement.setProgress(progress);
            userAchievement.setRewardClaimed(false);
            userAchievement.setCreatedAt(LocalDateTime.now());
            userAchievement.setUpdatedAt(LocalDateTime.now());
            userAchievementMapper.insert(userAchievement);
        } else {
            // 更新现有记录
            userAchievement.setProgress(progress);
            userAchievement.setUpdatedAt(LocalDateTime.now());
            
            // 如果进度达到100且未完成，标记为已完成
            if (progress >= 100 && !userAchievement.getIsCompleted()) {
                userAchievement.setIsCompleted(true);
                userAchievement.setCompletedAt(LocalDateTime.now());
            }
            
            userAchievementMapper.updateById(userAchievement);
        }
        
        return userAchievement;
    }
    
    /**
     * 完成成就
     * @param userId 用户ID
     * @param achievementId 成就ID
     * @return 是否成功
     */
    @Transactional
    public boolean completeAchievement(Long userId, Long achievementId) {
        UserAchievement userAchievement = getUserAchievement(userId, achievementId);
        
        if (userAchievement == null) {
            // 如果记录不存在，创建新记录并标记为已完成
            userAchievement = new UserAchievement();
            userAchievement.setUserId(userId);
            userAchievement.setAchievementId(achievementId);
            userAchievement.setIsCompleted(true);
            userAchievement.setProgress(100);
            userAchievement.setCompletedAt(LocalDateTime.now());
            userAchievement.setRewardClaimed(false);
            userAchievement.setCreatedAt(LocalDateTime.now());
            userAchievement.setUpdatedAt(LocalDateTime.now());
            return userAchievementMapper.insert(userAchievement) > 0;
        } else if (!userAchievement.getIsCompleted()) {
            // 如果记录存在但未完成，更新为已完成
            userAchievement.setIsCompleted(true);
            userAchievement.setProgress(100);
            userAchievement.setCompletedAt(LocalDateTime.now());
            userAchievement.setUpdatedAt(LocalDateTime.now());
            return userAchievementMapper.updateById(userAchievement) > 0;
        }
        
        return true; // 已经完成
    }
    
    /**
     * 领取成就奖励
     * @param userId 用户ID
     * @param achievementId 成就ID
     * @return 是否成功
     */
    @Transactional
    public boolean claimReward(Long userId, Long achievementId) {
        UserAchievement userAchievement = getUserAchievement(userId, achievementId);
        
        if (userAchievement == null || !userAchievement.getIsCompleted()) {
            return false; // 成就未完成，无法领取奖励
        }
        
        if (userAchievement.getRewardClaimed()) {
            return false; // 奖励已领取
        }
        
        userAchievement.setRewardClaimed(true);
        userAchievement.setUpdatedAt(LocalDateTime.now());
        return userAchievementMapper.updateById(userAchievement) > 0;
    }
    
    /**
     * 获取用户成就统计
     * @param userId 用户ID
     * @return 统计信息（总成就数、已完成数、完成率等）
     */
    public AchievementStats getAchievementStats(Long userId) {
        List<UserAchievement> allAchievements = getUserAchievements(userId);
        long totalCount = allAchievements.size();
        long completedCount = allAchievements.stream()
                .filter(UserAchievement::getIsCompleted)
                .count();
        
        AchievementStats stats = new AchievementStats();
        stats.setTotalCount(totalCount);
        stats.setCompletedCount(completedCount);
        stats.setCompletionRate(totalCount > 0 ? (double) completedCount / totalCount * 100 : 0.0);
        
        return stats;
    }
    
    /**
     * 成就统计信息
     */
    public static class AchievementStats {
        private long totalCount;
        private long completedCount;
        private double completionRate;
        
        // Getters and Setters
        public long getTotalCount() {
            return totalCount;
        }
        
        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }
        
        public long getCompletedCount() {
            return completedCount;
        }
        
        public void setCompletedCount(long completedCount) {
            this.completedCount = completedCount;
        }
        
        public double getCompletionRate() {
            return completionRate;
        }
        
        public void setCompletionRate(double completionRate) {
            this.completionRate = completionRate;
        }
    }
}

