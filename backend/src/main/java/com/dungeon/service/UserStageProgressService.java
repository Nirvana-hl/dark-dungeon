package com.dungeon.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dungeon.entity.UserStageProgress;
import com.dungeon.mapper.UserStageProgressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户关卡进度服务
 */
@Service
public class UserStageProgressService {
    
    @Autowired
    private UserStageProgressMapper userStageProgressMapper;
    
    /**
     * 获取用户的所有关卡进度
     */
    public List<UserStageProgress> getUserStageProgress(Long userId) {
        QueryWrapper<UserStageProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByAsc("stage_number");
        return userStageProgressMapper.selectList(wrapper);
    }
    
    /**
     * 获取用户的关卡进度（单个）
     */
    public UserStageProgress getUserStageProgress(Long userId, Integer stageNumber) {
        return userStageProgressMapper.selectByUserIdAndStageNumber(userId, stageNumber);
    }
    
    /**
     * 获取用户指定章节的关卡进度
     */
    public List<UserStageProgress> getUserStageProgressByChapter(Long userId, Integer chapterNumber) {
        return userStageProgressMapper.selectByUserIdAndChapterNumber(userId, chapterNumber);
    }
    
    /**
     * 初始化用户关卡进度（创建用户时调用）
     */
    @Transactional
    public void initializeUserStageProgress(Long userId) {
        // 解锁第1关
        UserStageProgress progress = new UserStageProgress();
        progress.setUserId(userId);
        progress.setStageNumber(1);
        progress.setChapterNumber(1);
        progress.setIsUnlocked(true);
        progress.setIsPassed(false);
        progress.setAttemptCount(0);
        userStageProgressMapper.insert(progress);
    }
    
    /**
     * 通过关卡
     */
    @Transactional
    public void passStage(Long userId, Integer stageNumber, Integer chapterNumber) {
        // 更新当前关卡为已通过
        UserStageProgress currentProgress = userStageProgressMapper.selectByUserIdAndStageNumber(userId, stageNumber);
        if (currentProgress != null) {
            if (!currentProgress.getIsPassed()) {
                currentProgress.setIsPassed(true);
                currentProgress.setPassedAt(LocalDateTime.now());
                currentProgress.setBestResult("victory");
            }
            currentProgress.setAttemptCount(currentProgress.getAttemptCount() + 1);
            userStageProgressMapper.updateById(currentProgress);
        } else {
            // 如果不存在，创建新记录
            currentProgress = new UserStageProgress();
            currentProgress.setUserId(userId);
            currentProgress.setStageNumber(stageNumber);
            currentProgress.setChapterNumber(chapterNumber);
            currentProgress.setIsPassed(true);
            currentProgress.setIsUnlocked(true);
            currentProgress.setPassedAt(LocalDateTime.now());
            currentProgress.setBestResult("victory");
            currentProgress.setAttemptCount(1);
            userStageProgressMapper.insert(currentProgress);
        }
        
        // 解锁下一关
        Integer nextStageNumber = stageNumber + 1;
        Integer nextChapterNumber = (int) Math.ceil(nextStageNumber / 5.0);
        
        UserStageProgress nextProgress = userStageProgressMapper.selectByUserIdAndStageNumber(userId, nextStageNumber);
        if (nextProgress == null) {
            nextProgress = new UserStageProgress();
            nextProgress.setUserId(userId);
            nextProgress.setStageNumber(nextStageNumber);
            nextProgress.setChapterNumber(nextChapterNumber);
            nextProgress.setIsUnlocked(true);
            nextProgress.setIsPassed(false);
            nextProgress.setAttemptCount(0);
            userStageProgressMapper.insert(nextProgress);
        } else if (!nextProgress.getIsUnlocked()) {
            nextProgress.setIsUnlocked(true);
            userStageProgressMapper.updateById(nextProgress);
        }
    }
    
    /**
     * 记录关卡尝试（失败时调用）
     */
    @Transactional
    public void recordStageAttempt(Long userId, Integer stageNumber, Integer chapterNumber, String result) {
        UserStageProgress progress = userStageProgressMapper.selectByUserIdAndStageNumber(userId, stageNumber);
        if (progress == null) {
            progress = new UserStageProgress();
            progress.setUserId(userId);
            progress.setStageNumber(stageNumber);
            progress.setChapterNumber(chapterNumber);
            progress.setIsUnlocked(true);
            progress.setIsPassed(false);
            progress.setBestResult(result);
            progress.setAttemptCount(1);
            userStageProgressMapper.insert(progress);
        } else {
            progress.setAttemptCount(progress.getAttemptCount() + 1);
            if (progress.getBestResult() == null || "defeat".equals(progress.getBestResult())) {
                progress.setBestResult(result);
            }
            userStageProgressMapper.updateById(progress);
        }
    }
    
    /**
     * 获取用户已通过的关卡数量
     */
    public Integer getPassedStageCount(Long userId) {
        return userStageProgressMapper.countPassedStages(userId);
    }
    
    /**
     * 获取用户已解锁的关卡数量
     */
    public Integer getUnlockedStageCount(Long userId) {
        return userStageProgressMapper.countUnlockedStages(userId);
    }
    
    /**
     * 获取用户当前最高关卡编号
     */
    public Integer getMaxStageNumber(Long userId) {
        Integer maxStage = userStageProgressMapper.selectMaxStageNumber(userId);
        return maxStage != null ? maxStage : 1;
    }
}

