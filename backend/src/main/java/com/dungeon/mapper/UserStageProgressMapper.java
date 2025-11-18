package com.dungeon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dungeon.entity.UserStageProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户关卡进度 Mapper
 */
@Mapper
public interface UserStageProgressMapper extends BaseMapper<UserStageProgress> {
    
    /**
     * 根据用户ID查询所有关卡进度
     * @param userId 用户ID
     * @return 关卡进度列表
     */
    List<UserStageProgress> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和关卡编号查询进度
     * @param userId 用户ID
     * @param stageNumber 关卡编号
     * @return 关卡进度
     */
    UserStageProgress selectByUserIdAndStageNumber(@Param("userId") Long userId, 
                                                    @Param("stageNumber") Integer stageNumber);
    
    /**
     * 根据用户ID和章节编号查询进度
     * @param userId 用户ID
     * @param chapterNumber 章节编号
     * @return 关卡进度列表
     */
    List<UserStageProgress> selectByUserIdAndChapterNumber(@Param("userId") Long userId, 
                                                           @Param("chapterNumber") Integer chapterNumber);
    
    /**
     * 查询用户已通过的关卡数量
     * @param userId 用户ID
     * @return 已通过关卡数量
     */
    Integer countPassedStages(@Param("userId") Long userId);
    
    /**
     * 查询用户已解锁的关卡数量
     * @param userId 用户ID
     * @return 已解锁关卡数量
     */
    Integer countUnlockedStages(@Param("userId") Long userId);
    
    /**
     * 查询用户当前最高关卡编号
     * @param userId 用户ID
     * @return 最高关卡编号
     */
    Integer selectMaxStageNumber(@Param("userId") Long userId);
}

