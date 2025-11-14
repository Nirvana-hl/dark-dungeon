package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 地牢探索记录实体
 */
@Data
@TableName("runs")
public class Run {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long characterId;
    private Integer dungeonId;
    private String difficulty;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String result; // victory/defeat/abandon
    private Integer monstersDefeated;
    private Integer durationMin;
    private Integer damageDone;
    private Integer damageTaken;
    private Integer rewardGold;
    private Integer rewardExp;
    private LocalDateTime createdAt;
}

