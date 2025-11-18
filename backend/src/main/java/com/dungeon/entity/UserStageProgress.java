package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关卡进度实体
 * 对应数据库表：user_stage_progress
 */
@Data
@TableName("user_stage_progress")
public class UserStageProgress {
    /**
     * 进度ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 关卡编号
     */
    private Integer stageNumber;
    
    /**
     * 章节编号
     */
    private Integer chapterNumber;
    
    /**
     * 是否已通过
     */
    private Boolean isPassed;
    
    /**
     * 是否已解锁（通过前一关后解锁）
     */
    private Boolean isUnlocked;
    
    /**
     * 最佳结果：victory/defeat
     */
    private String bestResult;
    
    /**
     * 首次通过时间
     */
    private LocalDateTime passedAt;
    
    /**
     * 尝试次数
     */
    private Integer attemptCount;
    
    /**
     * 最佳评分（可选）
     */
    private Integer bestScore;
}

