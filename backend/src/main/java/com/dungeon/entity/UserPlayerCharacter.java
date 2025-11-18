package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 玩家角色实例实体
 * 对应数据库表：user_player_characters
 */
@Data
@TableName("user_player_characters")
public class UserPlayerCharacter {
    /**
     * 玩家角色实例ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 玩家角色模板ID
     */
    private String playerCharacterId;
    
    /**
     * 最大血量（随等级提升）
     */
    private Integer maxHp;
    
    /**
     * 当前血量（跨关卡继承）
     */
    private Integer currentHp;
    
    /**
     * 最大行动点限制（随等级提升）
     */
    private Integer maxActionPoints;
    
    /**
     * 当前剩余行动点（每轮重置）
     */
    private Integer currentActionPoints;
    
    /**
     * 当前压力值（跨关卡持续累积）
     */
    private Integer currentStress;
    
    /**
     * 当前压力层级（1-4）
     */
    private Integer stressLevel;
    
    /**
     * 当前激活的压力debuff列表（JSON格式）
     */
    private String stressDebuffs;
}

