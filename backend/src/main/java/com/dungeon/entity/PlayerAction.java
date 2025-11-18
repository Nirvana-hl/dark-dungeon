package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 玩家操作记录实体
 * 对应数据库表：player_actions
 */
@Data
@TableName("player_actions")
public class PlayerAction {
    /**
     * 操作记录ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 操作类型
     */
    private String actionType;
    
    /**
     * 场景来源：camp-营地, dungeon-地牢, battle-战斗
     */
    private String sourceScene;
    
    /**
     * 元数据（JSON格式）
     */
    private String metadata;
    
    /**
     * 发生时间
     */
    private LocalDateTime occurredAt;
}

