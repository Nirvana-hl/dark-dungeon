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
     * 探索记录ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 玩家角色实例ID
     */
    private String userPlayerCharacterId;
    
    /**
     * 地牢ID
     */
    private String dungeonId;
    
    /**
     * 难度
     */
    private String difficulty;
    
    /**
     * 准备快照（JSON格式：包含手牌、道具、货币余量）
     */
    private String preparationSnapshot;
    
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

