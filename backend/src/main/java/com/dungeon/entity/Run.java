package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 地牢探索记录实体
 * 对应数据库表：runs
 */
@Data
@TableName("runs")
public class Run {
    /**
     * 探索记录ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 玩家角色实例ID
     */
    private Long userPlayerCharacterId;
    
    /**
     * 地牢ID
     */
    private Long dungeonId;
    
    /**
     * 当前关卡编号
     */
    private Integer stageNumber;
    
    /**
     * 当前章节编号
     */
    private Integer chapterNumber;
    
    /**
     * 难度
     */
    private String difficulty;
    
    /**
     * 准备快照（JSON格式：包含手牌、道具、货币余量）
     */
    private String preparationSnapshot;
    
    /**
     * 关卡内进度（JSON格式：探索进度、已触发事件、已击败敌人）
     */
    private String currentStageProgress;
    
    /**
     * 探索结果：victory-胜利, defeat-失败, abandon-放弃
     */
    private String result;
    
    /**
     * 开始时间
     */
    private LocalDateTime startedAt;
    
    /**
     * 结束时间
     */
    private LocalDateTime endedAt;
    
    /**
     * 奖励快照（JSON格式）
     */
    private String rewardSnapshot;
}

