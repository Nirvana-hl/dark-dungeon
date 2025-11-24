package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户成就实例实体
 * 对应数据库表：user_achievements
 * 
 * 设计说明：
 * - 遵循项目的"模板/实例分离"设计原则
 * - achievements 表存储成就定义（模板），所有用户共用
 * - user_achievements 表存储用户成就完成状态（实例），每个用户独立
 * - 支持成就进度追踪、完成时间记录、奖励领取状态管理
 */
@Data
@TableName("user_achievements")
public class UserAchievement {
    /**
     * 实例ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID（关联users.id）
     */
    private Long userId;
    
    /**
     * 成就ID（关联achievements.id）
     */
    private Long achievementId;
    
    /**
     * 是否已完成（0-未完成，1-已完成）
     */
    private Boolean isCompleted;
    
    /**
     * 完成进度（0-100）
     */
    private Integer progress;
    
    /**
     * 完成时间（NULL表示未完成）
     */
    private LocalDateTime completedAt;
    
    /**
     * 是否已领取奖励（0-未领取，1-已领取）
     */
    private Boolean rewardClaimed;
    
    /**
     * 创建时间（首次触发该成就时）
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

