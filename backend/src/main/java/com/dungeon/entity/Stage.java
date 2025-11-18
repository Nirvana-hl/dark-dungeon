package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 关卡模板实体
 * 对应数据库表：stages
 */
@Data
@TableName("stages")
public class Stage {
    /**
     * 关卡ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关卡编号（1, 2, 3...）
     */
    private Integer stageNumber;
    
    /**
     * 章节编号（每5关为一章：1, 2, 3...）
     */
    private Integer chapterNumber;
    
    /**
     * 关联的地牢模板ID
     */
    private Long dungeonId;
    
    /**
     * 关卡名称
     */
    private String stageName;
    
    /**
     * 难度：easy/normal/hard/expert
     */
    private String difficulty;
    
    /**
     * 是否为Boss关卡（每章第5关）
     */
    private Boolean isBossStage;
    
    /**
     * 敌人池配置（JSON格式：用于随机生成敌人）
     */
    private String enemyPool;
    
    /**
     * 事件池配置（JSON格式：用于随机生成事件）
     */
    private String eventPool;
    
    /**
     * 奖励池配置（JSON格式：关卡完成奖励）
     */
    private String rewardPool;
    
    /**
     * 探索地图配置（JSON格式：房间、路径、探索点）
     */
    private String explorationMap;
    
    /**
     * 关卡描述
     */
    private String description;
}

