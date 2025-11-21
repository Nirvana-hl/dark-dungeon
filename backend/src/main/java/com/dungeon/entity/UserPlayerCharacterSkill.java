package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职业技能实例实体
 * 对应数据库表：user_player_character_skills
 * 
 * 设计说明：
 * - 技能解锁关联到"用户+职业模板"，而不是"角色实例"
 * - 这样即使删除重建角色实例，技能解锁也不会丢失
 * - 技能解锁是用户对职业的永久投资
 */
@Data
@TableName("user_player_character_skills")
public class UserPlayerCharacterSkill {
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
     * 职业模板ID（关联player_characters.id）
     */
    private Long playerCharacterId;
    
    /**
     * 技能模板ID（关联skills.id）
     */
    private Long skillId;
    
    /**
     * 解锁时间
     */
    private LocalDateTime unlockedAt;
}

