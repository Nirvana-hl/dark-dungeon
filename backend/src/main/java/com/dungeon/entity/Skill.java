package com.dungeon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 职业技能模板实体
 * 对应数据库表：skills
 */
@Data
@TableName("skills")
public class Skill {
    /**
     * 技能模板ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 策划短码（技能代码，如：warden_sacred_shield）
     */
    private String code;
    
    /**
     * 职业代码（如：warden, occultist, ranger, warrior）
     */
    @TableField("player_character_code")
    private String playerCharacterCode;
    
    /**
     * 技能名称
     */
    private String name;
    
    /**
     * 技能描述
     */
    private String description;
    
    /**
     * 技能效果配置（JSON格式）
     */
    private String effectPayload;
    
    /**
     * 解锁所需职业等级
     */
    private Integer requiredLevel;
    
    /**
     * 技能树坐标（JSON格式：{row: INT, column: INT}）
     */
    private String positionInTree;
    
    /**
     * 前置技能依赖（JSON格式：["skill_code1", "skill_code2"]）
     */
    private String unlockPath;
}

