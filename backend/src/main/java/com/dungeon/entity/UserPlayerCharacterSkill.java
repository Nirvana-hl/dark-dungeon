package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职业技能实例实体
 * 对应数据库表：user_player_character_skills
 */
@Data
@TableName("user_player_character_skills")
public class UserPlayerCharacterSkill {
    /**
     * 实例ID（UUID格式）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 玩家角色实例ID
     */
    private String userPlayerCharacterId;
    
    /**
     * 技能模板ID
     */
    private String skillId;
    
    /**
     * 解锁时间
     */
    private LocalDateTime unlockedAt;
}

