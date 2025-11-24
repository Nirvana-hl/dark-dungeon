package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.UserAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户成就 Mapper
 */
@Mapper
public interface UserAchievementMapper extends BaseMapper<UserAchievement> {
    
    /**
     * 根据用户ID查询用户成就列表
     * @param userId 用户ID
     * @return 用户成就列表
     */
    List<UserAchievement> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和完成状态查询
     * @param userId 用户ID
     * @param isCompleted 是否已完成
     * @return 用户成就列表
     */
    List<UserAchievement> selectByUserIdAndCompleted(@Param("userId") Long userId, @Param("isCompleted") Boolean isCompleted);
    
    /**
     * 根据用户ID和成就ID查询
     * @param userId 用户ID
     * @param achievementId 成就ID
     * @return 用户成就实例
     */
    UserAchievement selectByUserIdAndAchievementId(@Param("userId") Long userId, @Param("achievementId") Long achievementId);
    
    /**
     * 根据用户ID和分类查询
     * @param userId 用户ID
     * @param category 成就分类
     * @return 用户成就列表
     */
    List<UserAchievement> selectByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);
}

